package me.zlataovce.bot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class BotApplication {
	public static void main(String[] args) {
		new SpringApplicationBuilder()
				.sources(BotApplication.class)
				.registerShutdownHook(true)
				.run(args);
	}
}
