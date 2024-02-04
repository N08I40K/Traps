package ru.n08i40k.traps.commands.subcommands.admin;

import com.google.common.base.Preconditions;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.n08i40k.traps.commands.SubCommand;

public class ConfigReloadCommand extends SubCommand {
    public ConfigReloadCommand(@Nullable SubCommand parent) {
        super(parent);
    }

    @Override
    public @NotNull String getName() {
        return "reload";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Preconditions.checkState(plugin.getMainConfig().load(),
                "Can't reload main config!");
        Preconditions.checkState(plugin.getTrapsConfig().load(),
                "Can't reload traps config!");

        localeRequestBuilder.get("has-been-reloaded").getSingle().sendMessage(sender);
        return true;
    }
}
