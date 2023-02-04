package moe.nikky

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.kotlindiscord.kord.extensions.utils.env
import io.klogging.logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import okio.buffer
import okio.source
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

val logger = logger("Main")

object TestTwitch

val blacklistSet by lazy {
    runBlocking {
        loadFilter()
    }
}

fun main(vararg args: String): Unit = runBlocking {
    setupLogging()

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    logger.info("Program arguments: {args}", args.toList())

    //TODO: deal with OAuth login using my own api credentials ?
    val accessToken = env("TWITCH_ACCESS_TOKEN")
//    val refreshToken = env("TWITCH_REFRESH_TOKEN")
//    val clientId = env("TWITCH_CLIENT_ID")

    // chat credential
    val credential = OAuth2Credential("twitch", accessToken)

    // twitch client
    val twitchClient = TwitchClientBuilder.builder()
        .withEnableChat(true)
        .withChatAccount(credential)
        .build()

    coroutineScope {
//        val filterSet = loadFilter()
        val messages = Channel<ChannelMessageEvent>()

        twitchClient.eventManager.onEvent(ChannelMessageEvent::class.java) { event ->
//            println("[${event.channel.name}] ${event.user.name}: ${event.message}")
            messages.trySend(event)
        }

//        val model = runModel(instances = 10)

        val poolContext = newFixedThreadPoolContext(8, "message")

        launch(poolContext) {
            messages
                .consumeAsFlow()
                .collect { event ->
                    launch(poolContext) {
                        processMessage(event)
                    }
                }
        }

//        twitchClient.chat.joinChannel("iskall85")
//        twitchClient.chat.joinChannel("pearlescentmoon")
//        twitchClient.chat.joinChannel("whoisfelyce")
//        twitchClient.chat.joinChannel("yejuniverse")
//        twitchClient.chat.joinChannel("supercatkei")
//        twitchClient.chat.joinChannel("elesky")
//        twitchClient.chat.joinChannel("colaway")

//        twitchClient.chat.joinChannel("skratchbastid")
//        twitchClient.chat.joinChannel("ultra_vibrance")
//
//        twitchClient.chat.joinChannel("expiredpopsicle")
//        twitchClient.chat.joinChannel("ribbonthe")
//        twitchClient.chat.joinChannel("kainei")
//
//        twitchClient.chat.joinChannel("njna_grimsdottir")
        twitchClient.chat.joinChannel("nikkyai")
        twitchClient.chat.joinChannel("mxtressmyxx")
    }
}

@OptIn(ExperimentalTime::class)
suspend fun loadFilter(): HashSet<String> {
//    // the maximum number of elements that the filter will contain
//    // the maximum number of elements that the filter will contain
//    val numberOfElements = 400_000
//
//    // the max false positive probability that is desired
//    // the lower the value - the more will be the memory usage
//
//    // the max false positive probability that is desired
//    // the lower the value - the more will be the memory usage
//    val fpp = 0.01
//    val filter = object : AbstractBloomFilter<String>(
//        /* expectedInsertions = */ numberOfElements,
//        /* falsePositiveProbability = */ fpp
//    ) {
//        /**
//         * Used a [FileBackedBitArray] to allow for file persistence.
//         *
//         * @returns a [BitArray] that will take care of storage of bloom filter
//         */
//        override fun createBitArray(numBits: Int): BitArray {
//            val tmpFile = File.createTempFile("alpha_words", "bloom.filter")
//            return FileBackedBitArray(tmpFile, numBits)
//        }
//    }

    val set = HashSet<String>()

    val duration = measureTime {
        logger.info("starting to fill set")
        TestTwitch::class.java.getResourceAsStream("/english_words_alpha.txt").source().use { s ->
            s.buffer().use { bufferedSource ->
                while (true) {
                    val line = bufferedSource.readUtf8Line() ?: break
//                    filter.add(line)
                    set.add(line.lowercase())
                }
            }
        }
        TestTwitch::class.java.getResourceAsStream("/blacklist.txt").source().use { s ->
            s.buffer().use { bufferedSource ->
                while (true) {
                    val line = bufferedSource.readUtf8Line() ?: break
//                    filter.add(line)
                    set.add(line.lowercase())
                }
            }
        }
    }
    logger.info("filling set took {duration}", duration)

    return set
}


