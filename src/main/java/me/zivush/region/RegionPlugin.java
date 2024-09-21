package me.zivush.region;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RegionPlugin extends JavaPlugin {

    private static RegionPlugin instance;
    private DatabaseManager databaseManager;
    private RegionManager regionManager;
    private MainRegionGUI mainRegionGUI;
    private ChatListener chatListener;
    private GUIListener guiListener;


    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        FileConfiguration config = getConfig();
        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        String database = config.getString("database.name");
        String username = config.getString("database.username");
        String password = config.getString("database.password");

        databaseManager = new DatabaseManager(host, port, database, username, password);
        databaseManager.connect();

        regionManager = new RegionManager(databaseManager);
        mainRegionGUI = new MainRegionGUI(this);
        chatListener = new ChatListener(this);
        guiListener = new GUIListener(this);


        RegionAPI.init(this);

        getCommand("region").setExecutor(new RegionCommand(this));

        getServer().getPluginManager().registerEvents(new RegionListener(this), this);
        getServer().getPluginManager().registerEvents(chatListener, this);
        getServer().getPluginManager().registerEvents(guiListener, this);


        getLogger().info("Region plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("Region plugin has been disabled!");
    }

    public static RegionPlugin getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public RegionManager getRegionManager() {
        return regionManager;
    }
    public void openMainRegionGUI(Player player, int page) {
        mainRegionGUI.openGUI(player, page);
    }

    public void openRegionManagementGUI(Player player, Region region) {
        new RegionManagementGUI(this, region).openGUI(player);
    }

    public void openFlagEditGUI(Player player, Region region) {
        new FlagEditGUI(this, region).openGUI(player);
    }

    public ChatListener getChatListener() {
        return chatListener;
    }

    public void openWhitelistGUI(Player player, Region region, int page) {
        new WhitelistGUI(this, region).openGUI(player, page);
    }

}