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

public class FlagEditGUI {
    private final RegionPlugin plugin;
    private final Region region;

    public FlagEditGUI(RegionPlugin plugin, Region region) {
        this.plugin = plugin;
        this.region = region;
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.BLUE + "Edit Flags: " + region.getName());

        Map<String, RegionFlag> flags = region.getFlags();

        int slot = 0;
        for (Map.Entry<String, RegionFlag> entry : flags.entrySet()) {
            inv.setItem(slot, createFlagItem(entry.getKey(), entry.getValue()));
            slot++;
        }

        player.openInventory(inv);
    }

    private ItemStack createFlagItem(String flagName, RegionFlag flag) {
        Material material;
        switch (flag.getState()) {
            case NONE:
                material = Material.RED_WOOL;
                break;
            case WHITELIST:
                material = Material.YELLOW_WOOL;
                break;
            case EVERYONE:
                material = Material.GREEN_WOOL;
                break;
            default:
                material = Material.GRAY_WOOL;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + flagName);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Current state: " + flag.getState());
        lore.add(ChatColor.YELLOW + "Click to change");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}

