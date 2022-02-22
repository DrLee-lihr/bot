package net.drleelihr.bot.command

import net.drleelihr.bot.downloadImage
import net.drleelihr.bot.projectPath
import net.drleelihr.bot.send
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

infix fun String.containedBy(father:String):Boolean = father.lowercase().contains(this.lowercase())
fun String.r()=this.reversed()


/**
 * maisongRandom
 *
 * 语法：随个【（某某曲师）的】【（某某谱师）写的】【难度名称（红紫白等等）]】<定数/难度级别>
 *
 * 例：随个t+p的@dp写的紫14+
 *
 * 例2：随个owl*tree的歌
 *
 * 例3：随个红13.5
 */
suspend fun maisongRandom(event: GroupMessageEvent, regexList:MutableList<String>){
    random(event,regexList[0])
}

private suspend fun random(event: GroupMessageEvent, command:String) {
    println("成功进入查询函数")
    val songDataCache = File("$projectPath\\cache\\songDataCache.json")
    val songData = JSONArray(songDataCache.readText())
    println("歌曲数据加载完成")
    var arguments = command.substring(2).reversed()
    var level = ""
    try {
        while (arguments[0] in listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '.', '+', '＋', '歌')) {
            level += arguments[0]
            arguments = arguments.drop(1)
        }
    } catch (_: Exception) {
    }
    level = level.reversed()
    if (level == "歌") level = ""
    println("歌曲定数分析完成:$level")

    val keyWords = listOf(
        "绿", "bsc", "basic", "bas", "黄", "adv", "advanced", "红", "exp", "expert",
        "紫", "mst", "master", "mas", "白", "rem", "re:master", "remaster"
    )
    var difficulty: Int? = null
    for (keyWord in keyWords) {
        if (arguments.startsWith(keyWord.r())) {
            difficulty = difficultyIDTransform(keyWord)
            arguments = arguments.substring(keyWord.length)
            break
        }
    }
    println("歌曲谱面颜色分析完成：$difficulty")

    var charter = ""
    var artist = ""
    if (arguments.startsWith("的写")) {
        arguments = arguments.substring(2)
        if (arguments.contains("的")) {
            charter = arguments.split("的")[0].r()
            artist = arguments.split("的")[1].r()
        } else charter = arguments.r()
    } else if (arguments.startsWith("的")) {
        arguments = arguments.substring(1)
        if (arguments.contains("的写")) {
            charter = arguments.split("的写")[1].r()
            artist = arguments.split("的写")[0].r()
        } else artist = arguments.r()
    }
    println(
        "谱师:$charter\n" +
                "曲师:$artist"
    )


    var songArray = JSONArray()
    //songArray.put(songData.getJSONObject(359))
    if (difficulty != null) {
        if (level.contains('.')) {
            val levelNum: Float = level.toFloat()
            for (i in 0 until songData.length()) {
                val song = songData.getJSONObject(i)
                try {
                    if ((if (level != "") (song.getJSONArray("ds").getFloat(difficulty) == levelNum) else true)
                        && artist containedBy song.getJSONObject("basic_info").getString("artist")
                        && charter containedBy song.getJSONArray("charts").getJSONObject(difficulty)
                            .getString("charter")
                    ) {
                        songArray.put(song)
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        } else {
            for (i in 0 until songData.length()) {
                val song = songData.getJSONObject(i)
                try {
                    if ((if (level != "") (song.getJSONArray("ds").getFloat(difficulty) inLevel level) else true)
                        && artist containedBy song.getJSONObject("basic_info").getString("artist")
                        && charter containedBy song.getJSONArray("charts").getJSONObject(difficulty)
                            .getString("charter")
                    ) {
                        songArray.put(song)
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        }
        var songResult: JSONObject
        try {
            songResult = songArray.getJSONObject((0 until songArray.length()).random())
        }
        catch (e:NoSuchElementException){
            send(event, "错误：没有满足条件的曲目。"+if(level in listOf("1+","1＋","2+","2＋","3+","3＋",
                    "4+","4＋","5+","5＋","6+","6＋",))
                "\n提示：一部分5级和6级曲目虽然定数小数位大于等于7，但仍标为5级和6级；" +
                        "1-4级则不存在定数小数位大于等于7的谱面；1+至6+这六个难度不存在；最低的带+的难度是7+。" else "")
            return
        }
        val resultHead1 = "从${songArray.length()}个满足条件的结果中随机："
        val resultHead = "${songResult.getString("id")}.${songResult.getString("title")}" +
                "(${songResult.getString("type")})\n"
        val resultDifficulty = "${difficultyFullLevelTransform(difficulty)} " +
                dxs2LevelTransform(songResult.getJSONArray("ds").getFloat(difficulty)) +
                " (${songResult.getJSONArray("ds").getFloat(difficulty)})\n"

        val songImageFile = File("$projectPath\\cache\\${songResult.getString("id")}.jpg")
        val songImage: Image =
            if (!songImageFile.exists())
                (downloadImage("https://www.diving-fish.com/covers/${songResult.getString("id")}.jpg", songImageFile)
                    .uploadAsImage(event.group))
            else (songImageFile.uploadAsImage(event.group))
        val messageChain = MessageChainBuilder()
            .append(resultHead1)
            .append(resultHead)
            .append(songImage)
            .append(resultDifficulty)
            .build()
        send(event, messageChain)
    }
    else {
        if (level.contains('.')) {
            val levelNum: Float = level.toFloat()
            for (i in 0 until songData.length()) {
                val song = songData.getJSONObject(i)
                try {
                    for (difficulty in 0 until 5) {
                        if ((if (level != "") (song.getJSONArray("ds").getFloat(difficulty) == levelNum) else true)
                            && artist containedBy song.getJSONObject("basic_info").getString("artist")
                            && charter containedBy song.getJSONArray("charts").getJSONObject(difficulty)
                                .getString("charter")
                        ) {
                            val jsonObj: JSONObject =
                                JSONObject("{\"difficulty\":$difficulty,\"song\":${song.toString()}}")
                            songArray.put(jsonObj)
                        }
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        } else {
            for (i in 0 until songData.length()) {
                val song = songData.getJSONObject(i)
                try {
                    for (difficulty in 0 until 5) {
                        if ((if (level != "") (song.getJSONArray("ds").getFloat(difficulty) inLevel level) else true)
                            && artist containedBy song.getJSONObject("basic_info").getString("artist")
                            && charter containedBy song.getJSONArray("charts").getJSONObject(difficulty)
                                .getString("charter")
                        ) {
                            val jsonObj: JSONObject =
                                JSONObject("{\"difficulty\":$difficulty,\"song\":${song.toString()}}")
                            songArray.put(jsonObj)
                        }
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        }

        var chartResult: JSONObject
        try{
            chartResult=songArray.getJSONObject((0 until songArray.length()).random())
        }
        catch (e:NoSuchElementException){
            send(event, "错误：没有满足条件的曲目。"+if(level in listOf("1+","1＋","2+","2＋","3+","3＋",
                    "4+","4＋","5+","5＋","6+","6＋",))
                "\n提示：一部分5级和6级曲目虽然定数小数位大于等于7，但仍标为5级和6级；" +
                        "1-4级则不存在定数小数位大于等于7的谱面；1+至6+这六个难度不存在；最低的带+的难度是7+。" else "")
            return
        }

        val difficulty: Int = chartResult.getInt("difficulty")
        val songResult = chartResult.getJSONObject("song")
        val resultHead1 = "从${songArray.length()}个满足条件的结果中随机："
        val resultHead = "${songResult.getString("id")}.${songResult.getString("title")}" +
                "(${songResult.getString("type")})\n"
        val resultDifficulty = "${difficultyFullLevelTransform(difficulty)} " +
                dxs2LevelTransform(songResult.getJSONArray("ds").getFloat(difficulty)) +
                " (${songResult.getJSONArray("ds").getFloat(difficulty)})\n"

        val songImageFile = File("$projectPath\\cache\\${songResult.getString("id")}.jpg")
        val songImage: Image =
            if (!songImageFile.exists())
                (downloadImage("https://www.diving-fish.com/covers/${songResult.getString("id")}.jpg", songImageFile)
                    .uploadAsImage(event.group))
            else (songImageFile.uploadAsImage(event.group))
        val messageChain = MessageChainBuilder()
            .append(resultHead1)
            .append(resultHead)
            .append(songImage)
            .append(resultDifficulty)
            .build()
        send(event, messageChain)
    }
}
