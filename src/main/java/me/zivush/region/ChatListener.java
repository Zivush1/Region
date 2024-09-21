package me.zivush.region;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatListener implements Listener {
    private final RegionPlugin plugin;
    private final Map<UUID, PendingAction> pendingActions;

    public ChatListener(RegionPlugin plugin) {
        this.plugin = plugin;
        this.pendingActions = new HashMap<>();
    }

    public void addPendingWhitelistAction(Player player, Region region, boolean isAdding) {
        pendingActions.put(player.getUniqueId(), new PendingAction(ActionType.WHITELIST, region, isAdding));
        player.sendMessage(ChatColor.YELLOW + "Please type the player name you want to " + (isAdding ? "add to" : "remove from") + " the whitelist.");
    }

    public void addPendingRenameAction(Player player, Region region) {
        pendingActions.put(player.getUniqueId(), new PendingAction(ActionType.RENAME, region, false));
        player.sendMessage(ChatColor.YELLOW + "Please type the new name for the region.");
    }

    public void addPendingRedefineAction(Player player, Region region) {
        pendingActions.put(player.getUniqueId(), new PendingAction(ActionType.REDEFINE, region, false));
        player.sendMessage(ChatColor.YELLOW + "Please type the new coordinates in the format: <x1>, <y1>, <z1>, <x2>, <y2>, <z2>, <world>");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PendingAction action = pendingActions.remove(player.getUniqueId());

        if (action != null) {
            event.setCancelled(true);
            Region region = action.getRegion();

            switch (action.getType()) {
                case WHITELIST:
                    handleWhitelistAction(player, region, event.getMessage(), action.isAdding());
                    break;
                case RENAME:
                    handleRenameAction(player, region, event.getMessage());
                    break;
                case REDEFINE:
                    handleRedefineAction(player, region, event.getMessage());
                    break;
            }
        }
    }

    private void handleWhitelistAction(Player player, Region region, String targetPlayerName, boolean isAdding) {
        UUID targetPlayerUUID = Bukkit.getOfflinePlayer(targetPlayerName).getUniqueId();

        if (isAdding) {
            region.addMember(targetPlayerUUID);
            player.sendMessage(ChatColor.GREEN + targetPlayerName + " has been added to the whitelist.");
        } else {
            region.removeMember(targetPlayerUUID);
            player.sendMessage(ChatColor.GREEN + targetPlayerName + " has been removed from the whitelist.");
        }

        plugin.getRegionManager().updateRegion(region);
    }

    private void handleRenameAction(Player player, Region region, String newName) {
        if (plugin.getRegionManager().getRegion(newName) != null) {
            player.sendMessage(ChatColor.RED + "A region with that name already exists.");
            return;
        }

        String oldName = region.getName();
        if (plugin.getRegionManager().renameRegion(oldName, newName)) {
            player.sendMessage(ChatColor.GREEN + "Region successfully renamed from " + oldName + " to " + newName);
        } else {
            player.sendMessage(ChatColor.RED + "Failed to rename the region. Please check the server logs and try again.");
        }
    }

    private void handleRedefineAction(Player player, Region region, String coordinates) {
        String[] parts = coordinates.split(", ");

        if (parts.length != 7) {
            player.sendMessage(ChatColor.RED + "Invalid format. Please use: <x1>, <y1>, <z1>, <x2>, <y2>, <z2>, <world>");
            return;
        }

        try {
            int x1 = Integer.parseInt(parts[0]);
            int y1 = Integer.parseInt(parts[1]);
            int z1 = Integer.parseInt(parts[2]);
            int x2 = Integer.parseInt(parts[3]);
            int y2 = Integer.parseInt(parts[4]);
            int z2 = Integer.parseInt(parts[5]);
            String world = parts[6];

            Location pos1 = new Location(Bukkit.getWorld(world), x1, y1, z1);
            Location pos2 = new Location(Bukkit.getWorld(world), x2, y2, z2);

            region.setPos1(pos1);
            region.setPos2(pos2);
            plugin.getRegionManager().updateRegion(region);
            player.sendMessage(ChatColor.GREEN + "Region " + region.getName() + " has been redefined.");
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid coordinates. Please use numbers for x, y, and z values.");
        }
    }

    private enum ActionType {
        WHITELIST, RENAME, REDEFINE
    }

    private static class PendingAction {
        private final ActionType type;
        private final Region region;
        private final boolean isAdding;

        public PendingAction(ActionType type, Region region, boolean isAdding) {
            this.type = type;
            this.region = region;
            this.isAdding = isAdding;
        }

        public ActionType getType() {
            return type;
        }

        public Region getRegion() {
            return region;
        }

        public boolean isAdding() {
            return isAdding;
        }
    }
}
