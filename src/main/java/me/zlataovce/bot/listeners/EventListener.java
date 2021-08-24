package me.zlataovce.bot.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class EventListener<T extends Event> {
    private final Class<T> event;

    public abstract Mono<Void> execute(T event);

    public EventListener(GatewayDiscordClient client, Class<T> event) {
        this.event = event;
        client.on(event)
                .flatMap(this::execute)
                .subscribe();
    }
}
