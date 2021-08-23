package me.zlataovce.bot.services;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Objects;

@Slf4j
@Getter
@Service("discordClientProvider")
public class DiscordClientProviderServiceImpl implements DiscordClientProviderService, ApplicationContextAware {
    @Getter(AccessLevel.NONE)
    private final Environment env;
    @Setter
    private ApplicationContext applicationContext;
    private DiscordClient discordClient;
    private GatewayDiscordClient gateway;

    @Autowired
    public DiscordClientProviderServiceImpl(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void init() {
        if (this.env.getProperty("bot.token") == null) {
            log.error("Discord bot token is null, can't continue.");
            ((ConfigurableApplicationContext) this.applicationContext).close();
            System.exit(1);
        }
        this.discordClient = DiscordClientBuilder.create(Objects.requireNonNull(this.env.getProperty("bot.token")))
                .build();
        this.gateway = this.discordClient.login().block();
    }
}
