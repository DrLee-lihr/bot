package net.drleelihr.bot.command


import net.drleelihr.bot.lib.httpRequest
import net.drleelihr.bot.lib.reply
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.nextEvent
import net.mamoe.mirai.message.data.content
import org.json.JSONArray
import org.json.JSONObject

val confirmMessages = listOf("y", "yes", "是", "对")

suspend fun wiki(event: GroupMessageEvent, commandContent: MutableList<String>) {
    val interwikis: Map<String, String> = mapOf(
        Pair("mc", "https://minecraft.fandom.com/zh/"),
        Pair("moe", "https://zh.moegirl.org.cn/"),
        Pair("maimai", "https://maimai.fandom.com/zh/"),
        Pair("tr", "https://terraria.fandom.com/zh/"),
    )
    var pageName = commandContent[0]
    var requestSite = interwikis["mc"].toString()
    var interwiki = "mc"
    for (i in interwikis.keys) {
        if (pageName.startsWith("$i:")) {
            interwiki = i
            requestSite = interwikis[i].toString()
            pageName = pageName.substring(i.length + 1)
        }
    }
    val host = "${requestSite}api.php?action=query&prop=info|extracts&inprop=url&redirects" +
            "&exsentences=1&format=json&titles=${pageName}"
    var result: String? = httpRequest(host)
    val jsonObj = JSONObject(result)
    var pages = jsonObj.getJSONObject("query").getJSONObject("pages")
    try {
        pages.getJSONObject("-1")
    } catch (e: Exception) {
        println("page exist,$pages")
        var result = ""
        var extract = ""
        val pageID = pages.keys().next()
        pages = pages.getJSONObject(pageID)
        if ("extract" !in pages.keySet()) {
            result += "(no TextExtract)\n"
        } else {
            extract = pages.getString("extract")
            extract = Regex("<.*?>").replace(extract, "")
            extract = Regex("\n").replace(extract, "")
        }
        if (pages.getString("title") != pageName) {
            result += "（重定向[${pageName}]至[${pages.getString("title")}]）\n"
        }
        result += pages.getString("fullurl") + "\n"
        result += extract
        reply(event, result)
        return
    }
    try {
        val searchJson =
            JSONArray(
                httpRequest(
                    "${requestSite}api.php?action=opensearch&format=json&namespace=*&limit=10&redirects=resolve&search=" +
                            if (pageName.contains(":")) pageName else ":$pageName"
                )
            )

        val message = reply(
            event, "未找到页面，您要找的是不是[${searchJson.getJSONArray(1).getString(0)}]？\n" +
                    "（请发送y或其他表示确认的词语来确认）"
        )
        val event = nextEvent<GroupMessageEvent>(100000) { it.group == event.group && it.sender == event.sender }

        try {
            if (event.message.content.lowercase() in confirmMessages) {
                message.recall()
                wiki(event, mutableListOf(interwiki + ":" + searchJson.getJSONArray(1).getString(0)))
            } else message.recall()
        } catch (_: Exception) {
            message.recall()
        }
    } catch (_: Exception) {
        reply(event, "未找到页面。")
    }

}