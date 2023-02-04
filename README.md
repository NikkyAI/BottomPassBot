# BottomPass Bot

## configuring `.env` file

copy the default .env file

```fish
cp .env.default.txt .env
```

### generating twitch chatbot access token

click `chat bot token` on here  
https://twitchtokengenerator.com/  
and copy the access token into `.env` as the value for `TWITCH_ACCESS_TOKEN`

## running

run directly

```fish
./gradlew run
```

or with shadow plugin

```fish
./gradlew runShadow
```

or create a fat jar

```fish
./gradlew shadowJar
java -jar build/libs/application.jar
```


## building docker container

**TODO**

## usage with docker compose

**TODO**