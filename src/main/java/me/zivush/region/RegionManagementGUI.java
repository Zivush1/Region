package me.zivush.region;

import me.zivush.region.RegionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RegionManagementGUI {
    private final RegionPlugin plugin;
    private final Region region;

    public RegionManagementGUI(RegionPlugin plugin, Region region) {
        this.plugin = plugin;
        this.region = region;
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.BLUE + "Manage: " + region.getName());

        inv.setItem(10, createGuiItem(Material.NAME_TAG, ChatColor.YELLOW + "Rename Region"));
        inv.setItem(12, createGuiItem(Material.PLAYER_HEAD, ChatColor.YELLOW + "Manage Whitelist"));
        inv.setItem(14, createGuiItem(Material.STICK, ChatColor.YELLOW + "Redefine Location"));
        inv.setItem(16, createGuiItem(Material.REDSTONE, ChatColor.YELLOW + "Edit Flags"));

        player.openInventory(inv);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(ChatColor.GRAY + line);
        }
        meta.setLore(loreList);

        item.setItemMeta(meta);
        return item;
    }
}
