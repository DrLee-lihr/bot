package net.drleelihr.bot.command

import net.drleelihr.bot.lib.endl
import net.drleelihr.bot.lib.httpRequest
import net.drleelihr.bot.lib.maimai.MaiSongList
import net.drleelihr.bot.lib.reply
import net.drleelihr.bot.lib.send
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChainBuilder
import org.json.JSONObject

suspend fun maiSongAlias(event: GroupMessageEvent, content: String) {
    val songList = MaiSongList()
    val alias = content.substring(0, content.length - 4)
    val result = JSONObject(httpRequest("https://maimai.ohara-rinne.tech/api/alias/query/$alias"))
    val data = result.getJSONArray("data")
    if (data.length() == 0) {
        reply(event, "没有找到您想找的乐曲。")
    } else {
        for (i in 0 until data.length()) {
            val temp = data.getJSONObject(i)
            if (temp.getString("alias").lowercase() == alias.lowercase()) {
                val song = songList.id(temp.getInt("musicId"))
                send(
                    event, MessageChainBuilder()
                        .append("您要找的是不是：\n")
                        .append(song.songInfoSummary.endl())
                        .append(song.getImageFileAsMessage(event))
                        .build()
                )
                return
            }
        }
        if (data.length() >= 4) {
            reply(event, "错误：结果过于宽泛，请尝试使用更准确更大众的别名进行搜索。")
            return
        } else {
            var resultString = "您要找的可能是以下歌曲：\n"
            for (i in data.toList().indices) {
                resultString += songList.id(
                    data.getJSONObject(i)
                        .getInt("musicId")
                ).songInfoSummary.endl()
            }
            reply(event, resultString)
        }
    }
}
