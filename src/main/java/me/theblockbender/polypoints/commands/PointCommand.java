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
        Location found = new Location(world, 0, 0, 0);
        int attempt = -1;
        boolean criteriaMet = false;
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

        while (!polygonalRegion.contains(BukkitUtil.toVector(found)) && !criteriaMet && attempt < 100) {
            criteriaMet = false;
            attempt++;
            System.out.print("[" + attempt + "]------------");
            int x = ThreadLocalRandom.current().nextInt(minX, maxX + 1);
            int z = ThreadLocalRandom.current().nextInt(minZ, maxZ + 1);
            int y = world.getHighestBlockYAt(x, z);
            found = new Location(world, x, y, z);
            System.out.print("Y = " + y);
            Block block = found.getBlock();
            if (block.getType() == Material.AIR) {
                System.out.print("Material was air");
                while (block.getType() == Material.AIR && found.getBlockY() >= minY) {
                    System.out.print("Moving down 1 block.");
                    found.add(0, -1, 0);
                    y = found.getBlockY();
                    System.out.print("Y = " + y);
                    block = found.getBlock();

                }
            }
            System.out.print("x = " + x);
            System.out.print("y = " + y);
            System.out.print("z = " + z);
            if (y < minY || y > maxY) {
                System.out.print("Y is outside the range of the polygon. Max = " + maxY + ", Min = " + minY);
                continue;
            }
            found = new Location(world, x, y, z);
            if (!main.validFloorMaterials.contains(block.getType())) {
                System.out.print("This block is not on the list of valid floor materials. Type = " + WordUtils.capitalizeFully(block.getType().name().replace("_", " ")));
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
            criteriaMet = true;
        }
        if (attempt == 100) {
            sender.sendMessage("Unable to generate a random spawnable point in the region " + regionName + ".");
            return true;
        }
        if (!criteriaMet) {
            sender.sendMessage("Unable to generate a random spawnable point in the region " + regionName + ".");
            return true;
        }
        Location finalLocation = found.add(0, -1, 0);
        int finalX = finalLocation.getBlockX();
        int finalY = finalLocation.getBlockY();
        int finalZ = finalLocation.getBlockZ();
        TextComponent message = new TextComponent("Found spawnable point for " + regionName + " at x = " + finalX + " y = " + finalY + " z = " + finalZ + ".");
        message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/minecraft:tp " + player.getName() + " " + finalX + " " + finalY + " " + finalZ));
        player.spigot().sendMessage(message);
        return true;
    }
}
