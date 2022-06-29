package luxielx;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Lis implements Listener {


    public static ArrayList<Player> getNearbyPlayerAsync(Location player, double radius) {
        World world = player.getWorld();
        ArrayList<Player> list = new ArrayList<>(Bukkit.getServer().getOnlinePlayers());
        for (Player online : Bukkit.getServer().getOnlinePlayers()) {
            if (online.getWorld().getName().equalsIgnoreCase(world.getName())) {
                if (online.getLocation().distance(player) > radius) {
                    list.remove(online);
                }
            }
        }
        return list;
    }

    public static void sendBlockChange(Location l, BlockData data, int radius) {
        getNearbyPlayerAsync(l, radius).stream().forEach(player -> player.sendBlockChange(l, data));
    }

    @EventHandler
    public void e(BlockPlaceEvent e) {
        if (e.getBlockAgainst().getType().hasGravity()) {
            e.getBlock().setMetadata("placedbyhand", new FixedMetadataValue(Main.main, 1));
        }
        if (e.getBlock().getLocation().subtract(0, 1, 0).getBlock().getType().hasGravity()) {
            Block b = e.getBlock().getLocation().subtract(0, 1, 0).getBlock();
            Bukkit.getScheduler().runTaskLaterAsynchronously(Main.main, () -> {
                sendBlockChange(b.getLocation(), b.getBlockData(), 10);
            }, 5);
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent evt) {
        Entity ent = evt.getEntity();
        if (ent instanceof ArmorStand || ent instanceof FallingBlock) {
            if (ent.hasGravity()) {
                if (ent instanceof ArmorStand) {
                    if (ent.isOnGround()) return;
                    ent.setGravity(true);
                    Bukkit.getScheduler().runTaskLater(Main.main, () -> {
                        Location loc = ent.getLocation().subtract(0, 0.5, 0);
                        int count = 0;
                        while (loc.getBlock().getType() == Material.AIR) {
                            if (count >= 100) return;
                            loc.subtract(0, 0.5, 0);
                            count++;
                        }
                        loc.add(0, 0.5, 0);
                        ent.teleport(loc);
                    }, 5);
                }


            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onSandFall(EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.FALLING_BLOCK && event.getTo() != Material.AIR) {
            FallingBlock flb = (FallingBlock) event.getEntity();
            if (!flb.getDropItem()) {
                event.setCancelled(true);
                flb.remove();
            }
        }
        if (event.getEntityType() == EntityType.FALLING_BLOCK && event.getTo() == Material.AIR) {
            Block b = event.getBlock();
            boolean tnt = false;
            boolean cancel;
            if (b.getBlockPower() > 0) {
                if (b.getRelative(BlockFace.UP).getType().toString().contains("PRESSURE")) {
                    event.getBlock().getState().update(true, true);
                    cancel = false;
                    tnt = true;
                } else {
                    cancel = true;

                }
            } else {
                cancel = true;

            }
            if (!b.getMetadata("placedbyhand").isEmpty() || tnt) {

                cancel = false;
            }
//            getNearbyBlocks(event.getBlock().getLocation(),1).stream().forEach(bb -> {
//               bb.getState().update(false, false);
//            });
//            sendBlockChange(event.getBlock().getLocation(), event.getBlockData(), 50);
//            event.getBlock().getState().update(false, false);


            event.setCancelled(true);
            if (!cancel) {
                if (b.getRelative(BlockFace.DOWN).getType() == Material.AIR) {
                    boolean finalTnt = tnt;
                    Bukkit.getScheduler().runTaskLater(Main.main, () -> {

                        FallingBlock fb = b.getWorld().spawnFallingBlock(event.getEntity().getLocation(), b.getBlockData());
                        fb.setGravity(true);
                        if (finalTnt) fb.setDropItem(false);
                        fb.setVelocity(new Vector(0, -2, 0));
                        b.setType(Material.AIR);
                        event.getEntity().remove();

                    }, Main.config.getInt("spleeffalldelay"));
                }
            } else {
                event.getBlock().getState().update(false, false);
                Bukkit.getScheduler().runTaskLaterAsynchronously(Main.main, () -> {
                    sendBlockChange(b.getLocation(), b.getBlockData(), 10);
                }, 5);

            }


        }
    }

    public List<Block> getNearbyBlocks(Location location, int radius) {
        List<Block> blocks = new ArrayList<Block>();
        for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
            for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
                    blocks.add(location.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }
}
