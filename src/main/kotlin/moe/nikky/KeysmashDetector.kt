package moe.nikky

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

data class Point(
    val x: Int,
    val y: Int,
) {
    fun distanceSquared(other: Point): Int {
        return ((this.x - other.x) * (this.x - other.x)) +
                ((this.y - other.y) * (this.y - other.y))
    }
    fun distance(other: Point): Double {
        return sqrt(
            distanceSquared(other).toDouble()
        )
    }

    override fun toString(): String {
        return "($x,$y)"
    }
}

sealed class KeyboardLayout(
    val name: String,
    val lowercase: String,
    val uppercase: String,
    val highRating: String,
) {
    companion object {
        fun String.toCoordinateMap() = buildMap<Char, Point> {
            lines().forEachIndexed { y, line ->
                line.forEachIndexed { x, char ->
                    if (char != ' ') {
                        put(char, Point(x, y))
                    }
                }
            }
        }
        val layouts = listOf(
            QWERTY,
            COLEMAK,
            DVORAK,
        )
    }

    private val mappedKeys = lowercase.toCoordinateMap() + uppercase.toCoordinateMap()

    fun lookup(char: Char): Point? {
        return mappedKeys[char]
    }

    val highPoints: List<Point> = highRating.mapNotNull { lookup(it) }

    object QWERTY : KeyboardLayout(
        name = "QWERTY",
        lowercase = """
        §1234567890-=
         qwertyuiop[]
         asdfghjkl;'\
        `zxcvbnm,./
    """.trimIndent().trim(),
        uppercase = """
        ±!@#$%^&*()_+
         QWERTYUIOP{}
         ASDFGHJKL:"|
        ~ZXCVBNM<>?
    """.trimIndent().trim(),
        highRating = "sdfwxhjkun"
    )
    object COLEMAK : KeyboardLayout(
        name = "COLEMAK",
        lowercase = """
            §1234567890-=
             qwfpgjluy;[]
             arstdhneio'\
            `zxcvbkm,./`
        """.trimIndent().trim(),
        uppercase = """
            ±!@#$%^&*()_+
             QWFPGJLUY:{}
             ARSTDHNEIO"|
            ~ZXCVBKM<>?
        """.trimIndent().trim(),
        highRating = "arswxhnelki"
    )
    object DVORAK : KeyboardLayout(
        name = "DVORAK",
        lowercase = """
            §1234567890[]
             ',.pyfgcrl/=
             aoeuidhtns-\
            `;qjkxbmwvz`
        """.trimIndent().trim(),
        uppercase = """
            ±!@#$%^&*(){
             "<>PYFGCRL?+
             AOEUIDHTNS_|
            ~:QJKXBMWVZ
        """.trimIndent().trim(),
        highRating = "aoe,qhtncm"
    )
}

const val MIN_LENGTH = 10
const val MAX_LENGTH = 35

fun rateKeysmash(
    word: String,
    layout: KeyboardLayout,
    debug: Boolean = false,
): Double {
    val dictScore = if(word.replace("[^A-Za-z]+".toRegex(), "").lowercase() in blacklistSet) {
        0.001
    } else {
        1.0
    }
    val points = word.mapNotNull { char -> layout.lookup(char) }

    if(debug) {
        println("$word => $points")
    }

    val scoredDistances = layout.highPoints.mapNotNull { referencePoint ->
        val distances = points
            .map { point ->
                point.distanceSquared(referencePoint)
            }
            .filter { distanceSquared -> distanceSquared < 35 }
        if(debug) {
            println("$referencePoint -> ${distances.average().formatRounded()} $distances")
        }
        distances.takeUnless { it.isEmpty() }?.average()
    }
    val distanceScore = scoredDistances.average()

    val lengthScore = when(val l = word.length) {
        in 0 until MIN_LENGTH -> {
            val distance = abs(MIN_LENGTH - l)
            0.95.pow(distance).pow(2.0)
        }
        in MIN_LENGTH until MAX_LENGTH -> {
            1.0
        }
        in MAX_LENGTH until 96 -> {
            val distance = abs(l - MAX_LENGTH)
            0.99.pow(distance).pow(2.0)
        }
        else -> {
            val distance = abs(l - 96)
            0.8.pow(distance)
        }
    }
//    println("${word.length} => ${lengthScore.formatRounded()}  ${word}")

    return (distanceScore * lengthScore * dictScore) / 15.0
}

suspend fun rateList(
    layout: KeyboardLayout,
    wordList: List<String>,
    highest: Int = 5,
    lowest: Int = 3,
    debug: Boolean = false,
): Unit = coroutineScope {
    val results = wordList.chunked(10_000).map { chunk ->
        async {
            chunk.map { word ->
                val score = word.split(" ").map {
                    rateKeysmash(it, layout, debug = debug)
                }.average()
//                val score = rateKeysmash(word, layout, debug = debug)
                word to score
            }
        }
    }.awaitAll().flatten()

    val sorted = results.sortedByDescending { it.second }

    println("layout: ${layout.name}")
    println()

    if(highest > 0) {
        println("MOST keysmashy:")
        sorted.take(highest).forEach { (word, score) ->
            println("${score.formatRounded()} : $word")
        }
        println()
    }
    if(lowest > 0) {
        println("LEAST keysmashy:")
        sorted.takeLast(lowest).reversed().forEach { (word, score) ->
            println("${score.formatRounded()} : $word")
        }
        println()
    }
}

fun main(args: Array<String>): Unit = runBlocking {
    val input = """
        jsjsjsjsjsjsjsjsjsjsjsjs
        dlk`ajksa`jkfskl`dsla`fjksla:DJKSa' dskALDJsklaJKDs sKLDJslaDJslaDsl
        sa`fdsfedEA`DWE`DEA`D`EDWDADECS
        kljmo;mklmkliko;mkl.m,.jkmllklm
        xklsda`dksaldklafjdkjlasDKLS;djksl
        Hello World, how are you doing
        dskljdakldakldfkadkadk
        ahahahahahahah
        hhhhhhhhhhhhha
        qazsedcftgbhujm
        slkdsalkdskldsl;ksdakl;dsakl;dsaklsda
        sresreisreisreisreiseisriosriesreisrier
        look
        poi
        lollipop
        apollo
        cook
        lip
        pokemon
        meo
        moe
        meow
        looney
        oily
    """.trimIndent()

    val layouts: List<KeyboardLayout> = listOf(
        KeyboardLayout.QWERTY,
        KeyboardLayout.COLEMAK,
        KeyboardLayout.DVORAK,
    )

    layouts.forEach { layout ->
        rateList(
            layout = layout,
            wordList = input.lines(),
            highest = input.lines().size,
            lowest = 0,
//            debug = true
        )
    }
}

fun Double.formatRounded(digits: Int = 2) = String.format("%.${digits}f", this)
