/*
*  Copyright(C) DrLee_lihr 2020
*          Apache v2.0
**/
package net.drleelihr.bot


import net.drleelihr.bot.command.maisong
import net.drleelihr.bot.command.regexWiki
import net.drleelihr.bot.command.wiki
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.code.MiraiCode
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.buildForwardMessage
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.BotConfiguration
import java.io.File
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.Path

val projectPath: Path = Path("D:\\Dev\\bot")

var regexCommandList = mutableMapOf(
    Regex("\\[\\[.*?]]") to ::regexWiki,
)

var commandList = mutableMapOf(
    "wiki" to ::wiki,
    "maisong" to ::maisong,
    "m" to ::maisong,
)

suspend fun commandCheck(event:GroupMessageEvent){
    for(regex in regexCommandList.keys){
        if(regex.find(event.message.content)!=null) {
            regexCommandList[regex]?.let {
                var b: MutableList<String> = emptyList<String>().toMutableList()
                for (c in regex.findAll(event.message.content).toMutableList()) {
                    b.add(0, c.value)
                }
                it(event, b)
            }
        }
    }
    if(event.message.content.startsWith("!")||event.message.content.startsWith("！")) {
        val commandContent=event.message.content.substring(1).split(" ").toMutableList()
        if (commandContent[0] in commandList.keys) {
            val commandName=commandContent[0]
            commandContent.removeAt(0)
            commandList[commandName]?.let { it(event,commandContent) }
        }
    }
}

suspend fun main() {
    val qqId = 3538158187L
    val password = readLine()!!
    val bot = BotFactory.newBot(qqId, password) {
        fileBasedDeviceInfo()
        protocol = BotConfiguration.MiraiProtocol.ANDROID_PHONE
    }.alsoLogin()
    val abuseBotGroup= bot.getGroup(921834695L)
    bot.eventChannel.subscribeAlways<GroupMessageEvent> { event -> commandCheck(event) }
}