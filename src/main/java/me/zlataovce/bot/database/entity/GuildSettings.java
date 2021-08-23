package me.zlataovce.bot.database.entity;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "guilds")
@Builder(toBuilder = true)
public class GuildSettings {
    @Id
    private Long id;
}
