package net.drleelihr.bot

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.messageChainOf

suspend fun reply(event:GroupMessageEvent,content:String) {
    if(content.length>=contentLengthLimit) {
        send(event,content)
    }
    else event.group.sendMessage(messageChainOf(QuoteReply(event.message)+content))
}