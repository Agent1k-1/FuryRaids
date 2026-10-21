package com.agent1k.region.gui;

import com.agent1k.FuryRaids;
import com.agent1k.region.action.ActionType;
import com.agent1k.region.click.ClickType;
import com.agent1k.region.data.RegionData;
import com.agent1k.region.gui.menu.config.MainMenu;
import com.agent1k.region.listener.MechanicsListener;
import com.agent1k.region.util.ColorUtil;
import com.sk89q.worldguard.protection.flags.Flags;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.Predicate;

public final class RegionGUI implements Listener {
    private final Supplier<MainMenu> menus;
    private final Function<Location, RegionData> regionAt;
    private final Function<String, RegionData> regionById;
    private final BiPredicate<Player, RegionData> protection;
    private final Renderer renderer;
    private final Consumer<Runnable> nextTick;
    private final BiConsumer<Player, String> actions;
    private Predicate<PlayerInteractEvent> repairClick = event -> false;

    public RegionGUI(FuryRaids plugin) {
        this(plugin.getMenuLoader()::getMainMenu, plugin.getRegionManager()::at,
                plugin.getRegionManager()::byId, (player, region) -> {
                    World world = Bukkit.getWorld(region.getWorld());
                    return world != null && plugin.getWorldGuardHook().canBuild(player,
                            new Location(world, region.getX(), region.getY(), region.getZ()), Flags.INTERACT);
                }, (player, menu, holder) -> render(plugin, player, menu, holder),
                task -> Bukkit.getScheduler().runTask(plugin, task),
                (player, line) -> ActionType.dispatch(player, format(plugin, player, line)));
        repairClick = plugin.getCores()::isRepairClick;
    }

    RegionGUI(Supplier<MainMenu> menus, Function<Location, RegionData> regionAt,
              Function<String, RegionData> regionById, BiPredicate<Player, RegionData> protection,
              Renderer renderer, Consumer<Runnable> nextTick, BiConsumer<Player, String> actions) {
        this.menus = menus;
        this.regionAt = regionAt;
        this.regionById = regionById;
        this.protection = protection;
        this.renderer = renderer;
        this.nextTick = nextTick;
        this.actions = actions;
    }

    public boolean open(Player player, String regionId) {
        MainMenu menu = menus.get();
        RegionData region = regionById.apply(regionId);
        if (menu == null || !canView(player, region)) return false;
        MenuHolder holder = new MenuHolder(player.getUniqueId(), regionId, menu);
        holder.inventory = renderer.render(player, menu, holder);
        player.openInventory(holder.inventory);
        return true;
    }

    public boolean canView(Player player, RegionData region) {
        return region != null && player.hasPermission("eregionplus.menu")
                && (region.isOwnerOrMember(player.getUniqueId()) || player.hasPermission("eregionplus.admin"))
                && protection.test(player, region);
    }

    public @Nullable RegionData getOpenRegion(Player player) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        if (!(inventory.getHolder() instanceof MenuHolder holder) || menus.get() != holder.menu
                || !holder.viewer.equals(player.getUniqueId())) return null;
        RegionData region = regionById.apply(holder.regionId);
        return canView(player, region) ? region : null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (repairClick.test(event)) return;
        MainMenu menu = menus.get();
        if (menu == null || event.getHand() != EquipmentSlot.HAND
                || menu.openClicks().stream().noneMatch(click -> click.matches(event.getAction()))) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        RegionData region = regionAt.apply(block.getLocation());
        Player player = event.getPlayer();
        if (!MechanicsListener.isCenter(region, block) || !canView(player, region)) return;
        Inventory previous = player.getOpenInventory().getTopInventory();
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        nextTick.accept(() -> {
            if (player.isOnline() && menus.get() == menu
                    && player.getOpenInventory().getTopInventory() == previous) open(player, region.getId());
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (!(inventory.getHolder() instanceof MenuHolder holder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)
                || !holder.viewer.equals(player.getUniqueId())) return;
        ClickType click = ClickType.from(event.getClick());
        if (click == null || event.getRawSlot() < 0 || event.getRawSlot() >= inventory.getSize()) return;
        MainMenu.MenuItem item = holder.menu.items().get(event.getRawSlot());
        if (item == null) return;
        List<String> lines = item.actionsFor(click);
        if (lines.isEmpty()) return;
        nextTick.accept(() -> {
            if (!player.isOnline() || menus.get() != holder.menu
                    || player.getOpenInventory().getTopInventory() != inventory) return;
            RegionData region = regionById.apply(holder.regionId);
            if (!canView(player, region)) {
                player.closeInventory();
                return;
            }
            for (String line : lines) actions.accept(player, line);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof MenuHolder) event.setCancelled(true);
    }

    public void closeAll(Iterable<? extends Player> players) {
        for (Player player : players) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder) {
                player.closeInventory();
            }
        }
    }

    private static Inventory render(FuryRaids plugin, Player player,
                                    MainMenu menu, InventoryHolder holder) {
        Inventory inventory = Bukkit.createInventory(holder, menu.size(), format(plugin, player, menu.name()));
        for (MainMenu.MenuItem button : menu.items().values()) {
            ItemStack stack = new ItemStack(button.material());
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(format(plugin, player, button.name()));
            meta.setLore(button.lore().stream().map(line -> format(plugin, player, line)).toList());
            stack.setItemMeta(meta);
            inventory.setItem(button.slot(), stack);
        }
        return inventory;
    }

    private static String format(FuryRaids plugin, Player player, String line) {
        String text = plugin.getMessages().isPapiEnabled() ? PlaceholderAPI.setPlaceholders(player, line) : line;
        return ColorUtil.apply(text);
    }

    @FunctionalInterface
    interface Renderer {
        Inventory render(Player player, MainMenu menu, InventoryHolder holder);
    }

    private static final class MenuHolder implements InventoryHolder {
        private final UUID viewer;
        private final String regionId;
        private final MainMenu menu;
        private Inventory inventory;

        private MenuHolder(UUID viewer, String regionId, MainMenu menu) {
            this.viewer = viewer;
            this.regionId = regionId;
            this.menu = menu;
        }

        @Override
        public @NotNull Inventory getInventory() { return inventory; }
    }
}
