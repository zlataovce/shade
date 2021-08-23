package me.zlataovce.bot.services;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface DiscordClientProviderService {
    @NonNull
    DiscordClient getDiscordClient();
    @NonNull
    GatewayDiscordClient getGateway();
}
