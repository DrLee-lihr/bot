package net.drleelihr.bot

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildForwardMessage

const val contentLengthLimit:Int=250

suspend fun send(event:GroupMessageEvent,content:String){
    if(content.length>=contentLengthLimit){
        val forward: ForwardMessage = buildForwardMessage(event.group) {
            3538158187L named "DrLeeBot" says content
        }
        event.group.sendMessage(forward)
    }
    else event.group.sendMessage(content)
}

suspend fun send(event:GroupMessageEvent,content:MessageChain){
    if(content.serializeToMiraiCode().length>=contentLengthLimit+30){
        val forward: ForwardMessage = buildForwardMessage(event.group) {
            3538158187L named "DrLeeBot" says content
        }
        event.group.sendMessage(forward)
    }
    else event.group.sendMessage(content)
}