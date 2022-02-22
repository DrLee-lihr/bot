package net.drleelihr.bot

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*

const val contentLengthLimit:Int=250


class SongListDisplayStrategy(private val title: String) : ForwardMessage.DisplayStrategy {

    override fun generateTitle(forward: RawForwardMessage): String = "${title}的曲目（谱面）"

    override fun generateBrief(forward: RawForwardMessage): String = "[曲目列表]"

}

suspend fun sendMessageOrForward(event: GroupMessageEvent,content: String,title: String="您请求"): MessageReceipt<Group> {
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
        return event.group.sendMessage(buildForwardMessage(event.group,SongListDisplayStrategy(title)) {
            for(singleBigContentIndex in bigContents.indices)add(event.bot, buildMessageChain {
                +(bigContents[singleBigContentIndex] +
                        "共${singleContent.size/10+(if(singleContent.size%10==0)0 else 1)}页，第${singleBigContentIndex+1}页")
            })
        })
    }
    else return event.group.sendMessage(content)
}

suspend fun send(event:GroupMessageEvent,content:String): MessageReceipt<Group> {
    return event.group.sendMessage(content)
}

suspend fun send(event:GroupMessageEvent,content:MessageChain): MessageReceipt<Group> {
    return event.group.sendMessage(content)
}