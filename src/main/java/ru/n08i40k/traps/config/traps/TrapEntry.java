package ru.n08i40k.traps.config.traps;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.n08i40k.npluginapi.custom.craft.NCraftRecipe;
import ru.n08i40k.npluginapi.custom.craft.NCraftRecipeEntry;
import ru.n08i40k.npluginapi.registry.NCraftRecipeRegistry;
import ru.n08i40k.traps.TrapsPlugin;
import ru.n08i40k.traps.config.TrapsConfig;
import ru.n08i40k.traps.napi.TrapItemStack;
import ru.n08i40k.npluginapi.NPluginApi;
import ru.n08i40k.npluginapi.registry.NItemStackRegistry;
import ru.n08i40k.npluginapi.plugin.NPlugin;
import ru.n08i40k.npluginapi.plugin.NPluginManager;
import ru.n08i40k.npluginconfig.event.ConfigLoadEvent;
import ru.n08i40k.npluginlocale.LocaleRequestBuilder;
import ru.n08i40k.npluginlocale.SingleLocaleResult;
import ru.n08i40k.npluginlocale.event.LocaleReloadEvent;
import ru.n08i40k.npluginlocale.Locale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TrapEntry {
    private String id;
    private int boxRadius;
    private int catchRadius;
    private int activeTime;

    private Material itemMaterial;
    private List<Material> materials;
    private List<PotionEffectEntry> effectEntries;

    private NCraftRecipeEntry craftRecipe;

    public TrapEntry() {
        id = "template";
        boxRadius = 3;
        catchRadius = 3;
        activeTime = 10;
        itemMaterial = Material.NETHERITE_SCRAP;

        materials = new ArrayList<>();
        materials.add(Material.OBSIDIAN);
        materials.add(Material.CRYING_OBSIDIAN);
        materials.add(Material.BARRIER);

        effectEntries = new ArrayList<>();

        PotionEffectEntry witherPotionEffect = new PotionEffectEntry();
        PotionEffectEntry.Accessor.setPotionEffect(
                witherPotionEffect,
                new PotionEffect(PotionEffectType.WITHER, 5, 1, false, false, true));

        PotionEffectEntry blindnessPotionEffect = new PotionEffectEntry();
        PotionEffectEntry.Accessor.setPotionEffect(
                blindnessPotionEffect,
                new PotionEffect(PotionEffectType.BLINDNESS, 5, 1, false, false, true));

        effectEntries.add(witherPotionEffect);
        effectEntries.add(blindnessPotionEffect);

        craftRecipe = new NCraftRecipeEntry();
        craftRecipe.setShape(List.of(
                "NSN",
                "OCO",
                "NSN"));
        craftRecipe.setIngredients(Map.of(
                'N', Material.NETHERITE_INGOT.name(),
                'S', Material.SHULKER_SHELL.name(),
                'O', Material.OBSIDIAN.name(),
                'C', Material.NETHER_STAR.name()));
    }

    public static class Accessor {
        @Getter
        @Setter
        public static class RegisteredTrap {
            private TrapEntry trapEntry;

            private NCraftRecipe nCraftRecipe;
            private TrapItemStack nItemStack;
        }

        @Getter
        private static final Map<String, RegisteredTrap> registeredTrapMap = new HashMap<>();

        private static final LocaleRequestBuilder localeRoot =
                new LocaleRequestBuilder(null, "trap");


        // Base

        public static LocaleRequestBuilder getLocaleBuilder(@NonNull TrapEntry entry) {
            return localeRoot.extend(entry.getId());
        }

        public static SingleLocaleResult getDisplayName(@NonNull TrapEntry entry) {
            return getLocaleBuilder(entry).get("name").getSingle();
        }

        // NAPI

        @EventHandler
        public static void onLocaleReloadPre(LocaleReloadEvent.Pre ignoredEvent) {
            unloadAll();
        }

        @EventHandler
        public static void onLocaleReloadPost(LocaleReloadEvent.Post ignoredEvent) {
            loadAll();
        }

        @EventHandler
        public static void onConfigReloadPre(ConfigLoadEvent.Pre<TrapsConfig> event) {
            if (!event.getConfig().isLoaded())
                return;

            if (!Locale.isLocaleLoaded())
                return;

            unloadAll();
        }

        @EventHandler
        public static void onConfigReloadPost(ConfigLoadEvent.Post<TrapsConfig> event) {
            if (!event.isSuccessful())
                return;

            if (!Locale.isLocaleLoaded())
                return;

            loadAll();
        }

        public static void unloadAll() {
            if (TrapsPlugin.getInstance().getNPlugin() == null)
                return;

            NPluginManager nPluginManager = NPluginApi.getInstance().getNPluginManager();
            NPlugin nPlugin = TrapsPlugin.getInstance().getNPlugin();

            nPluginManager.getNCraftRecipeRegistry().removeAll(nPlugin);
            nPluginManager.getNItemStackRegistry().removeAll(nPlugin);

            registeredTrapMap.clear();
        }

        public static void loadAll() {
            if (TrapsPlugin.getInstance().getNPlugin() == null)
                return;

            NPluginManager nPluginManager = NPluginApi.getInstance().getNPluginManager();
            NPlugin nPlugin = TrapsPlugin.getInstance().getNPlugin();

            NItemStackRegistry      nItemStackRegistry      = nPluginManager.getNItemStackRegistry();
            NCraftRecipeRegistry    nCraftRecipeRegistry    = nPluginManager.getNCraftRecipeRegistry();

            TrapsConfig trapsConfig = TrapsPlugin.getInstance().getTrapsConfigData();

            trapsConfig.getTrapEntries().values()
                    .forEach(caseEntry -> {
                        RegisteredTrap registeredTrap = new RegisteredTrap();
                        registeredTrap.setTrapEntry(caseEntry);

                        registeredTrapMap.put(caseEntry.getId(), registeredTrap);
                    });

            registeredTrapMap.forEach((key, registeredTrap) -> {
                TrapEntry trapEntry = registeredTrap.getTrapEntry();

                TrapItemStack trapItemStack = new TrapItemStack(trapEntry);

                nItemStackRegistry.add(trapItemStack);
                registeredTrap.setNItemStack(trapItemStack);
            });

            if (TrapsPlugin.getInstance().getMainConfigData().isEnableCrafts()) {
                registeredTrapMap.forEach((key, registeredTrap) -> {
                    TrapEntry trapEntry = registeredTrap.getTrapEntry();

                    NCraftRecipe nCraftRecipe = new NCraftRecipe(nPlugin, trapEntry.getId(), trapEntry.getCraftRecipe(), registeredTrap.getNItemStack());

                    nCraftRecipeRegistry.add(nCraftRecipe);
                    registeredTrap.setNCraftRecipe(nCraftRecipe);
                });
            }
        }
    }
}
