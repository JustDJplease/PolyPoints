package me.theblockbender.polypoints.commands;

import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.theblockbender.polypoints.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class PointCommand implements CommandExecutor {

    private Main main;

    public PointCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Please execute this command as a player.");
            return true;
        }
        if (args.length != 1)
            return false;

        Player player = (Player) sender;
        World world = player.getWorld();
        String regionName = args[0];
        ProtectedRegion region = WGBukkit.getRegionManager(world).getRegion(regionName);

        if (region == null) {
            sender.sendMessage("The region " + regionName + " does not exist in your current world.");
            return false;
        }

        if (!(region instanceof ProtectedPolygonalRegion)) {
            sender.sendMessage("The region " + regionName + " it not a polygonal region.");
            return true;
        }

        ProtectedPolygonalRegion polygonalRegion = (ProtectedPolygonalRegion) region;
        if (polygonalRegion.volume() < 1) {
            sender.sendMessage("The region " + regionName + " has a negative volume.");
            return true;
        }
        Location found = new Location(world, 0, 0, 0);
        int attempt = -1;
        boolean criteriaMet;
        int minX = polygonalRegion.getMinimumPoint().getBlockX();
        int maxX = polygonalRegion.getMaximumPoint().getBlockX();
        int minY = polygonalRegion.getMinimumPoint().getBlockY();
        int maxY = polygonalRegion.getMaximumPoint().getBlockY();
        int minZ = polygonalRegion.getMinimumPoint().getBlockZ();
        int maxZ = polygonalRegion.getMaximumPoint().getBlockZ();

        do {
            criteriaMet = false;
            attempt++;
            int x = ThreadLocalRandom.current().nextInt(minX, maxX + 1);
            int z = ThreadLocalRandom.current().nextInt(minZ, maxZ + 1);
            int y = world.getHighestBlockYAt(x, z);
            if (y < minY || y > maxY)
                continue;
            found = new Location(world, x, y, z);
            Block block = found.getBlock();
            if (!main.validFloorMaterials.contains(block.getType()))
                continue;
            Block airSpaceAbove = found.getBlock();
            if (airSpaceAbove.getType() != Material.AIR)
                continue;
            Block airSpaceTwoAbove = found.add(0, 1, 0).getBlock();
            if (airSpaceTwoAbove.getType() != Material.AIR)
                continue;
            criteriaMet = true;
        } while (!polygonalRegion.contains(BukkitUtil.toVector(found)) && !criteriaMet && attempt < 100);
        if (attempt == 100) {
            sender.sendMessage("Unable to generate a random spawnable point in the region " + regionName + ".");
            return true;
        }
        if (!criteriaMet) {
            sender.sendMessage("Unable to generate a random spawnable point in the region " + regionName + ".");
            return true;
        }
        sender.sendMessage("Found spawnable point for " + regionName + " at x=" + found.getBlockX() + " y=" + found.getBlockY() + " z=" + found.getBlockZ() + ".");
        return true;
    }
}
