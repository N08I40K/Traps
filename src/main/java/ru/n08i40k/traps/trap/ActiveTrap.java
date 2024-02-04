package ru.n08i40k.traps.trap;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.FuzzyBlockState;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.n08i40k.traps.TrapsPlugin;
import ru.n08i40k.traps.config.traps.TrapEntry;
import ru.n08i40k.traps.world.WorldCache;

import java.util.*;

@Getter
@Setter
public class ActiveTrap {
    public static final Map<UUID, ActiveTrap> ACTIVE_TRAPS = new HashMap<>();

    private final TrapEntry trapEntry;
    private Map<Player, Location> players;
    private final Location center;
    private final WorldCache worldCache;
    private final UUID uuid;

    public ActiveTrap(@NonNull TrapEntry trapEntry, @NonNull Set<Player> players, @NonNull Location center) {
        uuid = UUID.randomUUID();
        this.trapEntry = trapEntry;
        this.players = new HashMap<>();
        this.center = center;
        worldCache = new WorldCache();

        players.forEach(player -> this.players.put(player, player.getLocation()));

        new TrapRemoverTask(this);
    }

    public void activate() {
        worldCache.createRegion(center, trapEntry.getBoxRadius() + 1);

        fillMaterials();
        fillAir();

        players.keySet().forEach(player -> player.teleport(center));

        ACTIVE_TRAPS.put(uuid, this);

        TrapsPlugin plugin = TrapsPlugin.getInstance();
        List<String> runCommands = plugin.getMainConfigData().getRunCommands();

        Server server = plugin.getServer();
        ConsoleCommandSender consoleCommandSender = server.getConsoleSender();

        players.keySet().forEach(player ->
            runCommands.forEach(command ->
                server.dispatchCommand(consoleCommandSender, command.replace("%player%", player.getName()))
            )
        );
    }

    private void fillMaterials() {
        BlockVector3[] boundingBox = WorldCache.getBoundingBox(center, trapEntry.getBoxRadius() + 1);
        CuboidRegion region = new CuboidRegion(boundingBox[0], boundingBox[1]);

        RandomPattern randomPattern = new RandomPattern();
        trapEntry.getMaterials().forEach(material -> {
            BlockType blockType = new BlockType(material.name().toLowerCase());
            FuzzyBlockState fuzzyBlockState = new FuzzyBlockState.Builder()
                    .type(blockType)
                    .build();

            randomPattern.add(fuzzyBlockState, 1.f);
        });

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(center.getWorld()))) {
            RegionFunction set = new BlockReplace(editSession, randomPattern);
            RegionVisitor visitor = new RegionVisitor(region, set);

            Operations.completeBlindly(visitor);
        }
    }

    private void fillAir() {
        BlockVector3[] boundingBox = WorldCache.getBoundingBox(center, trapEntry.getBoxRadius());
        CuboidRegion region = new CuboidRegion(boundingBox[0], boundingBox[1]);

        FuzzyBlockState fuzzyBlockState = new FuzzyBlockState.Builder()
                .type(new BlockType(Material.AIR.name().toLowerCase()))
                .build();

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(center.getWorld()))) {
            RegionFunction set = new BlockReplace(editSession, fuzzyBlockState);
            RegionVisitor visitor = new RegionVisitor(region, set);

            Operations.completeBlindly(visitor);
        }
    }

    public void deactivate() {
        worldCache.removeRegion();
        players.forEach(Player::teleport);
    }

    public static class ActiveTrapListener implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onPlayerMove(PlayerMoveEvent event) {
            Player player = event.getPlayer();
            ACTIVE_TRAPS.forEach((uuid, activeTrap) -> {
                if (activeTrap.players.containsKey(player)) {
                    Location playerLocation = player.getLocation();
                    Location centerLocation = activeTrap.getCenter();
                    int radius = activeTrap.getTrapEntry().getBoxRadius();
                    if (
                            Math.abs(playerLocation.getBlockX() - centerLocation.getBlockX()) > radius ||
                            Math.abs(playerLocation.getBlockY() - centerLocation.getBlockY()) > radius ||
                            Math.abs(playerLocation.getBlockZ() - centerLocation.getBlockZ()) > radius) {
                        player.teleport(centerLocation);
                    }
                }
            });
        }

        @EventHandler
        public void onPlayerDeath(PlayerDeathEvent event) {
            Player player = event.getEntity();
            ACTIVE_TRAPS.forEach((uuid, activeTrap) -> activeTrap.players.remove(player));
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            ACTIVE_TRAPS.forEach((uuid, activeTrap) -> activeTrap.players.remove(player));
        }
    }
}