@OptIn(ExperimentalTime::class)
suspend fun processMessage(event: ChannelMessageEvent) {
    logger.info(
        "[{channel}] {user}: {message}",
        event.channel.name,
        event.user.name,
        event.message
    )
    if (event.message.startsWith("!")) {
        logger.warn("ignoring command: {message}", event.message)
        return
    }
    if(event.customRewardId.isPresent) {
        logger.warn("ignoring reward message: {message}", event.message)
        return
    }
    val words = event.stripMessage()

    val joined = words.joinToString(" ")
    if (joined.isBlank()) {
        logger.warn("ignoring empty message, originally: {message}", event.message)
        return
    }

    val realWordLength = words
        .filter { blacklistSet.contains(it.lowercase()) }
        .sumOf { it.length }
    val totalLength = words
        .sumOf { it.length }
    val realWordRatio = realWordLength.toFloat() / totalLength

    logger.info("real word ratio: {ratio}, {words}", realWordRatio, words.joinToString(" "))

    if(realWordRatio > 0.3f) {
        return
    }

    logger.info("analyzing: '{message}'", joined, event.channel.name)
//    val scores = coroutineScope {
//        withTimeoutOrNull(3000) {
//            words.filter { it.length >= 3 }.map { word ->
//                if(filterSet.contains(word.lowercase())) {
//                    // may be in bloomfilter
//                    async { 0.001f to word }
//                } else {
//                    model.invoke(word.take(96))
//                }
//            }.awaitAll()
//        }
//    } ?: run {
//        logger.error("model timed out, {input}", words)
//        return
//    }

//    val averageScore = scores.map { it.first }.average()
//    logger.info("scores: ~{average} == avg{scores}", averageScore, scores)
////                    logger.info("score: {average}", averageScore)
//    if(averageScore > 0.3) {
//        logger.warn("KEYSMASH DETECTED: {score}% {message}", (averageScore * 100).roundToInt(), joined)
//    }
//    val unifiedScore = model.invoke(joined)
//        .await().first
//    logger.info("unified: {score}", unifiedScore)
//    if (unifiedScore > 0.5) {
//        logger.warn("KEYSMASH DETECTED: {score}% {message}", (unifiedScore * 100).roundToInt(), joined)
//    }


    val keysmashScoreOne = KeyboardLayout.layouts.maxOf { layout ->
        rateKeysmash(joined, layout)
    }
    val keysmashScoreTwo = KeyboardLayout.layouts.maxOf { layout ->
        words
            .filter { it.length >= 3 }
            .map { word -> rateKeysmash(word, layout) }
            .average()
    }
    logger.info("rating: {score_one} {score_two}", keysmashScoreOne, keysmashScoreTwo)
    val score = if (keysmashScoreOne > 0.5) {
        logger.warn("KEYSMASH_ONE DETECTED: {score}% {message}", (keysmashScoreOne * 100).roundToInt(), joined)
        keysmashScoreOne
    } else if (keysmashScoreTwo > 0.5) {
        logger.warn("KEYSMASH_TWO DETECTED: {score}% {message}", (keysmashScoreTwo * 100).roundToInt(), joined)
        keysmashScoreTwo
    } else {
        0.0
    }
    if(score > 0.0) {
//        if(event.channel.name == "nikkyai") {
            event.twitchChat.sendMessage(
                /* channel = */
                event.channel.name,
                /* message = */
                "@${event.user.name} you seem to have sent a password into chat there, i rate it ${(score * 100).roundToInt()}% (THIS IS A TEST)",
            )
//        }
    }
}

private val SPACES_REGEX = "\\s+".toRegex()

suspend fun ChannelMessageEvent.stripMessage(): List<String> {
    val emotes = parseEmotes()

    var msg = message

    msg = msg.replace("https://\\S+".toRegex(), "")
    msg = msg.replace("https://\\S+".toRegex(), "")

    replyInfo?.also { replyInfo ->
        logger.debug("reply to '{displayName}'", replyInfo.displayName)
        val prefix = "@${replyInfo.userLogin} "
//        msg = msg.substringAfter(prefix)
        msg = msg.replaceRange(
            0,
            prefix.length-1,
            " ".repeat(prefix.length)
        )
    }

    if (emotes != null) {
        logger.debug("emotes: {emotes}", emotes)
        val ranges = emotes.values.flatten()

//        println("message: '$msg'")

        val blankedMessage = ranges.fold(msg) { message, range ->
            try {
                message.replaceRange(
                    range,
                    " ".repeat((range.last - range.first) + 1)
                )
            } catch (e: IndexOutOfBoundsException) {
                logger.warn("inputs: '{message}'", msg)
                logger.warn("inputs: '{message}', {range}", message, range)
                logger.warn("emotes: {emotes}", emotes)
                logger.error(e, "IOOB")
                message
            }
        }
        logger.debug("input: {input}", msg)
        logger.debug("output:{blanked}", blankedMessage)
//        println("blanked: '$blankedMessage'")
        msg = blankedMessage
    }

    val words = msg.split(SPACES_REGEX)

    return words.filter { it.isNotBlank() }.map { it.trim() }
}

suspend fun ChannelMessageEvent.parseEmotes(): Map<String, List<IntRange>>? {
    val tags = messageEvent.tags
    val emotes = tags["emotes"]
    return if (emotes != null) {
        logger.debug("emotes: {emotes}", emotes)

        emotes.split('/').associate { emote ->
            val emoteId = emote.substringBefore(":")
            val ranges = emote.substringAfter(":")
                .split(",").map { range ->
                    val from = range.substringBefore('-').toInt()
                    val to = range.substringAfter('-').toInt()
                    from..to
                }
            emoteId to ranges
        }
    } else {
        null
    }

}