package net.drleelihr.bot.command

import net.drleelihr.bot.downloadImage
import net.drleelihr.bot.httpRequest
import net.drleelihr.bot.projectPath
import net.drleelihr.bot.send
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.json.JSONArray
import java.io.File


suspend fun maisong(event:GroupMessageEvent,commandContent:MutableList<String>){
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
    val difficultyIDTransform= { a: String ->
        when (a.lowercase()) {
            "绿","bsc","basic" -> 0
            "黄","adv","advanced" -> 1
            "红","exp","expert" -> 2
            "紫","mst","master" -> 3
            "白","rem","re:master","remaster" -> 4
            else -> -1
        }
    }
    val difficultyStandardize= { a: String ->
        difficultyLevelTransform(difficultyIDTransform(a))
    }
    val songData=JSONArray(httpRequest("https://www.diving-fish.com/api/maimaidxprober/music_data"))
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
                        result+="${song.getString("id")}.${song.getString("title")}<${song.getString("type")}>" +
                                "[${difficultyLevelTransform(chartIndex)}]\n"
                    }
                    catch(e:Exception){ continue }
            }
            send(event,result)
        }
        "search" -> {
            var result:String=""
            var limit:Int=0
            for(index in (0 until totalSongNum)){
                val song=songData.getJSONObject(index)
                if (song.getString("title").lowercase().contains(commandContent[1].lowercase())||
                        song.getJSONObject("basic_info").getString("artist").lowercase()
                            .contains(commandContent[1].lowercase())){
                    result+="${song.getString("id")}.${song.getString("title")}<${song.getString("type")}>\n"
                    limit++
                }
                if(limit>=50){
                    result="条目过多，请缩小查询范围（结果大于50条）"
                    break
                }
            }
            send(event,result)
        }
        "bpm" -> {
            var result:String=""
            if(commandContent.size<3)
                for(index in (0 until totalSongNum)){
                    val song=songData.getJSONObject(index)
                    if (song.getJSONObject("basic_info").getInt("bpm")==commandContent[1].toInt()){
                        result+="${song.getString("id")}.${song.getString("title")}<${song.getString("type")}>\n"
                    }
                }
            else {
                var limit:Int=0
                for (index in (0 until totalSongNum)) {
                    val song = songData.getJSONObject(index)
                    if (song.getJSONObject("basic_info").getInt("bpm") in
                            commandContent[1].toInt() until commandContent[2].toInt()+1) {
                        result += "${song.getString("id")}.${song.getString("title")}<${song.getString("type")}>\n"
                        limit++
                    }
                    if(limit>=50){
                        result="条目过多，请缩小查询范围（结果大于50条）"
                        break
                    }
                }
            }
            send(event,result)
        }
        "charter" -> {
            var result:String=""
            for(index in 0 until totalSongNum){
                val song=songData.getJSONObject(index)
                val songCharts=song.getJSONArray("charts")
                for(levelID in 0 until 5){
                    try{
                        if(songCharts.getJSONObject(levelID).getString("charter").lowercase()
                                .contains(commandContent[1].lowercase())){
                            result+="${song.getString("id")}.${song.getString("title")}<${song.getString("type")}>" +
                                    "[${difficultyLevelTransform(levelID)}](${song.getJSONArray("ds").getFloat(levelID)})\n"
                        }
                    }
                    catch(e:Exception){
                        continue
                    }
                }
            }
            send(event,result)
        }
        else -> {
            commandContent.add(commandContent.size,"")
            var result:String=""
            var resultHead:String=""
            var difficultyID=difficultyIDTransform(commandContent[1])
            var song=songData.getJSONObject(359)//你好，这是我最爱的监狱
            for(index in (0 until totalSongNum)){
                if(songData.getJSONObject(index).getInt("id")==commandContent[0].toInt()){
                    song=songData.getJSONObject(index)
                }
            }
            var songImageFile:File=File("$projectPath\\cache\\${song.getString("id")}.jpg")
            var songImage: Image =
                if(!songImageFile.exists())
                        (downloadImage("https://www.diving-fish.com/covers/${song.getString("id")}.jpg",songImageFile)
                        .uploadAsImage(event.group,"jpg"))
                else (songImageFile.uploadAsImage(event.group,"jpg"))
            val basicInfo=song.getJSONObject("basic_info")
            if(difficultyID==-1){
                resultHead="${song.getString("id")}.${song.getString("title")}<${song.getString("type")}>\n"
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
                resultHead="${song.getString("id")}.${song.getString("title")}<${song.getString("type")}>" +
                        "[${difficultyStandardize(commandContent[1])}]\n"
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
            val messageChain=MessageChainBuilder()
                    .append(resultHead)
                    .append(songImage)
                    .append(result)
                    .build()
            send(event,messageChain)
        }
    }
}