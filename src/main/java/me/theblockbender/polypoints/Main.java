package me.theblockbender.polypoints;

import me.theblockbender.polypoints.commands.PointCommand;
import me.theblockbender.polypoints.runnables.CraftingSlotSetter;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main extends JavaPlugin {

    public List<Material> validFloorMaterials = new ArrayList<>();
    public Random random = new Random();

    public void onEnable() {
        populateFloor();
        getCommand("polypoint").setExecutor(new PointCommand(this));
        CraftingSlotSetter craftingSlotSetter = new CraftingSlotSetter();
        getServer().getPluginManager().registerEvents(craftingSlotSetter, this);
        craftingSlotSetter.runTaskTimer(this, 0L, 1L);
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
