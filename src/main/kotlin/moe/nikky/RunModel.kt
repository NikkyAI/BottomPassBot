//package moe.nikky
//
//import io.klogging.logger
//import kotlinx.coroutines.CompletableDeferred
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Deferred
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.channels.Channel
//import kotlinx.coroutines.flow.receiveAsFlow
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.runBlocking
//import kotlinx.coroutines.withContext
//import okio.use
//import java.io.File
//
//fun convert(char: Char): Int {
//    return when (char) {
//        'd' -> 2
//        'f' -> 3
//        'j' -> 4
//        's' -> 5
//        'h' -> 6
//        'k' -> 7
//        'g' -> 8
//        'l' -> 9
//        'a' -> 10
//        'i' -> 11
//        'n' -> 12
//        'o' -> 13
//        'u' -> 14
//        'b' -> 15
//        'e' -> 16
//        ';' -> 17
//        'r' -> 18
//        'w' -> 19
//        'c' -> 20
//        'v' -> 21
//        'p' -> 22
//        ' ' -> 23
//        't' -> 24
//        'y' -> 25
//        'm' -> 26
//        ',' -> 27
//        'z' -> 28
//        '\'' -> 29
//        'q' -> 30
//        '0' -> 31
//        '9' -> 32
//        'x' -> 33
//        '.' -> 34
//        ']' -> 35
//        '[' -> 36
//        '/' -> 37
//        ':' -> 38
//        '-' -> 39
//        '#' -> 40
//        '>' -> 41
//        '7' -> 42
//        '\\' -> 43
//        '2' -> 44
//        '4' -> 45
//        '\u00e1' -> 46
//        '\u2018' -> 47
//        '<' -> 48
//        '?' -> 49
//        '!' -> 50
//        '8' -> 51
//        '=' -> 52
//        '5' -> 53
//        '6' -> 54
//        '3' -> 55
//        ')' -> 56
//        else -> 1
//    }
//}
//
//typealias ModelFun = suspend (String) -> Deferred<Pair<Float, String>>
//
//suspend fun CoroutineScope.runModel(instances: Int = 1): ModelFun {
//    val logger = logger("Main")
//    val results = mutableMapOf<String, CompletableDeferred<Pair<Float, String>>>()
//    val inputChannel = Channel<String>()
//    repeat(instances.toInt()) {
//        launch {
//            ProcessBuilder()
//                .command("node", "model_executor/index.mjs")
//                .directory(File("."))
//                .redirectInput(ProcessBuilder.Redirect.PIPE)
//                .redirectOutput(ProcessBuilder.Redirect.PIPE)
//                .start()
//                .also { process ->
//                    withContext(Dispatchers.IO) { // More info on this context switching : https://elizarov.medium.com/blocking-threads-suspending-coroutines-d33e11bf4761
//                        launch {
//                            process.outputWriter().use { stdin ->
//                                inputChannel.receiveAsFlow().collect { line ->
//                                    logger.trace("sending: {line}", line)
//                                    stdin.write(line)
//                                    stdin.newLine()
//                                    stdin.flush()
//                                }
//                            }
//                        }
//                        launch {
//                            process.inputStream.bufferedReader().let { stdout ->
//                                while (true) { // Breaks when readLine returns null
//                                    stdout.readLine()?.also { line ->
//                                        logger.trace("received: {line}", line)
//                                        if (line.startsWith("RESULT")) {
//                                            val withoutPrefix = line.substringAfter("RESULT ")
//                                            val result = withoutPrefix
//                                                .substringBefore(" ")
//                                                .toFloat()
//                                            val key = withoutPrefix.substringAfter(" ")
//                                            results[key]?.let { deferred ->
//                                                deferred.complete(result to key)
//                                            }
//                                            results.remove(key)
//                                        }
////                                        logger.trace(line) // realtime logging
////                                        logs += "$line\n" // record
//                                    } ?: break
//                                }
//                                logger.trace("output ended")
//                            }
//                        }
//
//                        process.waitFor()
//                        if (process.isAlive) {
////                        logs += "TIMEOUT occurred".also { logger.warn(it) } + "\n"
//                            logger.warn("timeout occured?")
//                            process.destroy()
//                        }
//                    }
//                }
//        }
//    }
//
//    suspend fun call(input: String): Deferred<Pair<Float, String>> {
//        val deferred = CompletableDeferred<Pair<Float, String>>().also { deferred ->
//            val sizedInput = input.take(96)
//            results[sizedInput] = deferred
//            if (results.size > 20) {
//                logger.warn("result queue size: {size}", results.size)
//            } else {
//                logger.trace("result queue size: {size}", results.size)
//            }
//            inputChannel.send(sizedInput)
//        }
//        return deferred
//    }
//
//    return ::call
//}
//
//fun main(args: Array<String>) {
//    runBlocking {
//
//        val model = runModel()
//
//        model.invoke("SDSADAFDASAD").await().let {
//            println("result: $it")
//        }
//        model.invoke("fdsafdafdsf").await().let {
//            println("result: $it")
//        }
//    }
//
////    SavedModelBundle.load("model_out/", "serve").use {
////        println(it)
////    }
//
//
////    val input = "freshtrsegrewafeaghtrshgrea"
////    val inputConverted = input.map { convert(it).toFloat() }.toFloatArray()
////
////
////    val modelConfig = File("model_out/model.json")
////    val weights = File("model_out/weights")
////
////    val model = Sequential.loadModelConfiguration(modelConfig)
////
////    model.use {
////        it.compile(Adam(), Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS, Metrics.ACCURACY)
////
////        it.loadWeights(HdfFile(weights))
////
////        val prediction = it.predict(inputConverted)
////        println("Predicted label is: $prediction. This corresponds to class ${prediction}.")
////    }
//}