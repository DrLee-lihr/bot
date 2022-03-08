package net.drleelihr.bot.command

import net.drleelihr.bot.lib.maimai.MaiB40Result
import net.drleelihr.bot.lib.reply
import net.drleelihr.bot.lib.send
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage


suspend fun b40(event: GroupMessageEvent,content:String) {
    send(event,"处理时间较长，请耐心等待……")
    var maiB40Result: MaiB40Result?=null
    try{
        maiB40Result =
            if (content.length <= 4) MaiB40Result(event.sender.id)
            else MaiB40Result(content.substring(4))
    }
    catch (e:Exception){
        reply(event, e.message!!)
        return
    }
    reply(event,MessageChainBuilder().append(maiB40Result!!.drawB40Image().uploadAsImage(event.group)).build())
}