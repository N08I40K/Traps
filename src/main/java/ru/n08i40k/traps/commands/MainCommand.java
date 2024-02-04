package ru.n08i40k.traps.commands;

import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import ru.n08i40k.traps.TrapsPlugin;
import ru.n08i40k.traps.commands.subcommands.AdminCommand;
import ru.n08i40k.traps.commands.subcommands.LocaleCommand;
import ru.n08i40k.npluginlocale.Locale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainCommand extends Command {
    TrapsPlugin plugin;
    Logger logger;
    Locale locale;

    List<SubCommand> subcommands = new ArrayList<>();

    public MainCommand() {
        super(TrapsPlugin.PLUGIN_NAME_LOWER, "Main " + TrapsPlugin.PLUGIN_NAME + " command.", "/" + TrapsPlugin.PLUGIN_NAME_LOWER, List.of("trap"));

        plugin = TrapsPlugin.getInstance();

        locale = Locale.getInstance();
        logger = plugin.getSLF4JLogger();

        subcommands.add(new LocaleCommand(null));
        subcommands.add(new AdminCommand(null));
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");

        if (args.length > 1)
            for (SubCommand subcommand : subcommands)
                if (subcommand.getName().equals(args[0]))
                    return subcommand.tabComplete(sender, alias, Arrays.copyOfRange(args, 1, args.length));

        String lastWord = args[0];
        List<String> matchedSubcommands = new ArrayList<>();

        for (SubCommand subcommand : subcommands) {
            if (!subcommand.getPermissionBuilder().has(sender))
                continue;

            if (lastWord.isEmpty() || StringUtil.startsWithIgnoreCase(subcommand.getName(), lastWord))
                matchedSubcommands.add(subcommand.getName());
        }

        matchedSubcommands.sort(String.CASE_INSENSITIVE_ORDER);
        return matchedSubcommands;
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length >= 1)
            for (SubCommand subcommand : subcommands)
                if (subcommand.getName().equals(args[0]))
                    return subcommand.tryExecute(sender, label, Arrays.copyOfRange(args, 1, args.length));

        return false;
    }
}
