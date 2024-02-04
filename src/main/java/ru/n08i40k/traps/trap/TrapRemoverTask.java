package ru.n08i40k.traps.trap;

import lombok.NonNull;
import ru.n08i40k.traps.utils.PluginUse;

import java.util.UUID;

public class TrapRemoverTask extends PluginUse implements Runnable {
    private final UUID uuid;

    public TrapRemoverTask(@NonNull ActiveTrap activeTrap) {
        this.uuid = activeTrap.getUuid();

        plugin.getScheduler().runTaskLater(plugin, this, 20L * activeTrap.getTrapEntry().getActiveTime());
    }

    @Override
    public void run() {
        if (!ActiveTrap.ACTIVE_TRAPS.containsKey(uuid)) {
            plugin.getSLF4JLogger().warn("Unable to deactivate trap, because it doesn't exists! UUID {}", uuid.toString());
            return;
        }

        ActiveTrap activeTrap = ActiveTrap.ACTIVE_TRAPS.get(uuid);
        ActiveTrap.ACTIVE_TRAPS.remove(uuid);
        activeTrap.deactivate();
    }
}
