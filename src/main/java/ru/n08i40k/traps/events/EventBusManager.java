package ru.n08i40k.traps.events;

import lombok.Getter;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;

import java.lang.invoke.MethodHandles;

public class EventBusManager {
    @Getter
    private static IEventBus eventBus;

    public static IEventBus initEventBus() {
        if (eventBus != null)
            throw new IllegalArgumentException("Bus already initialized");

        eventBus = new EventBus();

        eventBus.registerLambdaFactory("ru.n08i40k.traps", (lookupInMethod, klass) ->
                (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        return eventBus;
    }

}
