package me.theblockbender.polypoints;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.theblockbender.polypoints.commands.PointCommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Main extends JavaPlugin {

    public List<Material> validFloorMaterials = new ArrayList<>();
    public HashMap<String, List<Location>> preLoadedSpawnLocations = new HashMap<>();
    private PointCommand pointCommand = new PointCommand(this);

    public void onEnable() {
        populateFloor();
        for (World world : Bukkit.getWorlds()) {
            for (Map.Entry<String, ProtectedRegion> entry : WGBukkit.getRegionManager(world).getRegions().entrySet()) {
                ProtectedRegion region = entry.getValue();
                if (!(region instanceof ProtectedPolygonalRegion)) {
                    continue;
                }
                pointCommand.populateRegion(world, entry.getKey(), region);
            }
        }
        getCommand("polypoint").setExecutor(pointCommand);
    }

    private void populateFloor() {
        validFloorMaterials.add(Material.STONE);
        validFloorMaterials.add(Material.GRASS);
        validFloorMaterials.add(Material.GRAVEL);
        validFloorMaterials.add(Material.SAND);
        validFloorMaterials.add(Material.DIRT);
        validFloorMaterials.add(Material.GRASS_PATH);
    }
}
