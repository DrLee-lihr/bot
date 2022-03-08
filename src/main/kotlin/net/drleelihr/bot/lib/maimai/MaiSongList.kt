package net.drleelihr.bot.lib.maimai

import net.drleelihr.bot.lib.httpRequest
import net.drleelihr.bot.projectPath
import org.json.JSONArray
import java.io.File
import java.time.Instant

class MaiSongList() {
    var totalSongNum:Int=0
    private var list:MutableList<MaiSong> = mutableListOf()
    var chartsList= mutableListOf<MaiSong.Chart>()

    init{
        val timeStampRun= File("$projectPath\\cache\\RunTime.txt")
        val lastRunTime= timeStampRun.readText().toLong()
        timeStampRun.delete()
        timeStampRun.writeText(Instant.now().epochSecond.toString())
        val songDataCache: File = File("$projectPath\\cache\\songDataCache.json")
        if(lastRunTime+86400< Instant.now().epochSecond){
            println("refreshing song data cache")
            songDataCache.delete()
            songDataCache.writeText(httpRequest("https://www.diving-fish.com/api/maimaidxprober/music_data")!!)
        }
        val songData= JSONArray(songDataCache.readText())
        totalSongNum=songData.length()
        for(i in 0 until totalSongNum){
            list+= MaiSong(songData.getJSONObject(i))
            chartsList+=list[i].charts
        }
    }
    fun id(id:Int): MaiSong {
        for(i in list)
            if(i.id==id)return i
        throw NoSuchElementException("没有id为${id}的曲目。")
    }
    val iterator=list.iterator()
}