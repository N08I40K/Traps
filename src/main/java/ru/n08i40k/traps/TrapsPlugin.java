package ru.n08i40k.traps;

import com.google.common.base.Preconditions;
import lombok.Getter;
import meteordevelopment.orbit.IEventBus;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import ru.n08i40k.npluginapi.craft.NCraftRecipeEntry;
import ru.n08i40k.traps.commands.MainCommand;
import ru.n08i40k.traps.config.TrapsConfig;
import ru.n08i40k.traps.config.MainConfig;
import ru.n08i40k.traps.config.main.WorldCacheEntry;
import ru.n08i40k.traps.config.traps.PotionEffectEntry;
import ru.n08i40k.traps.config.traps.TrapEntry;
import ru.n08i40k.traps.events.EventBusManager;
import ru.n08i40k.npluginapi.NPluginApi;
import ru.n08i40k.npluginapi.plugin.NPlugin;
import ru.n08i40k.npluginconfig.Config;
import ru.n08i40k.npluginlocale.Locale;
import ru.n08i40k.traps.trap.ActiveTrap;
import ru.n08i40k.traps.world.WorldCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class TrapsPlugin extends JavaPlugin {
    public static final String PLUGIN_NAME = "Traps";
    public static final String PLUGIN_NAME_LOWER = "traps";

    @Getter
    private static TrapsPlugin instance;

    @Getter
    private Config<MainConfig> mainConfig;

    @Getter
    private Config<TrapsConfig> trapsConfig;

    private final List<Command> commands = new ArrayList<>();

    @Getter
    private NPlugin nPlugin;

    @Getter
    private BukkitScheduler scheduler;

    @Override
    public void onLoad() {
        instance = this;

        IEventBus bus = EventBusManager.initEventBus();

        bus.subscribe(TrapEntry.Accessor.class);
    }

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdir()) {
                getSLF4JLogger().error("Cannot create plugin data dir!");
                disable();
            }
        }

        scheduler = getServer().getScheduler();

        // Load configs

        mainConfig = new Config<>(this, EventBusManager.getEventBus(), "main", MainConfig.class, Set.of(
                WorldCacheEntry.class.getName()
        ));
        Preconditions.checkState(mainConfig.isLoaded(),
                "Main config is not loaded!");

        trapsConfig = new Config<>(this, EventBusManager.getEventBus(), "traps", TrapsConfig.class, Set.of(
                TrapEntry.class.getName(),
                Material.class.getName(),
                PotionEffectEntry.class.getName(),
                NCraftRecipeEntry.class.getName()
        ));
        Preconditions.checkState(trapsConfig.isLoaded(),
                "Traps config is not loaded!");

        // Load locale file

        new Locale(this, EventBusManager.getEventBus(), mainConfig.getData().getLang());

        // Register events handler

        this.getServer().getPluginManager().registerEvents(new EventsListener(), this);
        this.getServer().getPluginManager().registerEvents(new ActiveTrap.ActiveTrapListener(), this);

        // Register commands

        commands.add(new MainCommand());

        getServer().getCommandMap().registerAll(PLUGIN_NAME_LOWER, commands);

        // Remove caches

        mainConfig.getData().getCaches().forEach((uuid, worldCacheEntry) ->
                new WorldCache(worldCacheEntry).removeRegion());

        nPlugin = NPluginApi.getInstance().getNPluginManager().registerNPlugin(
                this,
                this.getName(),
                Locale.getInstance().get("npapi.view-name").getSingle().get(),
                Material.NETHERITE_SCRAP);

        TrapEntry.Accessor.loadAll();
    }

    @Override
    public void onDisable() {
        if (nPlugin != null)
            NPluginApi.getInstance().getNPluginManager().unregisterNPlugin(nPlugin);
    }

    public MainConfig getMainConfigData() {
        return mainConfig.getData();
    }
    public TrapsConfig getTrapsConfigData() {
        return trapsConfig.getData();
    }

    public void disable() {
        getPluginLoader().disablePlugin(this);
    }
}
