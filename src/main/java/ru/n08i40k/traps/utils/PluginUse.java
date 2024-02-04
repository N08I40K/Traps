package ru.n08i40k.traps.utils;

import org.slf4j.Logger;
import ru.n08i40k.traps.TrapsPlugin;
import ru.n08i40k.npluginlocale.Locale;

public class PluginUse {
    protected final TrapsPlugin plugin;
    protected final Locale locale;
    protected final Logger logger;

    public PluginUse() {
        plugin = TrapsPlugin.getInstance();
        locale = Locale.getInstance();
        logger = plugin.getSLF4JLogger();
    }
}
