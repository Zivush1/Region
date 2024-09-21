package me.zivush.region;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RegionManager {

    private final DatabaseManager databaseManager;
    private final Map<String, Region> regions;
    private final Map<UUID, Location[]> selections;

    public RegionManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.regions = new HashMap<>();
        this.selections = new HashMap<>();
        loadRegions();
    }

    private void loadRegions() {
        List<Region> loadedRegions = databaseManager.loadRegions();
        for (Region region : loadedRegions) {
            regions.put(region.getName(), region);
        }
    }

    public boolean createRegion(String name, Location pos1, Location pos2, UUID owner) {
        if (regions.containsKey(name)) {
            return false;
        }
        Region region = new Region(name, pos1, pos2, owner);
        regions.put(name, region);
        databaseManager.saveRegion(region);
        return true;
    }

    public boolean addMember(String regionName, UUID playerUUID) {
        Region region = regions.get(regionName);
        if (region == null) {
            return false;
        }
        region.addMember(playerUUID);
        databaseManager.updateRegion(region);
        return true;
    }

    public boolean removeMember(String regionName, UUID playerUUID) {
        Region region = regions.get(regionName);
        if (region == null) {
            return false;
        }
        region.removeMember(playerUUID);
        databaseManager.updateRegion(region);
        return true;
    }

    public List<UUID> getWhitelist(String regionName) {
        Region region = regions.get(regionName);
        return region != null ? region.getMembers() : null;
    }

    public boolean setFlag(String regionName, String flagName, RegionFlag.State state) {
        Region region = regions.get(regionName);
        if (region == null) {
            return false;
        }
        region.setFlag(flagName, state);
        databaseManager.updateRegion(region);
        return true;
    }

    public Region getRegion(String name) {
        return regions.get(name);
    }

    public void setSelection(UUID playerUUID, Location pos1, Location pos2) {
        selections.put(playerUUID, new Location[]{pos1, pos2});
    }

    public Location[] getSelection(UUID playerUUID) {
        return selections.get(playerUUID);
    }

    public void clearSelection(UUID playerUUID) {
        selections.remove(playerUUID);
    }

    public boolean isInRegion(Location location) {
        for (Region region : regions.values()) {
            if (region.contains(location)) {
                return true;
            }
        }
        return false;
    }

    public Region getRegionAt(Location location) {
        for (Region region : regions.values()) {
            if (region.contains(location)) {
                return region;
            }
        }
        return null;
    }

    public boolean removeRegion(String name) {
        Region region = regions.remove(name);
        if (region != null) {
            databaseManager.deleteRegion(name);
            return true;
        }
        return false;
    }

    public boolean renameRegion(String oldName, String newName) {
        Region region = regions.get(oldName);
        if (region == null) {
            return false;
        }

        boolean success = databaseManager.renameRegion(oldName, newName);
        if (success) {
            regions.remove(oldName);
            region.setName(newName);
            regions.put(newName, region);
        }

        return success;
    }

    public Map<String, Region> getRegions() {
        return new HashMap<>(regions);
    }

    public void updateRegion(Region region) {
        regions.put(region.getName(), region);
        databaseManager.updateRegion(region);
    }
}
