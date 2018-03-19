package me.theblockbender.polypoints.commands;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.internal.LocalWorldAdapter;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
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

import java.util.ArrayList;
import java.util.List;

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
            // Unsure when this would be the case.
            // Added it as a protective measure, to prevent unneccecary point generating.
            sender.sendMessage("The region " + regionName + " has a negative volume.");
            return true;
        }

        LocalSession session = WorldEdit.getInstance().getSession(player.getName());
        LocalWorld localWorld = LocalWorldAdapter.adapt(session.getSelectionWorld());
        Polygonal2DRegion weRegion = new Polygonal2DRegion(localWorld, polygonalRegion.getPoints(), polygonalRegion.getMinimumPoint().getBlockY(), polygonalRegion.getMaximumPoint().getBlockY());

        List<Block> possibleBlocks = new ArrayList<>();

        for (BlockVector block : weRegion) {
            Block bukkitBlock = BukkitUtil.toBlock(new BlockWorldVector(localWorld, block));
            Material type = bukkitBlock.getType();
            if (type == Material.AIR)
                continue;
            if (!main.validFloorMaterials.contains(type))
                continue;
            possibleBlocks.add(bukkitBlock);
        }
        if (possibleBlocks.isEmpty()) {
            sender.sendMessage("The region " + regionName + " has no valid spawn blocks.");
            return true;
        }
        int id = main.random.nextInt(possibleBlocks.size());
        Block random = possibleBlocks.get(id);
        Location location = random.getLocation().clone().add(0, 1, 0);
        sender.sendMessage("Picked random block at x=" + location.getBlockX() + " y=" + location.getBlockY() + " z=" + location.getBlockZ());
        return true;
    }
}
