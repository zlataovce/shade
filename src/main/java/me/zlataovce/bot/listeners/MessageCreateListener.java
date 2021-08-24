package me.zlataovce.bot.listeners;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.rest.util.Color;
import me.zlataovce.bot.services.DiscordClientProviderService;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@DependsOn("discordClientProvider")
public class MessageCreateListener extends EventListener<MessageCreateEvent> {
    private final TaskScheduler scheduler;

    @Autowired
    public MessageCreateListener(DiscordClientProviderService service, TaskScheduler scheduler) {
        super(service.getGateway(), MessageCreateEvent.class);
        this.scheduler = scheduler;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        if (event.getGuildId().isPresent()) {
            final String[] parts = event.getMessage().getContent().split(" ");
            for (String part : parts) {
                if (part.startsWith("http://") || part.startsWith("https://")) {
                    final String url = part.replace("http://", "").replace("https://", "").split("/")[0];
                    final int distance = LevenshteinDistance.getDefaultInstance().apply("steamcommunity.com", url);
                    if (distance < 8 && distance > 0) {
                        return event.getMessage().delete().onErrorResume(e -> Mono.empty())
                            .then(event.getMessage().getChannel())
                                .flatMap(channel -> channel.createMessage(messageCreateSpec -> {
                                    messageCreateSpec.addEmbed(embedCreateSpec -> {
                                        embedCreateSpec.setTitle("Possible scam attempt!")
                                                .setDescription("A possible scam attempt was mitigated.\nURL: ||`" + url + "`||")
                                                .setColor(Color.RED)
                                                .setTimestamp(Instant.now());
                                        if (event.getMember().isPresent()) {
                                            embedCreateSpec.setAuthor(event.getMember().get().getTag(), null, null);
                                            embedCreateSpec.setThumbnail(event.getMember().get().getAvatarUrl());
                                        }
                                    });
                                    messageCreateSpec.setContent(Objects.requireNonNullElse(
                                            event.getGuild()
                                                    .flatMapMany(Guild::getRoles)
                                                    .filter(role -> role.getName().toLowerCase(Locale.ROOT).contains("shade notifications"))
                                                    .map(Role::getMention)
                                                    .collect(Collectors.joining(" "))
                                                    .block(), null
                                    ));
                                })).onErrorStop().doOnSuccess(message -> this.scheduler.schedule(() -> message.delete().block(), new Date(System.currentTimeMillis() + 300000))).then();
                    }
                }
            }
        }
        return Mono.empty();
    }
}
