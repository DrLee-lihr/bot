package net.drleelihr.bot.command

import net.mamoe.mirai.event.events.GroupMessageEvent

/***
 * 正则快捷查询wiki
 * @see net.drleelihr.bot.command.wiki
 */
suspend fun regexWiki(event:GroupMessageEvent,regexList:MutableList<String>){
    for(pageName in regexList){
        wiki(event, mutableListOf(pageName.substring(2,pageName.length-2)))
    }
}