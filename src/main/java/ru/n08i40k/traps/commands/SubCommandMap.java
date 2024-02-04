package ru.n08i40k.traps.commands;

import java.util.HashMap;

public class SubCommandMap extends HashMap<String, SubCommand> {
    public void put(SubCommand subcommand) {
        super.put(subcommand.getName(), subcommand);
    }
}
