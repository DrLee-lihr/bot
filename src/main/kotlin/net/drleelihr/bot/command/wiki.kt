package net.drleelihr.bot.command


import net.drleelihr.bot.httpRequest
import net.drleelihr.bot.reply
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.json.JSONObject

suspend fun wiki(event: GroupMessageEvent, commandContent:MutableList<String>){
    val interWiki:Map<String,String> = mapOf(
        Pair("mc","https://minecraft.fandom.com/zh/"),
        Pair("moe","https://zh.moegirl.org.cn/"),
        Pair("maimai","https://maimai.fandom.com/zh/"),
        Pair("tr","https://terraria.fandom.com/zh/"),
    )
    var pageName = commandContent[0]
    var requestSite=interWiki["mc"].toString()
    for(i in interWiki.keys){
        if(pageName.startsWith(i)){
            requestSite = interWiki[i].toString()
            pageName = pageName.substring(i.length+1)
        }
    }
    val host = "${requestSite}api.php?action=query&prop=info|extracts&inprop=url&redirects" +
            "&exsentences=1&format=json&titles=${pageName}"
    var result:String?=null
    try { result=httpRequest(host) }
    catch (error:NullPointerException){
        reply(event,"发生错误：${error.message}")
        return
    }
    val jsonObj=JSONObject(result)
    var pages=jsonObj.getJSONObject("query").getJSONObject("pages")
    try{ val pagesNotExist=pages.getJSONObject("-1") }
    catch (e:Exception){
        println("page exist,$pages")
        var result=""
        var extract=""
        val pageID=pages.keys().next()
        pages=pages.getJSONObject(pageID)
        if("extract" !in pages.keySet()){
            result+="(no TextExtract)\n"
        }
        else{
            extract=pages.getString("extract")
            extract=Regex("<.*?>").replace(extract,"")
            extract=Regex("\n").replace(extract,"")
        }
        if(pages.getString("title")!=pageName){
            result+="（重定向[${pageName}]至[${pages.getString("title")}]）\n"
        }
        result+=pages.getString("fullurl")+"\n"
        result+=extract
        reply(event,result)
        return
    }
    reply(event,"未找到页面。")
}