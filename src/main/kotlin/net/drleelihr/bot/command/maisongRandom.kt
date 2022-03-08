package net.drleelihr.bot.command

import net.drleelihr.bot.lib.*
import net.drleelihr.bot.lib.maimai.MaiSong
import net.drleelihr.bot.lib.maimai.MaiSongList
import net.drleelihr.bot.lib.maimai.difficultyIDTransform
import net.drleelihr.bot.lib.maimai.inLevel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChainBuilder

infix fun String.containedBy(father:String):Boolean = father.lowercase().contains(this.lowercase())
fun String.r()=this.reversed()


/**
 * maiSongRandom
 *
 * 语法：随个【（某某曲师）的】【（某某谱师）写的】【难度名称（红紫白等等）]】<定数/难度级别>
 *
 * 例：随个t+p的@dp写的紫14+
 *
 * 例2：随个owl*tree的歌
 *
 * 例3：随个红13.5
 */
suspend fun maiSongRandom(event: GroupMessageEvent, content: String){
    println("成功进入查询函数")
    val songList= MaiSongList()
    val totalSongNum=songList.totalSongNum
    println("歌曲数据加载完成")


    var arguments = content.substring(2).reversed()

    val type:Int=
        when {
            "XD" containedBy arguments -> {
                var x=""
                arguments.split("XD").forEach{ a -> x+=a }
                arguments=x
                1
            }
            "DS" containedBy arguments || "准标" containedBy arguments -> {
                if("DS" containedBy arguments){
                    var x=""
                    arguments.split("DS").forEach{ a -> x+=a }
                    arguments=x
                } else{
                    var x=""
                    arguments.split("准标").forEach{ a -> x+=a }
                    arguments=x
                }
                0
            }
            else -> -1
        }

    println("歌曲版本判断完成：$type")

    var level = ""
    try {
        while (arguments[0] in listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '.', '+', '＋', '歌')) {
            level += arguments[0]
            arguments = arguments.drop(1)
        }
    } catch (_: Exception) {}
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
        arguments=""
    } else if (arguments.startsWith("的")) {
        arguments = arguments.substring(1)
        if (arguments.contains("的写")) {
            charter = arguments.split("的写")[1].r()
            artist = arguments.split("的写")[0].r()
        } else artist = arguments.r()
        arguments=""
    }
    println(
        "谱师:$charter\n" +
                "曲师:$artist"
    )
    println("剩余参数：$arguments")
    if (arguments!=""){
        reply(event,"错误：随机命令处理错误，请检查语法。")
    }


    var chartResultList:MutableList<MaiSong.Chart> = mutableListOf()
    for (chart in songList.chartsList) {
        if (artist containedBy chart.song.artist
            && charter containedBy chart.charter
            && (if (type != -1) type == chart.song.type else true)
            && when {
                level == "" -> true
                level.contains(".") -> chart.ds == level.toFloat()
                else -> chart.ds inLevel level
            }
            && (if(difficulty != null)difficulty == chart.difficulty else true)
        ) {
            chartResultList.add(chart)
        }
    }

    var result: MaiSong.Chart
    try {
        result=chartResultList.random()
    }
    catch (e:NoSuchElementException){
        send(event, "错误：没有满足条件的曲目。"
                + if(level in listOf("1+","1＋","2+","2＋","3+","3＋",
                "4+","4＋","5+","5＋","6+","6＋",))
            "\n提示：一部分5级和6级曲目虽然定数小数位大于等于7，但仍标为5级和6级；" +
                    "1-4级则不存在定数小数位大于等于7的谱面；1+至6+这六个难度不存在；最低的带+的难度是7+。" else "")
        return
    }
    val messageChain = MessageChainBuilder()
        .append("从${chartResultList.size}个满足条件的结果中随机：\n")
        .append(result.song.songInfoSummary.endl())
        .append(result.song.getImageFileAsMessage(event))
        .append(result.chartBaseSummary)
        .build()
    send(event, messageChain)
}