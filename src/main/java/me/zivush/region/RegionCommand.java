package me.zivush.region;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RegionCommand implements CommandExecutor, TabCompleter {

    private final RegionPlugin plugin;
    private final RegionManager regionManager;

    public RegionCommand(RegionPlugin plugin) {
        this.plugin = plugin;
        this.regionManager = plugin.getRegionManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            openRegionMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                handleCreateCommand(player, args);
                break;
            case "wand":
                handleWandCommand(player);
                break;
            case "add":
                handleAddCommand(player, args);
                break;
            case "remove":
                handleRemoveCommand(player, args);
                break;
            case "whitelist":
                handleWhitelistCommand(player, args);
                break;
            case "flag":
                handleFlagCommand(player, args);
                break;
            default:
                openRegionMenu(player, args[0]);
                break;
        }

        return true;
    }

    private void handleCreateCommand(Player player, String[] args) {
        if (!player.hasPermission("region.create")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to create regions.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /region create <name>");
            return;
        }

        String regionName = args[1];
        UUID playerUUID = player.getUniqueId();

        if (regionManager.getRegion(regionName) != null) {
            player.sendMessage(ChatColor.RED + "A region with that name already exists.");
            return;
        }

        if (regionManager.getSelection(playerUUID) == null) {
            player.sendMessage(ChatColor.RED + "Please select two points using the region wand first.");
            return;
        }

        if (regionManager.createRegion(regionName, regionManager.getSelection(playerUUID)[0], regionManager.getSelection(playerUUID)[1], playerUUID)) {
            player.sendMessage(ChatColor.GREEN + "Region " + regionName + " created successfully.");
            regionManager.clearSelection(playerUUID);
        } else {
            player.sendMessage(ChatColor.RED + "Failed to create region. Please try again.");
        }
    }

    private void handleWandCommand(Player player) {
        if (!player.hasPermission("region.wand")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use the region wand.");
            return;
        }

        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Region Wand");
        wand.setItemMeta(meta);
        player.getInventory().addItem(wand);
        player.sendMessage(ChatColor.GREEN + "You have been given the region wand.");
    }

    private void handleAddCommand(Player player, String[] args) {
        if (!player.hasPermission("region.add")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to add members to regions.");
            return;
        }

        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /region add <region> <player>");
            return;
        }

        String regionName = args[1];
        String targetPlayerName = args[2];
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        if (regionManager.addMember(regionName, targetPlayer.getUniqueId())) {
            player.sendMessage(ChatColor.GREEN + targetPlayerName + " has been added to region " + regionName + ".");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to add player to region. Please check if the region exists.");
        }
    }

    private void handleRemoveCommand(Player player, String[] args) {
        if (!player.hasPermission("region.remove")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to remove members from regions.");
            return;
        }

        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /region remove <region> <player>");
            return;
        }

        String regionName = args[1];
        String targetPlayerName = args[2];
        UUID targetPlayerUUID = Bukkit.getOfflinePlayer(targetPlayerName).getUniqueId();

        if (regionManager.removeMember(regionName, targetPlayerUUID)) {
            player.sendMessage(ChatColor.GREEN + targetPlayerName + " has been removed from region " + regionName + ".");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to remove player from region. Please check if the region exists.");
        }
    }

    private void handleWhitelistCommand(Player player, String[] args) {
        if (!player.hasPermission("region.whitelist")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to view region whitelists.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /region whitelist <region>");
            return;
        }

        String regionName = args[1];
        List<UUID> whitelist = regionManager.getWhitelist(regionName);

        if (whitelist == null) {
            player.sendMessage(ChatColor.RED + "Region not found.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "Whitelist for region " + regionName + ":");
        for (UUID uuid : whitelist) {
            player.sendMessage(ChatColor.YELLOW + "- " + Bukkit.getOfflinePlayer(uuid).getName());
        }
    }

    private void handleFlagCommand(Player player, String[] args) {
        if (!player.hasPermission("region.flag")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to edit region flags.");
            return;
        }

        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Usage: /region flag <region> <flag> <state>");
            return;
        }

        String regionName = args[1];
        String flagName = args[2].toUpperCase();
        String stateStr = args[3].toUpperCase();

        RegionFlag.State state;
        try {
            state = RegionFlag.State.valueOf(stateStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid state. Use NONE, WHITELIST, or EVERYONE.");
            return;
        }

        if (regionManager.setFlag(regionName, flagName, state)) {
            player.sendMessage(ChatColor.GREEN + "Flag " + flagName + " for region " + regionName + " set to " + state + ".");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to set flag. Please check if the region exists.");
        }
    }

    private void openRegionMenu(Player player) {
        plugin.openMainRegionGUI(player, 0);
    }

    private void openRegionMenu(Player player, String regionName) {
        Region region = plugin.getRegionManager().getRegion(regionName);
        if (region != null) {
            plugin.openRegionManagementGUI(player, region);
        } else {
            player.sendMessage(ChatColor.RED + "Region not found.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "wand", "add", "remove", "whitelist", "flag"));
            completions.addAll(regionManager.getRegions().keySet());
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "create":
                case "add":
                case "remove":
                case "whitelist":
                case "flag":
                    completions.addAll(regionManager.getRegions().keySet());
                    break;
            }
        } else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "add":
                case "remove":
                    completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
                    break;
                case "flag":
                    Region region = regionManager.getRegion(args[1]);
                    if (region != null) {
                        completions.addAll(region.getFlags().keySet());
                    }
                    if (completions.isEmpty()) {
                        completions.addAll(Arrays.asList("BLOCK_BREAK", "BLOCK_PLACE", "INTERACT", "ENTITY_DAMAGE"));
                    }
                    break;
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("flag")) {
            completions.addAll(Arrays.asList("NONE", "WHITELIST", "EVERYONE"));
        }

        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
