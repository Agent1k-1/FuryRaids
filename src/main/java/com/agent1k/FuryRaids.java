package com.agent1k;

import com.agent1k.api.FuryRaidsAPI;
import com.agent1k.api.FuryRaidsProvider;
import com.agent1k.api.internal.APIImpl;
import com.agent1k.region.command.PSCommand;
import com.agent1k.region.config.BlockConfig;
import com.agent1k.region.config.MessagesConfig;
import com.agent1k.region.data.Database;
import com.agent1k.region.data.ViewSettings;
import com.agent1k.region.gui.RegionGUI;
import com.agent1k.region.gui.menu.loader.MenuLoader;
import com.agent1k.file.FolderLoader;
import com.agent1k.region.core.CoreManager;
import com.agent1k.region.hologram.HologramManager;
import com.agent1k.region.hook.PAPIHook;
import com.agent1k.region.hook.WorldGuardHook;
import com.agent1k.region.listener.AccessListener;
import com.agent1k.region.listener.ChestListener;
import com.agent1k.region.listener.ExplosionListener;
import com.agent1k.region.listener.FlagProtectionListener;
import com.agent1k.region.listener.MechanicsListener;
import com.agent1k.region.listener.RegionBlockListener;
import com.agent1k.region.listener.WorldSyncListener;
import com.agent1k.region.manager.RegionManager;
import com.agent1k.region.manager.RegionMemberInputManager;
import com.agent1k.region.manager.RegionViewManager;
import com.agent1k.region.util.ItemUtil;
import com.agent1k.tnt.TntBlockTracker;
import com.agent1k.tnt.TntRegistry;
import com.agent1k.tnt.command.ETNTCommand;
import com.agent1k.tnt.config.TntConfig;
import com.agent1k.tnt.listener.BreakListener;
import com.agent1k.tnt.listener.InteractListener;
import com.agent1k.tnt.listener.PlacementListener;
import com.agent1k.tnt.listener.PrimeListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.logging.Level;

public final class FuryRaids extends JavaPlugin {

    private static FuryRaids instance;
    private FolderLoader files;
    private CoreManager cores;

    private MessagesConfig messages;
    private BlockConfig blocks;
    private Database database;
    private RegionManager regionManager;
    private ItemUtil items;
    private WorldGuardHook worldGuardHook;
    private PAPIHook papiHook;
    private FuryRaidsAPI api;
    private RegionViewManager regionViews;
    private MenuLoader menuLoader;
    private RegionGUI regionGUI;
    private RegionMemberInputManager memberInputs;
    private HologramManager holograms;
    private com.agent1k.tnt.config.MessagesConfig tntMessages;
    private TntConfig tntConfig;
    private TntRegistry tntRegistry;
    private com.agent1k.tnt.util.ItemUtil tntItems;
    private TntBlockTracker tntTracker;
    private com.agent1k.tnt.hook.PAPIHook tntPapiHook;

