package me.zlataovce.bot;

import me.zlataovce.bot.services.DiscordClientProviderService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Objects;

@SpringBootApplication
@EnableScheduling
public class BotApplication {
	public static void main(String[] args) {
		ApplicationContext context = new SpringApplicationBuilder()
				.sources(BotApplication.class)
				.registerShutdownHook(true)
				.run(args);
		Objects.requireNonNull(
				(DiscordClientProviderService) context.getBean("discordClientProvider")
		).getGateway().onDisconnect().block();
	}
}
