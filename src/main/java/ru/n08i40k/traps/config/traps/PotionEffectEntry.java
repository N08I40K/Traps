package ru.n08i40k.traps.config.traps;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

@Getter
@Setter
public class PotionEffectEntry {
    private int amplifier;
    private int duration;
    private String type;
    private boolean ambient;
    private boolean particles;
    private boolean icon;

    public static class Accessor {
        public static void setPotionEffect(@NonNull PotionEffectEntry potionEffectEntry, @NonNull PotionEffect potionEffect) {
            potionEffectEntry.setType(potionEffect.getType().getName());
            potionEffectEntry.setDuration(potionEffect.getDuration());
            potionEffectEntry.setAmplifier(potionEffect.getAmplifier());
            potionEffectEntry.setAmbient(potionEffect.isAmbient());
            potionEffectEntry.setParticles(potionEffect.hasParticles());
            potionEffectEntry.setIcon(potionEffect.hasIcon());
        }

        public static PotionEffect getPotionEffect(@NonNull PotionEffectEntry potionEffectEntry) {
            return new PotionEffect(
                    Objects.requireNonNull(PotionEffectType.getByName(potionEffectEntry.getType())),
                    potionEffectEntry.getDuration(),
                    potionEffectEntry.getAmplifier(),
                    potionEffectEntry.isAmbient(),
                    potionEffectEntry.isParticles(),
                    potionEffectEntry.isIcon());
        }
    }
}
