package pl.pixel.ladderrails;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Listeners implements Listener {
    private List<UUID> invincibleEntities = new ArrayList<>();

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (event.getVehicle() instanceof Minecart) {
            Minecart minecart = (Minecart) event.getVehicle();
            Block from = event.getFrom().getBlock();
            Block to = event.getTo().getBlock();

            boolean fromLadder = from.getType() == Material.LADDER;
            boolean toLadder = to.getType() == Material.LADDER;
            boolean goingUp;

            if (!fromLadder && !toLadder) return;
            if (!fromLadder && toLadder) {
                Block bellow = to.getRelative(0, -1, 0);
                goingUp = bellow.getType() != Material.LADDER;

                if (goingUp) {
                    minecart.setVelocity(new Vector(0, 0.2, 0));
                } else {
                    minecart.setVelocity(new Vector(0, -0.2, 0));
                }

                if (minecart.getPassenger() != null) {
                    invincibleEntities.add(minecart.getPassenger().getUniqueId());
                }

                return;
            } else {
                Vector velocity = minecart.getVelocity();
                goingUp = velocity.getY() > 0;
            }

            step(minecart, goingUp);

            if (!toLadder) {
                move(minecart, goingUp);
                minecart.setFallDistance(0);
                if (minecart.getPassenger() != null) {
                    minecart.getPassenger().setFallDistance(0);
                }

                if (minecart.getPassenger() != null) {
                    invincibleEntities.remove(minecart.getPassenger().getUniqueId());
                }

                return;
            }

            if (!goingUp) {
                Block bellow = to.getRelative(0, -1, 0);
                if (bellow.getType() != Material.AIR && bellow.getType() != Material.LADDER) {
                    move(minecart, false);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (invincibleEntities.contains(event.getEntity().getUniqueId())) {
                event.setCancelled(true);
                event.getEntity().setFallDistance(0);
            }
        }
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (event.getVehicle() instanceof Minecart) {
            Minecart minecart = (Minecart) event.getVehicle();
            if (minecart.getPassenger() != null) {
                invincibleEntities.remove(minecart.getPassenger().getUniqueId());
            }
        }
    }

    private void move(Minecart minecart, boolean up) {
        //search for rails arround minecart on x and z axis
        Block block = minecart.getLocation().getBlock();
        Block rails = null;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                Block b = block.getRelative(x, 0, z);
                if (b.getType() == Material.RAILS || b.getType() == Material.POWERED_RAIL || b.getType() == Material.DETECTOR_RAIL) {
                    rails = b;
                    break;
                }

                if (up) {
                    b = block.getRelative(x, 1, z);
                    if (b.getType() == Material.RAILS || b.getType() == Material.POWERED_RAIL || b.getType() == Material.DETECTOR_RAIL) {
                        rails = b;
                        break;
                    }
                }
            }
        }

        if (rails != null) {
            if (minecart.getPassenger() != null) {
                Entity passenger = minecart.getPassenger();

                float pitch = passenger.getLocation().getPitch();
                float yaw = passenger.getLocation().getYaw();


                minecart.remove();
                Minecart newMinecart = (Minecart) passenger.getWorld().spawnEntity(rails.getLocation().add(0.5, 0.5, 0.5), EntityType.MINECART);

                newMinecart.setVelocity(minecart.getVelocity());
                Location loc = newMinecart.getLocation();
                loc.setPitch(pitch);
                loc.setYaw(yaw);
                passenger.teleport(loc);
                newMinecart.setPassenger(passenger);

                return;
            }
            minecart.teleport(rails.getLocation().add(0.5, 0.5, 0.5));

            Vector direction = rails.getLocation().toVector().subtract(block.getLocation().toVector());
            minecart.setVelocity(direction);
        }
    }

    private void step(Minecart minecart, boolean up) {
        double speed = Main.instance.getConfig().getDouble("minecart-ladder-speed", 0.2);
        if (up) {
            minecart.setVelocity(new Vector(0, speed, 0));
        } else {
            minecart.setVelocity(new Vector(0, -speed, 0));
        }
    }
}
