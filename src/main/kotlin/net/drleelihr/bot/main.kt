/*
*  Copyright(C) DrLee_lihr 2020
*          Apache v2.0
**/
package net.drleelihr.bot


import net.drleelihr.bot.command.*
import net.drleelihr.bot.lib.reply
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.BotConfiguration
import java.nio.file.Path
import kotlin.io.path.Path

var enableCatchException=false

val projectPath: Path = Path("D:\\Dev\\bot")
const val resourcePath="D:\\Dev\\bot\\src\\main\\resources"

var regexCommandList = mutableMapOf(
    Regex("\\[\\[.*?]]") to ::regexWiki,
)

var fullRegexCommandList = mutableMapOf(
    Regex("随个.*") to ::maiSongRandom,
    Regex(".*是什么歌") to ::maiSongAlias,
    Regex("b40.*") to ::b40
)

var commandList = mutableMapOf(
    "wiki" to ::wiki,
    "maisong" to ::maiInfo,
    "m" to ::maiInfo,
)

suspend fun commandCheck(event:GroupMessageEvent){
    try{
        for(regex in regexCommandList.keys){
            if(regex.find(event.message.content)!=null) {
                regexCommandList[regex]?.let {
                    var b: MutableList<String> = mutableListOf()
                    for (c in regex.findAll(event.message.content).toMutableList()) {
                        b.add(c.value)
                    }
                    it(event, b)
                }
            }
        }
        for(regex in fullRegexCommandList.keys){
            if(regex.matches(event.message.content)){
                fullRegexCommandList[regex]!!(event,event.message.content)
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
    catch(e:Exception){
        if(enableCatchException) reply(event,"指令在执行过程中出错：\n${e.message}")
        else throw e
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