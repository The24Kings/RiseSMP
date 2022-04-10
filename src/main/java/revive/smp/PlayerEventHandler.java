package revive.smp;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class PlayerEventHandler implements Listener {
    List<Location> chests = new ArrayList<>();

    @EventHandler
    public void onHeadPlace(BlockPlaceEvent event) {
        if(event.getItemInHand().getType() == Material.PLAYER_HEAD) {
            ItemStack head = event.getItemInHand();
            Block block = event.getBlockPlaced();
            World world = block.getWorld();
            Location headLocation = block.getLocation();
            Location location = block.getLocation();
            location.add(0.5,0.0,0.5); //Sets to middle of block

            SkullMeta meta = (SkullMeta) head.getItemMeta();

            try {
                location.add(0.0,3.0,0.0);
                //Respawn Player
                Player player = meta.getOwningPlayer().getPlayer();
                world.strikeLightning(location);
                player.teleport(headLocation);
                for (Player players : getServer().getOnlinePlayers()) {
                    players.playSound(headLocation, Sound.ITEM_TRIDENT_THUNDER, 10, 0.3f);
                }
                player.setGameMode(GameMode.SURVIVAL);

                //Remove Head
                headLocation.getBlock().setType(Material.AIR);

                //Remove head from Inventory
                Player respawner = event.getPlayer();
                respawner.getInventory().setItem(event.getHand(), new ItemStack(Material.AIR, 1));

            } catch(NullPointerException e)  {
                event.setCancelled(true);
                Bukkit.getLogger().warning("Cannot find player!");
            }
        }
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Player attacker = player.getKiller();

        if(attacker != null) {
            //Sets dead player into spectator
            player.setGameMode(GameMode.SPECTATOR);

            //Creates player skull from dead player
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();

            meta.setOwningPlayer(player);

            meta.displayName(Component.text( ChatColor.YELLOW + player.getName() + "'s Head"));
            skull.setItemMeta(meta);

            //Give attacker item unless inv. full
            if(player.getInventory().firstEmpty() == -1) {
                Location location = attacker.getLocation();
                World world = attacker.getWorld();

                world.dropItemNaturally(location, skull);
            } else {
                attacker.getInventory().addItem(skull);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(event.getEntity().getKiller() == null) {
            //Place chest at player's death location
            Player player = event.getPlayer();
            Block block = event.getPlayer().getLocation().getBlock();
            block.setType(Material.CHEST);
            Chest chest = (Chest) block.getState();
            Inventory inventory = chest.getInventory();

            //Sets dead player into spectator
            player.setGameMode(GameMode.SPECTATOR);

            //Adds chest Location to list of active chests
            chests.add(block.getLocation());

            //Creates player skull from dead player
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();

            meta.setOwningPlayer(player);

            meta.displayName(Component.text(ChatColor.YELLOW + player.getName() + "'s Head"));
            skull.setItemMeta(meta);

            //Add skull to chest
            inventory.addItem(skull);
        }
    }

    @EventHandler
    public void onChestOpen(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        Location location = inv.getLocation();

        if(inv.isEmpty() && chests.contains(location)) {
            try {
                chests.remove(location);
                event.getInventory().getLocation().getBlock().setType(Material.AIR);

            } catch(NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}
