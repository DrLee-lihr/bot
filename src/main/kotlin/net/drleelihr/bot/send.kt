package net.drleelihr.bot

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*

const val contentLengthLimit:Int=250


class SongListDisplayStrategy(private val title: String) : ForwardMessage.DisplayStrategy {

    override fun generateTitle(forward: RawForwardMessage): String = "${title}的曲目（谱面）"

    override fun generateBrief(forward: RawForwardMessage): String = "[曲目列表]"

    override fun generateSummary(forward: RawForwardMessage): String = "查看${forward.nodeList.size}首曲目（谱面）"

}


suspend fun send(event:GroupMessageEvent,content:String,title:String="您请求"){
    if(content.length>=contentLengthLimit){
        val singleContent=content.split("\n")
        val bigContents= mutableListOf<String>()
        var bigContent=""
        for(i in singleContent.indices) {
            bigContent+="${singleContent[i]}\n"
            if(i%10==9){
                bigContents.add(bigContent)
                bigContent=""
            }
        }
        if(singleContent.size%10!=0)bigContents.add(bigContent)
        event.group.sendMessage(buildForwardMessage(event.group,SongListDisplayStrategy(title)) {
            for(singleBigContentIndex in bigContents.indices)add(event.bot, buildMessageChain {
                +(bigContents[singleBigContentIndex] +
                        "共${singleContent.size/10+(if(singleContent.size%10==0)0 else 1)}页，第${singleBigContentIndex+1}页")
            })
        })
    }
    else event.group.sendMessage(content)
}

suspend fun send(event:GroupMessageEvent,content:MessageChain){
    event.group.sendMessage(content)
}