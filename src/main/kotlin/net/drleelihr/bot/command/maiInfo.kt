package net.drleelihr.bot.command

import net.drleelihr.bot.lib.*
import net.drleelihr.bot.lib.maimai.MaiSongList
import net.drleelihr.bot.lib.maimai.difficultyIDTransform
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder


suspend fun maiInfo(event:GroupMessageEvent,commandContent:MutableList<String>){

    /*
     * 代码命名规则：
     * id -> 曲目id
     * difficulty -> 谱面颜色（绿黄红紫白）
     * level -> 难度等级（1-15）
     * base,ds -> 谱面定数（1.0-15.0）
     */

    val songList= MaiSongList()
    val totalSongNum=songList.totalSongNum

    when(commandContent[0]){

        "base" -> {
            var result=""
            val requireBase=commandContent[1].toFloat()
            for(song in songList.iterator){
                for(chart in song.charts)
                    try{
                        if(chart.ds==requireBase)
                        result+=chart.chartSummary.endl()
                    }
                    catch(e:Exception){ continue }
            }
            sendMessageOrForward(event,result,"定数为${commandContent[1]}的谱面")
        }

        "search" -> {
            try {
                for(index in 2 until commandContent.size)
                    commandContent[1]+=(" "+commandContent[index])
            }
            catch (_:Exception) {}
            var result=""
            var limit=0
            for(song in songList.iterator){
                if (commandContent[1] containedBy song.title||commandContent[1] containedBy song.artist){
                    result+=song.songInfoSummary.endl()
                    limit++
                }
                if(limit>100){
                    result="条目过多，请缩小查询范围（结果大于100条）"
                    break
                }
            }
            sendMessageOrForward(event,result,"有关“${commandContent[1]}”的曲目")
        }

        "bpm" -> {
            commandContent.add(commandContent[1])
            var result=""
            if(commandContent.size<3)
                for(song in songList.iterator){
                    if (song.bpm==commandContent[1].toInt()){
                        result+=song.songInfoSummary.endl()
                    }
                }
            else {
                var limit=0
                if(commandContent.size<=2)commandContent.add(commandContent[1])
                for (song in songList.iterator) {
                    if (song.bpm in
                            commandContent[1].toInt()..commandContent[2].toInt()) {
                        result += song.songInfoSummary.endl()
                        limit++
                    }
                    if(limit>100){
                        result="条目过多，请缩小查询范围（结果大于100条）"
                        break
                    }
                }
            }
            sendMessageOrForward(event,result,if(commandContent[1]==commandContent[2])"BPM为${commandContent[1]}的曲目"
                                else "BPM介于${commandContent[1]}和${commandContent[2]}的曲目")
        }

        "charter" -> {
            var result=""
            try {
                for(index in 2 until commandContent.size)
                    commandContent[1]+=(" "+commandContent[index])
            }
            catch (_:Exception) {}
            for(song in songList.iterator){
                for(chart in song.charts){
                    try{
                        if(commandContent[1] containedBy chart.charter){
                            result+=chart.chartSummaryWithBase.endl()
                        }
                    }
                    catch(e:Exception){ continue }
                }
            }
            sendMessageOrForward(event,result,"谱师为${commandContent[1]}的谱面")
        }

        else -> {

            commandContent.add(commandContent.size,"")
            val difficultyID= difficultyIDTransform(commandContent[1])
            val song=songList.id(commandContent[0].toInt())
            val songImage=song.getImageFileAsMessage(event)

            var msgChain:MessageChain=
                if(difficultyID==-1){
                    MessageChainBuilder()
                        .append(song.songInfoSummary.endl())
                        .append(songImage)
                        .append(song.songDifficultSummary.endl())
                        .append("""
                        artist: ${song.artist}
                        genre: ${song.genre}
                        BPM: ${song.bpm}
                        version: ${song.version}
                    """.trimIndent())
                        .build()
                }

                else{
                    MessageChainBuilder()
                        .append(song.songInfoSummary.endl())
                        .append(songImage)
                        .append(song.charts[difficultyID].chartBaseSummary.endl())
                        .append(song.charts[difficultyID].chartNotesInfo.endl())
                        .append(song.charts[difficultyID].getProbeData().chartProbeSummary.endl())
                        .build()
                }
            send(event,msgChain)
        }
    }
}