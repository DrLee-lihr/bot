package net.drleelihr.bot.command

import net.drleelihr.bot.downloadImage
import net.drleelihr.bot.httpRequest
import net.drleelihr.bot.projectPath
import net.drleelihr.bot.send
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.json.JSONArray
import org.w3c.dom.ranges.Range
import java.io.File
import java.time.Instant
import kotlin.math.floor
import kotlin.time.measureTimedValue

val difficultyLevelTransform= { a: Int ->
    when (a) {
        0 -> "BSC"
        1 -> "ADV"
        2 -> "EXP"
        3 -> "MST"
        4 -> "ReM"
        else -> "Original"
    }
}
val difficultyFullLevelTransform= { a: Int ->
    when (a) {
        0 -> "Basic"
        1 -> "Advanced"
        2 -> "Expert"
        3 -> "Master"
        4 -> "Re:Master"
        else -> "Original"
    }
}
val dxs2LevelTransform: (Float) -> String = { "${floor(it).toInt()}${if(it-floor(it)>=0.65)"+" else ""}" }
infix fun Float.inLevel(s:String) : Boolean  {
    val baseLevel:Int=s.split("+","＋")[0].toInt()
    return if(baseLevel<=6&&(s.contains("+")||s.contains("＋"))) false
    else if(s.contains("+")||s.contains("＋")) (this>=baseLevel+0.65&&this<=baseLevel+0.95)
    else (this>=baseLevel-0.05&&this<=baseLevel+0.65)
}
val difficultyIDTransform= { a: String ->
    when (a.lowercase()) {
        "绿","bsc","basic","bas" -> 0
        "黄","adv","advanced" -> 1
        "红","exp","expert" -> 2
        "紫","mst","master","mas" -> 3
        "白","rem","re:master","remaster" -> 4
        else -> -1
    }
}



