package me.zivush.region;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class GUIListener implements Listener {
    private final RegionPlugin plugin;

    public GUIListener(RegionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        ItemStack clickedItem = event.getCurrentItem();

        if (inventory == null || clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String inventoryTitle = event.getView().getTitle();

        if (inventoryTitle.startsWith(ChatColor.BLUE + "Regions - Page ")) {
            handleMainRegionGUI(player, clickedItem, inventory, inventoryTitle);
            event.setCancelled(true);
        } else if (inventoryTitle.startsWith(ChatColor.BLUE + "Manage: ")) {
            handleRegionManagementGUI(player, clickedItem, inventoryTitle);
            event.setCancelled(true);
        } else if (inventoryTitle.startsWith(ChatColor.BLUE + "Edit Flags: ")) {
            handleFlagEditGUI(player, clickedItem, inventoryTitle);
            event.setCancelled(true);
        } else if (inventoryTitle.startsWith(ChatColor.BLUE + "Whitelist: ")) {
            handleWhitelistGUI(player, clickedItem, inventory, inventoryTitle);
            event.setCancelled(true);
        }
    }

    private void handleMainRegionGUI(Player player, ItemStack clickedItem, Inventory inventory, String inventoryName) {
        if (clickedItem.getType() == Material.BOOK) {
            String regionName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            plugin.openRegionManagementGUI(player, plugin.getRegionManager().getRegion(regionName));
        } else if (clickedItem.getType() == Material.ARROW) {
            int currentPage = Integer.parseInt(inventoryName.split("Page ")[1]) - 1;
            if (clickedItem.getItemMeta().getDisplayName().contains("Previous")) {
                plugin.openMainRegionGUI(player, currentPage - 1);
            } else if (clickedItem.getItemMeta().getDisplayName().contains("Next")) {
                plugin.openMainRegionGUI(player, currentPage + 1);
            }
        }
    }

    private void handleRegionManagementGUI(Player player, ItemStack clickedItem, String inventoryName) {
        String regionName = ChatColor.stripColor(inventoryName.split("Manage: ")[1]);
        Region region = plugin.getRegionManager().getRegion(regionName);

        switch (clickedItem.getType()) {
            case NAME_TAG:
                player.closeInventory();
                plugin.getChatListener().addPendingRenameAction(player, region);
                break;
            case PLAYER_HEAD:
                plugin.openWhitelistGUI(player, region, 0);
                break;
            case STICK:
                player.closeInventory();
                plugin.getChatListener().addPendingRedefineAction(player, region);
                break;
            case REDSTONE:
                plugin.openFlagEditGUI(player, region);
                break;
        }
    }

    private void handleFlagEditGUI(Player player, ItemStack clickedItem, String inventoryTitle) {
        String regionName = ChatColor.stripColor(inventoryTitle.split("Edit Flags: ")[1]);
        Region region = plugin.getRegionManager().getRegion(regionName);
        String flagName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        RegionFlag flag = region.getFlag(flagName);
        if (flag != null) {
            RegionFlag.State newState = getNextState(flag.getState());
            region.setFlag(flagName, newState);
            plugin.getRegionManager().updateRegion(region);
            plugin.openFlagEditGUI(player, region);
        }
    }

    private void handleWhitelistGUI(Player player, ItemStack clickedItem, Inventory inventory, String inventoryName) {
        String[] titleParts = inventoryName.split(" - ");
        String regionName = ChatColor.stripColor(titleParts[0].split(": ")[1]);
        Region region = plugin.getRegionManager().getRegion(regionName);
        int currentPage = Integer.parseInt(titleParts[1].split("Page ")[1]) - 1;

        if (clickedItem.getType() == Material.PLAYER_HEAD || clickedItem.getType().name().equals("SKULL_ITEM")) {
            SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
            OfflinePlayer memberToRemove = meta.getOwningPlayer();
            region.removeMember(memberToRemove.getUniqueId());
            plugin.getRegionManager().updateRegion(region);
            plugin.openWhitelistGUI(player, region, currentPage);
        } else if (clickedItem.getType() == Material.EMERALD) {
            player.closeInventory();
            plugin.getChatListener().addPendingWhitelistAction(player, region, true);
        } else if (clickedItem.getType() == Material.ARROW) {
            if (clickedItem.getItemMeta().getDisplayName().contains("Previous")) {
                plugin.openWhitelistGUI(player, region, currentPage - 1);
            } else if (clickedItem.getItemMeta().getDisplayName().contains("Next")) {
                plugin.openWhitelistGUI(player, region, currentPage + 1);
            }
        }
    }

    private RegionFlag.State getNextState(RegionFlag.State currentState) {
        switch (currentState) {
            case NONE:
                return RegionFlag.State.WHITELIST;
            case WHITELIST:
                return RegionFlag.State.EVERYONE;
            case EVERYONE:
                return RegionFlag.State.NONE;
            default:
                return RegionFlag.State.NONE;
        }
    }
}
