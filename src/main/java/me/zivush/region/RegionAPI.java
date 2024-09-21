package me.zivush.region;

import org.bukkit.Location;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RegionAPI {

    private static RegionPlugin plugin;
    private static RegionManager regionManager;

    public static void init(RegionPlugin plugin) {
        RegionAPI.plugin = plugin;
        RegionAPI.regionManager = plugin.getRegionManager();
    }

    public static void registerFlag(String flagName) {
        Map<String, Region> regions = regionManager.getRegions();
        for (Region region : regions.values()) {
            if (region.getFlag(flagName) == null) {
                region.setFlag(flagName, RegionFlag.State.NONE);
            }
        }
        plugin.getDatabaseManager().addFlagColumn(flagName);
    }

    public static boolean setFlag(String regionName, String flagName, RegionFlag.State state) {
        return regionManager.setFlag(regionName, flagName, state);
    }

    public static RegionFlag getFlag(String regionName, String flagName) {
        Region region = regionManager.getRegion(regionName);
        return region != null ? region.getFlag(flagName) : null;
    }

    public static boolean createRegion(String name, Location pos1, Location pos2, UUID owner) {
        return regionManager.createRegion(name, pos1, pos2, owner);
    }

    public static boolean deleteRegion(String name) {
        return regionManager.removeRegion(name);
    }

    public static boolean addMember(String regionName, UUID playerUUID) {
        return regionManager.addMember(regionName, playerUUID);
    }

    public static boolean removeMember(String regionName, UUID playerUUID) {
        return regionManager.removeMember(regionName, playerUUID);
    }

    public static List<UUID> getWhitelist(String regionName) {
        return regionManager.getWhitelist(regionName);
    }

    public static Region getRegionAt(Location location) {
        return regionManager.getRegionAt(location);
    }

    public static boolean isInRegion(Location location) {
        return regionManager.isInRegion(location);
    }
}