suspend fun maisong(event:GroupMessageEvent,commandContent:MutableList<String>){

    var timeStampRun:File=File("$projectPath\\cache\\RunTime.txt")
    var lastRunTime= timeStampRun.readText().toLong()
    timeStampRun.delete()
    timeStampRun.writeText(Instant.now().epochSecond.toString())
    var songDataCache:File=File("$projectPath\\cache\\songDataCache.json")
    if(lastRunTime+86400<Instant.now().epochSecond){
        println("refreshing song data cache")
        songDataCache.delete()
        songDataCache.writeText(httpRequest("https://www.diving-fish.com/api/maimaidxprober/music_data")!!)
    }
    val songData=JSONArray(songDataCache.readText())
    val totalSongNum=songData.length()


    when(commandContent[0]){

        "base" -> {
            var result=""
            val requireBase=commandContent[1].toFloat()
            for(index in (0 until totalSongNum)){
                val song=songData.getJSONObject(index)
                val base=song.getJSONArray("ds")
                for(chartIndex in 0 until 5)
                    try{
                        if(base.getFloat(chartIndex)==requireBase)
                        result+="${song.getString("id")}.${song.getString("title")}(${song.getString("type")})" +
                                "[${difficultyLevelTransform(chartIndex)}]\n"
                    }
                    catch(e:Exception){ continue }
            }
            send(event,result,"定数为${commandContent[1]}")
        }

        "search" -> {
            try {
                for(index in 2 until commandContent.size)
                    commandContent[1]+=(" "+commandContent[index])
            }
            catch (e:Exception) {}
            var result:String=""
            var limit:Int=0
            for(index in (0 until totalSongNum)){
                val song=songData.getJSONObject(index)
                if (song.getString("title").lowercase().contains(commandContent[1].lowercase())||
                        song.getJSONObject("basic_info").getString("artist").lowercase()
                            .contains(commandContent[1].lowercase())){
                    result+="${song.getString("id")}.${song.getString("title")}(${song.getString("type")})\n"
                    limit++
                }
                if(limit>100){
                    result="条目过多，请缩小查询范围（结果大于100条）"
                    break
                }
            }
            send(event,result,"有关“${commandContent[1]}”")
        }

        "bpm" -> {
            var result:String=""
            if(commandContent.size<3)
                for(index in (0 until totalSongNum)){
                    val song=songData.getJSONObject(index)
                    if (song.getJSONObject("basic_info").getInt("bpm")==commandContent[1].toInt()){
                        result+="${song.getString("id")}.${song.getString("title")}(${song.getString("type")})\n"
                    }
                }

            else {
                var limit:Int=0
                if(commandContent.size<=2)commandContent.add(commandContent[1])
                for (index in (0 until totalSongNum)) {
                    val song = songData.getJSONObject(index)
                    if (song.getJSONObject("basic_info").getInt("bpm") in
                            commandContent[1].toInt()..commandContent[2].toInt()) {
                        result += "${song.getString("id")}.${song.getString("title")}(${song.getString("type")})\n"
                        limit++
                    }
                    if(limit>100){
                        result="条目过多，请缩小查询范围（结果大于100条）"
                        break
                    }
                }
            }
            send(event,result,if(commandContent[1]==commandContent[2])"BPM为${commandContent[1]}"
                                else "BPM介于${commandContent[1]}和${commandContent[2]}")
        }

        "charter" -> {
            var result:String=""
            try {
                for(index in 2 until commandContent.size)
                    commandContent[1]+=(" "+commandContent[index])
            }
            catch (e:Exception) {}
            for(index in 0 until totalSongNum){
                val song=songData.getJSONObject(index)
                val songCharts=song.getJSONArray("charts")
                for(levelID in 0 until 5){
                    try{
                        if(songCharts.getJSONObject(levelID).getString("charter").lowercase()
                                .contains(commandContent[1].lowercase())){
                            result+="${song.getString("id")}.${song.getString("title")}(${song.getString("type")})" +
                                    "[${difficultyLevelTransform(levelID)}](${song.getJSONArray("ds").getFloat(levelID)})\n"
                        }
                    }
                    catch(e:Exception){ continue }
                }
            }
            send(event,result,"谱师为${commandContent[1]}")
        }

        else -> {

            commandContent.add(commandContent.size,"")
            var result:String=""
            var resultDifficulty:String=""
            var resultHead:String=""
            val difficultyID=difficultyIDTransform(commandContent[1])
            var song=songData.getJSONObject(359)//你好，这是我最爱的监狱
            var isValidID=false

            for(index in (0 until totalSongNum)){
                if(songData.getJSONObject(index).getInt("id")==commandContent[0].toInt()){
                    song=songData.getJSONObject(index)
                    isValidID=true
                }
            }

            val songImageFile:File=File("$projectPath\\cache\\${song.getString("id")}.jpg")
            val songImage: Image =
                if(!songImageFile.exists())
                        (downloadImage("https://www.diving-fish.com/covers/${song.getString("id")}.jpg",songImageFile)
                        .uploadAsImage(event.group))
                else (songImageFile.uploadAsImage(event.group))

            val basicInfo=song.getJSONObject("basic_info")
            val dxsInfo=song.getJSONArray("ds")

            if(difficultyID==-1){
                resultHead="${song.getString("id")}.${song.getString("title")}(${song.getString("type")})\n"
                resultDifficulty="${dxsInfo.getFloat(0)}/${dxsInfo.getFloat(1)}/${dxsInfo.getFloat(2)}/" +
                        "${dxsInfo.getFloat(3)}${if(dxsInfo.length()==5)"/${dxsInfo.getFloat(4)}" else ""}\n"
                result="""
                    artist: ${basicInfo.getString("artist")}
                    genre: ${basicInfo.getString("genre")}
                    BPM: ${basicInfo.getInt("bpm")}
                    version: ${basicInfo.getString("from")}
                """.trimIndent()
            }

            else{
                val chartInfo=song.getJSONArray("charts").getJSONObject(difficultyIDTransform(commandContent[1]))
                val noteInfo=chartInfo.getJSONArray("notes")
                resultHead="${song.getString("id")}.${song.getString("title")}(${song.getString("type")})\n"
                resultDifficulty="${difficultyFullLevelTransform(difficultyIDTransform(commandContent[1]))} " +
                        dxs2LevelTransform(song.getJSONArray("ds").getFloat(difficultyIDTransform(commandContent[1]))) +
                        " (${song.getJSONArray("ds").getFloat(difficultyIDTransform(commandContent[1]))})\n"
                result=if(song.getString("type")=="DX")
                    """
                        TAP: ${noteInfo.getInt(0)}
                        HOLD: ${noteInfo.getInt(1)}
                        SLIDE: ${noteInfo.getInt(2)}
                        TOUCH: ${noteInfo.getInt(3)}
                        BREAK: ${noteInfo.getInt(4)}
                        charter: ${chartInfo.getString("charter")}
                    """.trimIndent()
                else
                    """
                        TAP: ${noteInfo.getInt(0)}
                        HOLD: ${noteInfo.getInt(1)}
                        SLIDE: ${noteInfo.getInt(2)}
                        BREAK: ${noteInfo.getInt(3)}
                        charter: ${chartInfo.getString("charter")}
                    """.trimIndent()
            }
            if(!isValidID){
                result+="\n(id无效，自动返回监狱)"
            }

            val messageChain=MessageChainBuilder()
                    .append(resultHead)
                    .append(songImage)
                    .append(resultDifficulty)
                    .append(result)
                    .build()
            send(event,messageChain)
        }
    }
}