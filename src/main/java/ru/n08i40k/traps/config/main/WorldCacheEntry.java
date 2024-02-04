package ru.n08i40k.traps.config.main;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorldCacheEntry {
    private String uuid;
    private String worldGuardUuid;
    private String worldEditSchematicUuid;
    private String worldName;
    private int centerX;
    private int centerY;
    private int centerZ;
    private int radius;
}
