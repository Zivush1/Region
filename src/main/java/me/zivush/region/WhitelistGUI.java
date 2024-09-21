package me.zivush.region;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WhitelistGUI {
    private final RegionPlugin plugin;
    private final Region region;
    private final int MEMBERS_PER_PAGE = 45;
    private int currentPage = 0;

    public WhitelistGUI(RegionPlugin plugin, Region region) {
        this.plugin = plugin;
        this.region = region;
    }

    public void openGUI(Player player, int page) {
        currentPage = page;
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.BLUE + "Whitelist: " + region.getName() + " - Page " + (currentPage + 1));

        List<UUID> members = new ArrayList<>(region.getMembers());
        int startIndex = currentPage * MEMBERS_PER_PAGE;
        int endIndex = Math.min(startIndex + MEMBERS_PER_PAGE, members.size());

        for (int i = startIndex; i < endIndex; i++) {
            UUID memberUUID = members.get(i);
            ItemStack item = createMemberItem(memberUUID);
            inv.addItem(item);
        }

        if (currentPage > 0) {
            inv.setItem(45, createNavigationItem(Material.ARROW, ChatColor.YELLOW + "Previous Page"));
        }

        if (endIndex < members.size()) {
            inv.setItem(53, createNavigationItem(Material.ARROW, ChatColor.YELLOW + "Next Page"));
        }

        inv.setItem(49, createAddMemberItem());

        player.openInventory(inv);
    }

    private ItemStack createMemberItem(UUID memberUUID) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberUUID);
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(offlinePlayer);
        meta.setDisplayName(ChatColor.YELLOW + offlinePlayer.getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to remove from whitelist");
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

    private ItemStack createAddMemberItem() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Add Member");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to add a new member");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}