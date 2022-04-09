package revive.smp;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerEventHandler implements Listener {
    @EventHandler
    public void onHeadPlace(BlockPlaceEvent event) {
        if(event.getItemInHand().getType() == Material.PLAYER_HEAD) {
            ItemStack head = event.getItemInHand();
            Block block = event.getBlockPlaced();
            Location location = block.getLocation();

            SkullMeta meta = (SkullMeta) head.getItemMeta();

            try {
                //Respawn Player
                Player player = meta.getOwningPlayer().getPlayer();
                player.teleport(location);
                player.setGameMode(GameMode.SURVIVAL);

                //Remove Head
                location.getBlock().setType(Material.AIR);

                //Remove head from Inventory
                Player respawner = event.getPlayer();
                respawner.getInventory().setItem(event.getHand(), new ItemStack(Material.AIR, 1));

            } catch(NullPointerException e)  {
                event.setCancelled(true);
                Bukkit.getLogger().warning("Head does not contain valid player!");
                e.printStackTrace();
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
}
