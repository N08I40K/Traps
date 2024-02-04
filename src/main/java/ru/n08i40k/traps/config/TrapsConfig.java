package ru.n08i40k.traps.config;

import lombok.Getter;
import lombok.Setter;
import ru.n08i40k.traps.config.traps.TrapEntry;

import java.util.HashMap;

@Getter
@Setter
public class TrapsConfig {
    public static class TrapEntryHashMap extends HashMap<String, TrapEntry> {
        public void put(TrapEntry value) {
            super.put(value.getId(), value);
        }

        @Override
        public TrapEntry put(String key, TrapEntry value) {
            return super.put(value.getId(), value);
        }
    }

    private TrapEntryHashMap trapEntries;

    public TrapsConfig() {
        trapEntries = new TrapEntryHashMap();
        trapEntries.put(new TrapEntry());
    }
}
