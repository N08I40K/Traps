package ru.n08i40k.traps.napi;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import ru.n08i40k.npluginapi.event.itemStack.NItemStackDamageEntityEvent;
import ru.n08i40k.npluginapi.event.itemStack.NItemStackInteractEvent;
import ru.n08i40k.npluginlocale.LocaleRequestBuilder;
import ru.n08i40k.traps.TrapsPlugin;
import ru.n08i40k.traps.config.traps.PotionEffectEntry;
import ru.n08i40k.traps.config.traps.TrapEntry;
import ru.n08i40k.npluginapi.itemStack.NItemStack;
import ru.n08i40k.traps.trap.ActiveTrap;
import ru.n08i40k.traps.world.WorldCache;

import javax.annotation.Nullable;
import java.util.*;

@Getter
public class TrapItemStack extends NItemStack {
    private final TrapEntry trapEntry;

    public TrapItemStack(@NonNull TrapEntry trapEntry) {
        super(TrapsPlugin.getInstance().getNPlugin(), getItemStackFromTrapEntry(trapEntry), trapEntry.getId(), false);

        this.trapEntry = trapEntry;
    }

    private static ItemStack getItemStackFromTrapEntry(@NonNull TrapEntry trapEntry) {
        ItemStack itemStack = new ItemStack(trapEntry.getItemMaterial());
        itemStack.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        itemStack.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        LocaleRequestBuilder localeRoot =
                new LocaleRequestBuilder(null, "trap").extend(trapEntry.getId());

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(localeRoot.get("name").getSingle().getC());
        itemMeta.lore(localeRoot.get("lore").format(Map.of(
                "catchRadius", trapEntry.getCatchRadius(),
                "size", 1 + trapEntry.getBoxRadius() * 2,
                "activeTime", trapEntry.getActiveTime()
        )).getMultiple().getC());

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @Override
    public void onInteract(NItemStackInteractEvent event) {
        if (createTrap(event.getBukkitEvent().getPlayer(), null))
            event.getItemStack().setAmount(event.getItemStack().getAmount() - 1);
    }

    @Override
    public void onDamageEntity(NItemStackDamageEntityEvent event) {
        EntityDamageByEntityEvent bukkitEvent = event.getBukkitEvent();

        if (!(bukkitEvent.getDamager() instanceof Player damager))
            return;

        if (!(bukkitEvent.getEntity() instanceof Player player))
            return;

        if (createTrap(damager, Set.of(player)))
            event.getItemStack().setAmount(event.getItemStack().getAmount() - 1);
    }

    private boolean createTrap(@NonNull Player player, @Nullable Set<Player> additionalPlayers) {
        if (!TrapsPlugin.getInstance().getMainConfigData()
                .getAllowedWorlds().contains(player.getWorld().getName()))
            return false;

        player.setCooldown(trapEntry.getItemMaterial(), 20);

        Set<Player> affectedPlayers = new HashSet<>(player.getLocation().getNearbyPlayers(trapEntry.getCatchRadius()));
        if (additionalPlayers != null)
            affectedPlayers.addAll(additionalPlayers);

        ActiveTrap.ACTIVE_TRAPS.forEach((uuid, activeTrap) ->
                activeTrap.getPlayers().keySet().forEach(affectedPlayers::remove));

        if (!affectedPlayers.contains(player))
            return false;

        if (WorldCache.isCollide(player.getLocation(), trapEntry.getBoxRadius() + 1))
            return false;

        player.setCooldown(trapEntry.getItemMaterial(), 20 * trapEntry.getActiveTime());

        List<PotionEffect> potionEffects = new ArrayList<>();
        trapEntry.getEffectEntries().forEach(potionEffectEntry ->
                potionEffects.add(
                        PotionEffectEntry.Accessor.getPotionEffect(potionEffectEntry)));

        affectedPlayers.forEach(affectedPlayer -> {
            if (affectedPlayer != player)
                player.addPotionEffects(potionEffects);
        });

        ActiveTrap newTrap = new ActiveTrap(trapEntry, affectedPlayers, player.getLocation());
        newTrap.activate();

        return true;
    }
}
