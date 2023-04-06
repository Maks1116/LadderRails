package pl.pixel.ladderrails;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

public class Listeners implements Listener {
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

                return;
            } else {
                Vector velocity = minecart.getVelocity();
                goingUp = velocity.getY() > 0;
            }

            step(minecart, goingUp);

            if (!toLadder) {
                move(minecart);
                minecart.setFallDistance(0);

                return;
            }

            if (!goingUp) {
                Block bellow = to.getRelative(0, -1, 0);
                if (bellow.getType() != Material.AIR && bellow.getType() != Material.LADDER) {
                    move(minecart);
                }
            }
        }
    }

    private void move(Minecart minecart) {
        float yaw = minecart.getLocation().getYaw();
        yaw = (yaw + 360) % 360;
        int direction = Math.round(yaw / 90) % 4;
        Vector velocity = minecart.getVelocity();

        switch (direction) {
            case 0:
                velocity.setX(0.5);
                break;
            case 1:
                velocity.setZ(-0.5);
                break;
            case 2:
                velocity.setX(-0.5);
                break;
            case 3:
                velocity.setZ(0.5);
                break;
        }

        minecart.setVelocity(velocity);
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
