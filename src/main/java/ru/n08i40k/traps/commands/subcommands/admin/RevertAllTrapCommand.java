package ru.n08i40k.traps.commands.subcommands.admin;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.n08i40k.traps.commands.SubCommand;
import ru.n08i40k.traps.trap.ActiveTrap;

import java.util.List;

public class RevertAllTrapCommand extends SubCommand {
    public RevertAllTrapCommand(@Nullable SubCommand parentCommand) {
        super(parentCommand);
    }

    @Override
    public @NotNull String getName() {
        return "revertAllTrap";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player))
            return false;

        ActiveTrap.ACTIVE_TRAPS.forEach((uuid, activeTrap) -> activeTrap.deactivate());
        ActiveTrap.ACTIVE_TRAPS.clear();

        plugin.getMainConfigData().getCaches().clear();
        Preconditions.checkState(plugin.getMainConfig().save(),
                "Unable save main config!");

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        return ImmutableList.of();
    }
}
