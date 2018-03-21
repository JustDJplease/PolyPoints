package me.theblockbender.polypoints.commands;

import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.theblockbender.polypoints.Main;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
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

        List<Location> locations = main.preLoadedSpawnLocations.get(regionName);
        sender.sendMessage("Found " + locations.size() + " locations to spawn an entity at.");
        for (Location location : locations) {
            TextComponent message = new TextComponent("--> " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());
            message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/minecraft:tp " + player.getName() + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ()));
            player.spigot().sendMessage(message);
        }
        return true;
    }

    public void populateRegion(World world, String regionName, ProtectedRegion region) {
        long timeAtStart = System.currentTimeMillis();
        ProtectedPolygonalRegion polygonalRegion = (ProtectedPolygonalRegion) region;
        Location found;
        int attempt = 0;
        int minX = polygonalRegion.getMinimumPoint().getBlockX();
        int maxX = polygonalRegion.getMaximumPoint().getBlockX();
        int minY = polygonalRegion.getMinimumPoint().getBlockY();
        int maxY = polygonalRegion.getMaximumPoint().getBlockY();
        int minZ = polygonalRegion.getMinimumPoint().getBlockZ();
        int maxZ = polygonalRegion.getMaximumPoint().getBlockZ();
        List<Location> locationList = new ArrayList<>();
        while (attempt < 100) {
            attempt++;
            int x = ThreadLocalRandom.current().nextInt(minX, maxX + 1);
            int z = ThreadLocalRandom.current().nextInt(minZ, maxZ + 1);
            int y = getHighestYAt(new Location(world, x, maxY, z), minY);
            if (y == -1) {
                continue;
            }
            found = new Location(world, x, y, z);
            Block block = found.getBlock();
            if (!main.validFloorMaterials.contains(block.getType()))
                continue;

            if (!polygonalRegion.contains(BukkitUtil.toVector(found)))
                continue;

            Block airSpaceAbove = found.add(0, 1, 0).getBlock();
            if (airSpaceAbove.getType() != Material.AIR)
                continue;

            Block airSpaceTwoAbove = found.add(0, 1, 0).getBlock();
            if (airSpaceTwoAbove.getType() != Material.AIR)
                continue;

            locationList.add(found.add(0, -1, 0));
        }
        main.preLoadedSpawnLocations.put(regionName, locationList);
        long duration = System.currentTimeMillis() - timeAtStart;
        System.out.print("Generated " + locationList.size() + " points for the region " + regionName + ". (Took " + duration + " ms.)");
    }

    private Integer getHighestYAt(Location locationIncludingYmax, int minY) {
        int y = locationIncludingYmax.getBlockY();
        while (y >= minY) {
            locationIncludingYmax.setY(y);
            Block block = locationIncludingYmax.getBlock();
            if (block.getType().isSolid())
                return y;
            y--;
        }
        return -1;
    }
}
