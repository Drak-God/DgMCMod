package top.DrakGod.DgMCMod.Functions;

import org.bukkit.Location;
import org.bukkit.World;

public class Locations {
    public static Location Find_Empty_Location(Location Location) {
        World World = Location.getWorld();

        int x = (int) Math.round(Location.getX());
        int y = (int) Math.round(Location.getY());
        int z = (int) Math.round(Location.getZ());

        if (World.getBlockAt(x, y, z).isEmpty()) {
            return Location;
        }

        if (World.getBlockAt(x, y + 1, z).isEmpty()) {
            return new Location(World, x, y + 1, z);
        }

        if (World.getBlockAt(x, y - 1, z).isEmpty()) {
            return new Location(World, x, y - 1, z);
        }

        return Location;
    }
}