package me.zivush.region;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainRegionGUI {
    private final RegionPlugin plugin;
    private final int REGIONS_PER_PAGE = 45;
    private int currentPage = 0;

    public MainRegionGUI(RegionPlugin plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player, int page) {
        currentPage = page;
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.BLUE + "Regions - Page " + (currentPage + 1));

        Map<String, Region> regions = plugin.getRegionManager().getRegions();
        List<Region> regionList = new ArrayList<>(regions.values());

        int startIndex = currentPage * REGIONS_PER_PAGE;
        int endIndex = Math.min(startIndex + REGIONS_PER_PAGE, regionList.size());

        for (int i = startIndex; i < endIndex; i++) {
            Region region = regionList.get(i);
            ItemStack item = createRegionItem(region);
            inv.addItem(item);
        }

        if (currentPage > 0) {
            inv.setItem(45, createNavigationItem(Material.ARROW, ChatColor.YELLOW + "Previous Page"));
        }

        if (endIndex < regionList.size()) {
            inv.setItem(53, createNavigationItem(Material.ARROW, ChatColor.YELLOW + "Next Page"));
        }

        player.openInventory(inv);
    }

    private ItemStack createRegionItem(Region region) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + region.getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Owner: " + Bukkit.getOfflinePlayer(region.getOwner()).getName());
        lore.add(ChatColor.GRAY + "Members: " + region.getMembers().size());
        lore.add(ChatColor.YELLOW + "Click to manage");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNavigationItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
