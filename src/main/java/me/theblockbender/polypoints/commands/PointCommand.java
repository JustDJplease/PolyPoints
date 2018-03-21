package me.theblockbender.polypoints.commands;

import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.theblockbender.polypoints.Main;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.WordUtils;
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
        ProtectedPolygonalRegion polygonalRegion = (ProtectedPolygonalRegion) region;
        Location found;
        int attempt = 0;
        System.out.print("[REGION STATISTICS]------------");
        int minX = polygonalRegion.getMinimumPoint().getBlockX();
        System.out.print("MinX = " + minX);
        int maxX = polygonalRegion.getMaximumPoint().getBlockX();
        System.out.print("MaxX = " + maxX);
        int minY = polygonalRegion.getMinimumPoint().getBlockY();
        System.out.print("MinY = " + minY);
        int maxY = polygonalRegion.getMaximumPoint().getBlockY();
        System.out.print("MaxY = " + maxY);
        int minZ = polygonalRegion.getMinimumPoint().getBlockZ();
        System.out.print("MinZ = " + minZ);
        int maxZ = polygonalRegion.getMaximumPoint().getBlockZ();
        System.out.print("MaxZ = " + maxZ);
        List<Location> locationList = new ArrayList<>();
        while (attempt < 100) {
            attempt++;
            System.out.print("[" + attempt + "/100]------------");
            int x = ThreadLocalRandom.current().nextInt(minX, maxX + 1);
            int z = ThreadLocalRandom.current().nextInt(minZ, maxZ + 1);
            int y = getHighestYAt(new Location(world, x, maxY, z), minY);
            if (y == -1) {
                System.out.print("Y is outside the range of the polygon. Max = " + maxY + ", Min = " + minY);
                continue;
            }
            System.out.print("x = " + x);
            System.out.print("y = " + y);
            System.out.print("z = " + z);
            found = new Location(world, x, y, z);
            Block block = found.getBlock();
            if (!main.validFloorMaterials.contains(block.getType())) {
                System.out.print("This block is not on the list of valid floor materials. Type = " + WordUtils.capitalizeFully(block.getType().name().replace("_", " ")));
                continue;
            }
            if (!polygonalRegion.contains(BukkitUtil.toVector(found))) {
                System.out.print("The point was not located inside the polygon.");
                continue;
            }
            Block airSpaceAbove = found.add(0, 1, 0).getBlock();
            if (airSpaceAbove.getType() != Material.AIR) {
                System.out.print("The airSpace ONE above this block is not Air. Type = " + WordUtils.capitalizeFully(airSpaceAbove.getType().name().replace("_", " ")));
                continue;
            }
            Block airSpaceTwoAbove = found.add(0, 1, 0).getBlock();
            if (airSpaceTwoAbove.getType() != Material.AIR) {
                System.out.print("The airSpace TWO above this block is not Air. Type = " + WordUtils.capitalizeFully(airSpaceTwoAbove.getType().name().replace("_", " ")));
                continue;
            }
            locationList.add(found.add(0, -1, 0));
        }
        main.preLoadedSpawnLocations.put(regionName, locationList);
        System.out.print("Point generating for region " + regionName + " has finished after " + attempt + " attempts.");
        System.out.print(locationList.size() + " points have been created for the region " + regionName);
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
