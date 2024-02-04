package ru.n08i40k.traps.commands.subcommands.locale;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.n08i40k.traps.commands.SubCommand;

public class LocaleReloadCommand extends SubCommand {
    public LocaleReloadCommand(@Nullable SubCommand parent) {
        super(parent);
    }

    @Override
    public @NotNull String getName() {
        return "reload";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        locale.reload(null);
        localeRequestBuilder.get("has-been-reloaded").getSingle().sendMessage(sender);

        return true;
    }
}
