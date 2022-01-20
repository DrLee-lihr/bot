/*
*  Copyright(C) DrLee_lihr 2020
*          Apache v2.0
**/
package net.drleelihr.bot


import io.ktor.utils.io.core.*
import net.drleelihr.bot.command.wiki
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.content


var regexCommandList = mutableMapOf(
    (Regex("\\[\\[.*?]]") to ::wiki),
)

var commandList = mutableMapOf(
    Pair("wiki",::wiki)
)

suspend fun commandCheck(event:GroupMessageEvent){
    if(event.message.content.startsWith("!?")) {
        val commandContent=event.message.content.substring(2).split(" ").toMutableList()
        if (commandContent[0] in commandList.keys) {
            val commandName=commandContent[0]
            commandContent.removeAt(0)
            commandList[commandName]?.let { it(event,commandContent) }
        }
    }
}

suspend fun main() {
    val qqId = 3538158187L//Bot的QQ号，需为Long类型，在结尾处添加大写L
    val password = readLine()!!
    val bot = BotFactory.newBot(qqId, password) { fileBasedDeviceInfo() }.alsoLogin()//新建Bot并登录
    val sevenClassGroup=bot.getGroup(834714536)
    val abuseBotGroup=bot.getGroup(1044813316)
    bot.eventChannel.subscribeAlways<GroupMessageEvent> { event -> commandCheck(event) }
}