    @Override
    public void onEnable() {
        instance = this;
        try {
            getFiles().loadAll();
            if (ViewSettings.installDefaults(getConfig(), "view")) saveConfig();
            worldGuardHook = new WorldGuardHook(this);
            worldGuardHook.verify();
            if (!worldGuardHook.isPresent()) return;
            enableRegions();
            enableTnt();

            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                papiHook = new PAPIHook(this);
                papiHook.register();
                tntPapiHook = new com.agent1k.tnt.hook.PAPIHook(this);
                tntPapiHook.register();
                getLogger().info("PlaceholderAPI подключён: плейсхолдеры регионов и TNT зарегистрированы.");
            }

            holograms = new HologramManager(this);
            registerListeners(holograms);
            holograms.start();
            getLogger().info("FuryRaids запущен. Системы регионов и TNT подключены.");
        } catch (RuntimeException | LinkageError e) {
            getLogger().log(Level.SEVERE, "Не удалось запустить FuryRaids. Плагин отключён.", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void enableRegions() {
        messages = new MessagesConfig(this);
        blocks = new BlockConfig(this);
        items = new ItemUtil(this);
        database = new Database(this);
        regionManager = new RegionManager(this);
        database.init();
        regionManager.loadAll();
        regionManager.syncWorldGuard();
        cores = new CoreManager(this);
        regionViews = new RegionViewManager(this);
        menuLoader = new MenuLoader(this);
        menuLoader.reload();
        regionGUI = new RegionGUI(this);
        memberInputs = new RegionMemberInputManager(this);

        registerListeners(
                new RegionBlockListener(this),
                new AccessListener(this),
                new ChestListener(this),
                new ExplosionListener(this),
                new MechanicsListener(this),
                new WorldSyncListener(this),
                new FlagProtectionListener(this),
                cores,
                regionViews,
                regionGUI,
                memberInputs
        );

        PSCommand command = new PSCommand(this);
        var ps = getCommand("ps");
        if (ps == null) throw new IllegalStateException("Команда ps отсутствует в plugin.yml");
        ps.setExecutor(command);
        ps.setTabCompleter(command);

        api = new APIImpl(this);
        FuryRaidsProvider.register(api);
        Bukkit.getServicesManager().register(FuryRaidsAPI.class, api, this, ServicePriority.Normal);
        Bukkit.getScheduler().runTaskTimer(this, regionManager::syncWorldGuard, 1200L, 1200L);
    }

    private void enableTnt() {
        tntMessages = new com.agent1k.tnt.config.MessagesConfig(this);
        tntRegistry = new TntRegistry();
        tntConfig = new TntConfig(this, tntRegistry);
        tntItems = new com.agent1k.tnt.util.ItemUtil(this);
        tntTracker = new TntBlockTracker(this);

        registerListeners(
                new InteractListener(this),
                new PlacementListener(this),
                new BreakListener(this),
                new PrimeListener(this),
                new com.agent1k.tnt.listener.ExplosionListener(this)
        );
        var tnt = getCommand("tnt");
        if (tnt == null) throw new IllegalStateException("Команда tnt отсутствует в plugin.yml");
        ETNTCommand command = new ETNTCommand(this);
        tnt.setExecutor(command);
        tnt.setTabCompleter(command);
    }

    @Override
    public void onDisable() {
        if (holograms != null) holograms.shutdown();
        if (memberInputs != null) memberInputs.shutdown(false);
        if (regionGUI != null) regionGUI.closeAll(Bukkit.getOnlinePlayers());
        if (regionViews != null) regionViews.shutdown();
        if (api != null) {
            Bukkit.getServicesManager().unregister(FuryRaidsAPI.class, api);
            FuryRaidsProvider.unregister();
        }
        if (database != null) database.shutdown();
        if (regionManager != null && api != null) worldGuardHook.flush();
        if (papiHook != null) papiHook.unregister();
        if (tntPapiHook != null) tntPapiHook.unregister();
        instance = null;
    }

    public void reloadAll() {
        if (memberInputs != null) memberInputs.shutdown(true);
        if (regionViews != null) regionViews.shutdown();
        reloadConfig();
        if (ViewSettings.installDefaults(getConfig(), "view")) saveConfig();
        if (messages != null) messages.reload();
        if (blocks != null) blocks.reload();
        if (tntMessages != null) tntMessages.reload();
        if (tntConfig != null) tntConfig.reload();
        if (menuLoader != null && menuLoader.reload() && regionGUI != null) {
            regionGUI.closeAll(Bukkit.getOnlinePlayers());
        }
        if (regionManager != null) {
            regionManager.invalidateLocationCache();
            regionManager.syncWorldGuard();
        }
        if (holograms != null) holograms.reload();
    }

    private void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) Bukkit.getPluginManager().registerEvents(listener, this);
    }

    @Override
    public FileConfiguration getConfig() { return getFiles().get("region/config.yml").getConfig(); }

    @Override
    public void reloadConfig() { getFiles().get("region/config.yml").load(); }

    @Override
    public void saveConfig() { getFiles().get("region/config.yml").save(); }

    @Override
    public void saveDefaultConfig() { getFiles().get("region/config.yml").getConfig(); }

    public FolderLoader getFiles() {
        if (files == null) files = new FolderLoader(this);
        return files;
    }

    @NotNull public static FuryRaids instance() { return instance; }
    @NotNull public MessagesConfig getMessages() { return messages; }
    @NotNull public BlockConfig getBlocks() { return blocks; }
    @NotNull public Database getDatabase() { return database; }
    @NotNull public RegionManager getRegionManager() { return regionManager; }
    @NotNull public ItemUtil getItems() { return items; }
    @NotNull public WorldGuardHook getWorldGuardHook() { return worldGuardHook; }
    @NotNull public FuryRaidsAPI getAPI() { return api; }
    @NotNull public RegionViewManager getRegionViews() { return regionViews; }
    @NotNull public MenuLoader getMenuLoader() { return menuLoader; }
    @NotNull public RegionGUI getRegionGUI() { return regionGUI; }
    @NotNull public RegionMemberInputManager getMemberInputs() { return memberInputs; }
    @Nullable public HologramManager getHolograms() { return holograms; }
    @Nullable public PAPIHook getPapiHook() { return papiHook; }
    @NotNull public com.agent1k.tnt.config.MessagesConfig getTntMessages() { return tntMessages; }
    @NotNull public TntConfig getTntConfig() { return tntConfig; }
    @NotNull public TntRegistry getTntRegistry() { return tntRegistry; }
    @NotNull public com.agent1k.tnt.util.ItemUtil getTntItems() { return tntItems; }
    @NotNull public TntBlockTracker getTntTracker() { return tntTracker; }
    @Nullable public CoreManager getCores() { return cores; }
}
