# Shade
A bot designed to catch the so-called "steam scams" in it's roots

## Installation (needs Java 11+)
 - Compile the executable JAR with `gradlew bootJar` and take it from `build/libs`.
 - or get it from my CI: [https://ci.kcra.me/project/Shade](https://ci.kcra.me/project/Shade)

## Running the bot
 - You can run the JAR with a simple command: `java -jar <file>.jar --bot.token=yourbottokenhere`
 - Or you can create an `application.properties` file next to the JAR with the following format:
```properties
bot.token=yourbottokenhere
```
and then you can run it like this: `java -jar <file>.jar`