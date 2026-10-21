package com.agent1k.region.data;

import com.agent1k.FuryRaids;
import com.agent1k.api.flag.RegionFlag;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.function.IntUnaryOperator;

public final class Database {

    private final File file;
    private final Logger logger;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "FuryRaids-DB");
        t.setDaemon(true);
        return t;
    });
    private Connection connection;
    private IntUnaryOperator defaultDurability = id -> 5;

    public Database(@NotNull FuryRaids plugin) {
        this(new File(plugin.getDataFolder(), plugin.getConfig().getString("database.file", "data.db")), plugin.getLogger());
        defaultDurability = id -> {
            RegionType type = plugin.getBlocks().byId(id);
            return type == null ? 5 : type.durability();
        };
    }

    Database(File file, Logger logger) {
        this.file = file;
        this.logger = logger;
    }

    public void init() {
        try {
            File parent = file.getAbsoluteFile().getParentFile();
            if (parent != null) parent.mkdirs();
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            try (Statement s = connection.createStatement()) {
                s.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS regions (
                            id TEXT PRIMARY KEY,
                            owner TEXT NOT NULL,
                            world TEXT NOT NULL,
                            x INTEGER NOT NULL,
                            y INTEGER NOT NULL,
                            z INTEGER NOT NULL,
                            radius INTEGER NOT NULL,
                            type_id INTEGER NOT NULL,
                            flags TEXT NOT NULL,
                            members TEXT NOT NULL,
                            durability INTEGER,
                            max_durability INTEGER,
                            last_damage_time INTEGER NOT NULL DEFAULT 0
                        )
                        """);
                Set<String> columns = new HashSet<>();
                try (ResultSet info = s.executeQuery("PRAGMA table_info(regions)")) {
                    while (info.next()) columns.add(info.getString("name"));
                }
                if (!columns.contains("durability")) s.executeUpdate("ALTER TABLE regions ADD COLUMN durability INTEGER");
                if (!columns.contains("max_durability")) s.executeUpdate("ALTER TABLE regions ADD COLUMN max_durability INTEGER");
                if (!columns.contains("last_damage_time")) s.executeUpdate("ALTER TABLE regions ADD COLUMN last_damage_time INTEGER NOT NULL DEFAULT 0");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка инициализации БД", e);
        }
    }

    @NotNull
    public Collection<RegionData> loadAll() {
        Collection<RegionData> out = new ArrayList<>();
        Collection<RegionData> migrated = new ArrayList<>();
        if (connection == null) throw new IllegalStateException("БД не подключена");
        try (Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM regions")) {
            while (rs.next()) {
                RegionData region = fromRow(rs);
                out.add(region);
                if (rs.getObject("max_durability") == null || rs.getObject("durability") == null) migrated.add(region);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка загрузки регионов", e);
        }
        for (RegionData region : migrated) saveSync(region);
        return out;
    }

    @NotNull
    public CompletableFuture<Void> save(@NotNull RegionData region) {
        RegionData snapshot = new RegionData(region.getId(), region.getOwner(), region.getWorld(),
                region.getX(), region.getY(), region.getZ(), region.getRadius(), region.getTypeId(),
                region.membersSnapshot(), region.getFlags(), region.getDurability(),
                region.getMaxDurability(), region.getLastDamageTime());
        return CompletableFuture.runAsync(() -> saveSync(snapshot), executor);
    }

    @NotNull
    public CompletableFuture<Void> delete(@NotNull String id) {
        return CompletableFuture.runAsync(() -> deleteSync(id), executor);
    }

    private void saveSync(@NotNull RegionData r) {
        if (connection == null) throw new IllegalStateException("БД не подключена");
        try (PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO regions(id, owner, world, x, y, z, radius, type_id, flags, members, durability, max_durability, last_damage_time)
                VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)
                ON CONFLICT(id) DO UPDATE SET
                    owner=excluded.owner,
                    world=excluded.world,
                    x=excluded.x, y=excluded.y, z=excluded.z,
                    radius=excluded.radius,
                    type_id=excluded.type_id,
                    flags=excluded.flags,
                    members=excluded.members,
                    durability=excluded.durability,
                    max_durability=excluded.max_durability,
                    last_damage_time=excluded.last_damage_time
                """)) {
            ps.setString(1, r.getId());
            ps.setString(2, r.getOwner().toString());
            ps.setString(3, r.getWorld());
            ps.setInt(4, r.getX());
            ps.setInt(5, r.getY());
            ps.setInt(6, r.getZ());
            ps.setInt(7, r.getRadius());
            ps.setInt(8, r.getTypeId());
            ps.setString(9, encodeFlags(r.getFlags()));
            ps.setString(10, encodeMembers(r.membersSnapshot()));
            ps.setInt(11, r.getDurability());
            ps.setInt(12, r.getMaxDurability());
            ps.setLong(13, r.getLastDamageTime());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка сохранения региона " + r.getId(), e);
        }
    }

    private void deleteSync(@NotNull String id) {
        if (connection == null) throw new IllegalStateException("БД не подключена");
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM regions WHERE id=?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка удаления региона " + id, e);
        }
    }

    @NotNull
    private RegionData fromRow(@NotNull ResultSet rs) throws SQLException {
        int maximum = rs.getObject("max_durability") == null
                ? defaultDurability.applyAsInt(rs.getInt("type_id")) : Math.max(0, rs.getInt("max_durability"));
        int current = rs.getObject("durability") == null ? maximum : rs.getInt("durability");
        return new RegionData(
                rs.getString("id"),
                UUID.fromString(rs.getString("owner")),
                rs.getString("world"),
                rs.getInt("x"), rs.getInt("y"), rs.getInt("z"),
                rs.getInt("radius"),
                rs.getInt("type_id"),
                decodeMembers(rs.getString("members")),
                decodeFlags(rs.getString("flags")), current, maximum, rs.getLong("last_damage_time")
        );
    }

    @NotNull
    private String encodeFlags(@NotNull EnumMap<RegionFlag, Boolean> flags) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (var e : flags.entrySet()) {
            if (!first) sb.append(';');
            sb.append(e.getKey().getKey()).append('=').append(e.getValue());
            first = false;
        }
        return sb.toString();
    }

    @NotNull
    private EnumMap<RegionFlag, Boolean> decodeFlags(@NotNull String raw) {
        EnumMap<RegionFlag, Boolean> map = RegionFlag.defaults();
        if (raw.isEmpty()) return map;
        for (String part : raw.split(";")) {
            int eq = part.indexOf('=');
            if (eq <= 0) continue;
            RegionFlag flag = RegionFlag.from(part.substring(0, eq));
            if (flag == null) continue;
            map.put(flag, Boolean.parseBoolean(part.substring(eq + 1)));
        }
        return map;
    }

    @NotNull
    private String encodeMembers(@NotNull Set<UUID> members) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (UUID u : members) {
            if (!first) sb.append(',');
            sb.append(u);
            first = false;
        }
        return sb.toString();
    }

    @NotNull
    private Set<UUID> decodeMembers(@NotNull String raw) {
        Set<UUID> set = new HashSet<>();
        if (raw.isEmpty()) return set;
        for (String part : raw.split(",")) {
            if (part.isBlank()) continue;
            try {
                set.add(UUID.fromString(part));
            } catch (IllegalArgumentException ignored) {}
        }
        return set;
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.severe("Очередь БД не завершена: соединение оставлено открытым до завершения задач.");
                return;
            }
            if (connection != null) connection.close();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (SQLException e) {
            logger.severe("Ошибка закрытия БД: " + e.getMessage());
        }
    }
}
