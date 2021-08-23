package me.zlataovce.bot.database.mongo;

import me.zlataovce.bot.database.entity.GuildSettings;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuildSettingsRepository extends ReactiveMongoRepository<GuildSettings, Long> {
}
