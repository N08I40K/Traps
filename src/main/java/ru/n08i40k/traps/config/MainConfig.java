package ru.n08i40k.traps.config;

import lombok.Getter;
import lombok.Setter;
import ru.n08i40k.traps.config.main.WorldCacheEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MainConfig {
    private String lang;
    private boolean enableCrafts;
    private Map<String, WorldCacheEntry> caches;
    private Map<String, Object> regionFlags;
    private List<String> runCommands;
    private List<String> allowedWorlds;

    public MainConfig() {
        lang = "ru-RU";
        enableCrafts = true;
        caches = new HashMap<>();

        regionFlags = new HashMap<>();
        regionFlags.put("pvp", true);

        runCommands = new ArrayList<>();
        runCommands.add("god %player% off");
        runCommands.add("fly %player% off");
        runCommands.add("minecraft:gamemode survival %player%");

        allowedWorlds = new ArrayList<>();
        allowedWorlds.add("world");
        allowedWorlds.add("world_nether");
        allowedWorlds.add("world_the_end");
    }
}
