package me.zivush.region;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Region {

    private String name;
    private Location pos1;
    private Location pos2;
    private UUID owner;
    private List<UUID> members;
    private Map<String, RegionFlag> flags;

    public Region(String name, Location pos1, Location pos2, UUID owner) {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.owner = owner;
        this.members = new ArrayList<>();
        this.flags = new HashMap<>();
        initDefaultFlags();
    }

    private void initDefaultFlags() {
        flags.put("BLOCK_BREAK", new RegionFlag("BLOCK_BREAK", RegionFlag.State.NONE));
        flags.put("BLOCK_PLACE", new RegionFlag("BLOCK_PLACE", RegionFlag.State.NONE));
        flags.put("INTERACT", new RegionFlag("INTERACT", RegionFlag.State.NONE));
        flags.put("ENTITY_DAMAGE", new RegionFlag("ENTITY_DAMAGE", RegionFlag.State.NONE));
    }

    public String getName() {
        return name;
    }

    public Location getPos1() {
        return pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public UUID getOwner() {
        return owner;
    }

    public List<UUID> getMembers() {
        return new ArrayList<>(members);
    }

    public void addMember(UUID playerUUID) {
        if (!members.contains(playerUUID)) {
            members.add(playerUUID);
        }
    }

    public void removeMember(UUID playerUUID) {
        members.remove(playerUUID);
    }

    public boolean isMember(UUID playerUUID) {
        return members.contains(playerUUID);
    }

    public void setFlag(String flagName, RegionFlag.State state) {
        flags.put(flagName, new RegionFlag(flagName, state));
    }

    public RegionFlag getFlag(String flagName) {
        return flags.get(flagName);
    }

    public Map<String, RegionFlag> getFlags() {
        return new HashMap<>(flags);
    }

    public boolean contains(Location location) {
        double minX = Math.min(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        return location.getX() >= minX && location.getX() <= maxX &&
                location.getY() >= minY && location.getY() <= maxY &&
                location.getZ() >= minZ && location.getZ() <= maxZ;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }
}
