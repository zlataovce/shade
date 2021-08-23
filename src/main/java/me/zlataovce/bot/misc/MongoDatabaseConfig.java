package me.zlataovce.bot.misc;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import java.util.Objects;

@Configuration
@Slf4j
@EnableReactiveMongoRepositories(basePackages = "me.zlataovce.bot.database.mongo")
public class MongoDatabaseConfig extends AbstractReactiveMongoConfiguration implements ApplicationContextAware {
    @Setter
    private ApplicationContext applicationContext;
    private final Environment env;

    @Autowired
    public MongoDatabaseConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public MongoClient mongoClient() {
        if (this.env.getProperty("bot.db.conn") == null) {
            log.error("Database connection string is null, can't continue.");
            ((ConfigurableApplicationContext) this.applicationContext).close();
            System.exit(1);
        }
        return MongoClients.create(Objects.requireNonNull(this.env.getProperty("bot.db.conn")));
    }

    @Override
    protected @NonNull String getDatabaseName() {
        return this.env.getProperty("db", "bot");
    }
}
