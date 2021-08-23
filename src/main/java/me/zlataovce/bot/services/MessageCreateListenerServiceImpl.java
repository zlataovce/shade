package me.zlataovce.bot.services;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Color;
import me.zlataovce.bot.listeners.EventListener;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@DependsOn("discordClientProvider")
public class MessageCreateListenerServiceImpl extends EventListener<MessageCreateEvent> {
    @Autowired
    public MessageCreateListenerServiceImpl(DiscordClientProviderService service) {
        service.getGateway().on(this.getEventType())
                .flatMap(this::execute)
                .onErrorResume(this::handleError)
                .subscribe();
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
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
                        return event.getMessage().delete()
                            .then(event.getMessage().getChannel()).flatMap(channel -> channel.createMessage(messageCreateSpec -> messageCreateSpec.addEmbed(embedCreateSpec -> {
                                embedCreateSpec.setTitle("Possible scam link!");
                                embedCreateSpec.setDescription("A possible scam link was removed:\n`" + url + "`");
                                if (event.getMember().isPresent()) {
                                    embedCreateSpec.setAuthor(event.getMember().get().getTag(), null, event.getMember().get().getAvatarUrl());
                                }
                                embedCreateSpec.setColor(Color.RED);
                            }))).then();
                    }
                }
            }
        }
        return Mono.empty();
    }
}
