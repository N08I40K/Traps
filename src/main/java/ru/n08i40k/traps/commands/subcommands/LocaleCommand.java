package ru.n08i40k.traps.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.n08i40k.traps.commands.SubCommand;
import ru.n08i40k.traps.commands.subcommands.locale.LocaleReloadCommand;
import ru.n08i40k.traps.commands.subcommands.locale.LocaleSetCommand;

public class LocaleCommand extends SubCommand {
    public LocaleCommand(@Nullable SubCommand parent) {
        super(parent);

        subcommands.put(new LocaleReloadCommand(this));
        subcommands.put(new LocaleSetCommand(this));
    }

    @Override
    public @NotNull String getName() {
        return "locale";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        localeRequestBuilder.get("select-action", String.join(", ", subcommands.keySet()))
                .getSingle().sendMessage(sender);
        return false;
    }
}
