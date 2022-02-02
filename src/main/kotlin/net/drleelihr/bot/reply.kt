package net.drleelihr.bot

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.messageChainOf

suspend fun reply(event:GroupMessageEvent,content:String) {
    event.group.sendMessage(messageChainOf(QuoteReply(event.message)+content))
}

suspend fun reply(event:GroupMessageEvent,content:MessageChain) {
    event.group.sendMessage(messageChainOf(QuoteReply(event.message)+content))
}