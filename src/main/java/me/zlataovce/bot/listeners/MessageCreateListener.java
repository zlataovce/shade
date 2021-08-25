package me.zlataovce.bot.listeners;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.rest.util.Color;
import info.debatty.java.stringsimilarity.Cosine;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.Levenshtein;
import me.zlataovce.bot.services.DiscordClientProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@DependsOn("discordClientProvider")
public class MessageCreateListener extends EventListener<MessageCreateEvent> {
    private final TaskScheduler scheduler;

    private static final Levenshtein LEVENSHTEIN = new Levenshtein();
    private static final Cosine COSINE = new Cosine();
    private static final JaroWinkler JARO_WINKLER = new JaroWinkler();

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
                    final List<String> urlParts = Arrays.stream(part.replace("http://", "")
                            .replace("https://", "")
                            .split("/")[0]
                            .split("\\.")).collect(Collectors.toList());
                    if (Objects.equals(urlParts.get(0), "www")) {
                        urlParts.remove(0);
                    }
                    final String url = String.join(".", urlParts);
                    final int distance = (int) LEVENSHTEIN.distance("steamcommunity.com", url);
                    final double cosineSimilarity = COSINE.similarity("steamcommunity.com", url);
                    final double jwSimilarity = JARO_WINKLER.similarity("steamcommunity.com", url);
                    if ((distance <= 6 && distance > 0) && (cosineSimilarity < 1 && cosineSimilarity >= 0.75) && (jwSimilarity < 1 && jwSimilarity >= 0.75)) {
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
                                    messageCreateSpec.setContent(
                                            Optional.ofNullable(event.getGuild()
                                                    .flatMapMany(Guild::getRoles)
                                                    .filter(role -> role.getName().toLowerCase(Locale.ROOT).contains("shade notifications"))
                                                    .map(Role::getMention)
                                                    .collect(Collectors.joining(" "))
                                                    .block()).orElse("")
                                    );
                                })).doOnSuccess(message -> this.scheduler.schedule(() -> message.delete().block(), new Date(System.currentTimeMillis() + 300000))).onErrorResume(e -> Mono.empty()).then();
                    }
                }
            }
        }
        return Mono.empty();
    }
}
