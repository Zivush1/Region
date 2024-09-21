package me.zivush.region;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class RegionListener implements Listener {

    private final RegionPlugin plugin;
    private final RegionManager regionManager;

    public RegionListener(RegionPlugin plugin) {
        this.plugin = plugin;
        this.regionManager = plugin.getRegionManager();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() == Material.STICK &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Region Wand")) {

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                Location loc = event.getClickedBlock().getLocation();
                regionManager.setSelection(player.getUniqueId(), loc, null);
                player.sendMessage(ChatColor.GREEN + "First position set to " + formatLocation(loc));
                event.setCancelled(true);
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Location loc = event.getClickedBlock().getLocation();
                Location[] selection = regionManager.getSelection(player.getUniqueId());
                if (selection != null && selection[0] != null) {
                    regionManager.setSelection(player.getUniqueId(), selection[0], loc);
                    player.sendMessage(ChatColor.GREEN + "Second position set to " + formatLocation(loc));
                } else {
                    player.sendMessage(ChatColor.RED + "Please set the first position first.");
                }
                event.setCancelled(true);
            }
        }

        if (event.getClickedBlock() != null) {
            Region region = regionManager.getRegionAt(event.getClickedBlock().getLocation());
            if (region != null) {
                RegionFlag flag = region.getFlag("INTERACT");
                if (!canPerformAction(player, region, flag)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You don't have permission to interact in this region.");
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Region region = regionManager.getRegionAt(event.getBlock().getLocation());
        if (region != null) {
            RegionFlag flag = region.getFlag("BLOCK_BREAK");
            if (!canPerformAction(player, region, flag)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to break blocks in this region.");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Region region = regionManager.getRegionAt(event.getBlock().getLocation());
        if (region != null) {
            RegionFlag flag = region.getFlag("BLOCK_PLACE");
            if (!canPerformAction(player, region, flag)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to place blocks in this region.");
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            Region region = regionManager.getRegionAt(event.getEntity().getLocation());
            if (region != null) {
                RegionFlag flag = region.getFlag("ENTITY_DAMAGE");
                if (!canPerformAction(player, region, flag)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You don't have permission to damage entities in this region.");
                }
            }
        }
    }

    private boolean canPerformAction(Player player, Region region, RegionFlag flag) {
        if (player.hasPermission("region.bypass")) {
            return true;
        }

        switch (flag.getState()) {
            case NONE:
                return false;
            case WHITELIST:
                return region.isMember(player.getUniqueId());
            case EVERYONE:
                return true;
            default:
                return false;
        }
    }

    private String formatLocation(Location loc) {
        return String.format("(%d, %d, %d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
