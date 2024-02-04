package ru.n08i40k.traps.world;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import ru.n08i40k.traps.TrapsPlugin;
import ru.n08i40k.traps.config.main.WorldCacheEntry;

import java.io.*;
import java.util.Map;
import java.util.UUID;

public class WorldCache {
    private final WorldCacheEntry worldCacheEntry;

    public WorldCache() {
        worldCacheEntry = new WorldCacheEntry();

        worldCacheEntry.setUuid(UUID.randomUUID().toString());
        worldCacheEntry.setWorldGuardUuid("traps-" + worldCacheEntry.getUuid());
        worldCacheEntry.setWorldEditSchematicUuid(worldCacheEntry.getUuid());
    }

    public WorldCache(@NonNull WorldCacheEntry worldCacheEntry) {
        this.worldCacheEntry = worldCacheEntry;
    }

    public static @NotNull BlockVector3[] getBoundingBox(@NotNull Location center, int radius) {
        BlockVector3[] boundingBox = new BlockVector3[2];

        boundingBox[0] = BlockVector3.at(center.getX() - radius, center.getY() - radius, center.getZ() - radius);
        boundingBox[1] = BlockVector3.at(center.getX() + radius, center.getY() + radius, center.getZ() + radius);

        return boundingBox;
    }

    public static boolean isCollide(@NotNull Location location, int radius) {
        BlockVector3[] boundingBox = getBoundingBox(location, radius);

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(location.getWorld()));
        assert regionManager != null;

        ProtectedCuboidRegion region = new ProtectedCuboidRegion("traps-has-area", boundingBox[0], boundingBox[1]);
        ApplicableRegionSet regionSet = regionManager.getApplicableRegions(region);

        return regionSet.size() > 0;
    }

    public void createRegion(@NonNull Location center, int radius) {
        Preconditions.checkState(radius > 1, "Trap radius must be greater than 1");

        worldCacheEntry.setRadius(radius);
        worldCacheEntry.setCenterX(center.getBlockX());
        worldCacheEntry.setCenterY(center.getBlockY());
        worldCacheEntry.setCenterZ(center.getBlockZ());
        worldCacheEntry.setWorldName(center.getWorld().getName());

        createWorldGuardRegion();
        createWorldEditCache();

        TrapsPlugin.getInstance().getMainConfigData().getCaches().put(worldCacheEntry.getUuid(), worldCacheEntry);
        TrapsPlugin.getInstance().getMainConfig().save();
    }

    public void removeRegion() {
        removeWorldGuardRegion();
        revertWorldEditCache();

        TrapsPlugin.getInstance().getMainConfigData().getCaches().remove(worldCacheEntry.getUuid());
        TrapsPlugin.getInstance().getMainConfig().save();
    }

    public void createWorldEditCache() {
        World world = Bukkit.getWorld(worldCacheEntry.getWorldName());
        assert world != null;

        BlockVector3[] boundingBox = getBoundingBox(
                new Location(world, worldCacheEntry.getCenterX(), worldCacheEntry.getCenterY(), worldCacheEntry.getCenterZ()),
                worldCacheEntry.getRadius());

        CuboidRegion region = new CuboidRegion(boundingBox[0], boundingBox[1]);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                BukkitAdapter.adapt(world), region, clipboard, region.getMinimumPoint()
        );

        Logger logger = TrapsPlugin.getInstance().getSLF4JLogger();

        try {
            Operations.complete(forwardExtentCopy);
        } catch (WorldEditException e) {
            logger.error(e.toString());
            return;
        }
        File schematicsDirectory = new File(TrapsPlugin.getInstance().getDataFolder(), "schematics");
        if (!schematicsDirectory.exists() && !schematicsDirectory.mkdirs()) {
            logger.error("Unable to create schematics directory!");
            return;
        }

        File file = new File(schematicsDirectory, worldCacheEntry.getWorldEditSchematicUuid() + ".schematic");
        try {
            if (!file.createNewFile()) {
                logger.error("Unable to create schematic file!");
                return;
            }
        } catch (IOException e) {
            logger.error(e.toString());
            return;
        }

        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
        } catch (IOException e) {
            logger.error(e.toString());
        }
    }

    public void createWorldGuardRegion() {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

        World world = Bukkit.getWorld(worldCacheEntry.getWorldName());
        assert world != null;

        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
        assert regionManager != null;

        BlockVector3[] boundingBox = getBoundingBox(
                new Location(world, worldCacheEntry.getCenterX(), worldCacheEntry.getCenterY(), worldCacheEntry.getCenterZ()),
                worldCacheEntry.getRadius());

        ProtectedCuboidRegion region = new ProtectedCuboidRegion(
                        worldCacheEntry.getWorldGuardUuid(), boundingBox[0], boundingBox[1]);

        regionManager.addRegion(region);

        applyFlags(region);
    }

    private void applyFlags(@NotNull ProtectedRegion region) {
        Map<String, Object> flags = TrapsPlugin.getInstance().getMainConfigData().getRegionFlags();

        FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();

        for (String flagName : flags.keySet()) {
            Flag<?> flag = Flags.fuzzyMatchFlag(flagRegistry, flagName);

            Object flagValue = flags.get(flagName);

            if (flagValue instanceof Boolean stateFlag)
                region.setFlag((StateFlag) flag, stateFlag ? StateFlag.State.ALLOW : StateFlag.State.DENY);

            else if (flagValue instanceof Integer integerFlag)
                region.setFlag((IntegerFlag) flag, integerFlag);

            else if (flagValue instanceof Double doubleFlag)
                region.setFlag((DoubleFlag) flag, doubleFlag);

            else if (flagValue instanceof String stringFlag)
                region.setFlag((StringFlag) flag, stringFlag);
        }
    }

    public void removeWorldGuardRegion() {
        World world = Bukkit.getWorld(worldCacheEntry.getWorldName());
        assert world != null;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
        assert regionManager != null;

        regionManager.removeRegion(worldCacheEntry.getWorldGuardUuid());
    }

    public void revertWorldEditCache() {
        World world = Bukkit.getWorld(worldCacheEntry.getWorldName());
        assert world != null;

        BlockVector3[] boundingBox = getBoundingBox(
                new Location(world, worldCacheEntry.getCenterX(), worldCacheEntry.getCenterY(), worldCacheEntry.getCenterZ()),
                worldCacheEntry.getRadius());

        CuboidRegion region = new CuboidRegion(boundingBox[0], boundingBox[1]);
        Clipboard clipboard;

        Logger logger = TrapsPlugin.getInstance().getSLF4JLogger();

        File schematicsDirectory = new File(TrapsPlugin.getInstance().getDataFolder(), "schematics");
        if (!schematicsDirectory.exists()) {
            logger.error("Schematics directory doesn't exists!");
            return;
        }

        File file = new File(schematicsDirectory, worldCacheEntry.getWorldEditSchematicUuid() + ".schematic");
        if (!file.exists()) {
            logger.error("Schematic file doesn't exists!");
            return;
        }

        try (ClipboardReader reader = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (IOException e) {
            logger.error(e.toString());
            return;
        }

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(region.getMinimumPoint())
                    .build();
            Operations.complete(operation);
        } catch (WorldEditException e) {
            logger.error(e.toString());
            return;
        }

        if (!file.delete())
            logger.error("Unable to delete schematic file! {}", file.getPath());
    }
}
