package ru.n08i40k.traps.commands.subcommands.locale;

import com.google.common.collect.ImmutableList;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.n08i40k.traps.commands.SubCommand;

import java.util.List;
import java.util.Set;

public class LocaleSetCommand extends SubCommand {
    public LocaleSetCommand(@Nullable SubCommand parent) {
        super(parent);
    }

    @Override
    public @NotNull String getName() {
        return "set";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Set<String> locales = locale.getAvailableLocalesNames();

        if (args.length == 0) {
            localeRequestBuilder.get("select-locale", String.join(", ", locales)).getSingle().sendMessage(sender);
            return false;
        }

        if (!locales.contains(args[0])) {
            localeRequestBuilder.get("incorrect-locale", String.join(", ", locales)).getSingle().sendMessage(sender);
            return false;
        }

        plugin.getMainConfigData().setLang(args[0]);
        plugin.saveConfig();

        locale.reload(args[0]);

        localeRequestBuilder.get("successful").getSingle().sendMessage(sender);
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1)
            return getAutocompletion(args[0], locale.getAvailableLocalesNames());

        return ImmutableList.of();
    }
}
