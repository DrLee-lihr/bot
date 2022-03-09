package net.drleelihr.bot.lib.maimai

import net.drleelihr.bot.lib.GaussianBlurUtil
import net.drleelihr.bot.lib.resizeImage
import net.drleelihr.bot.lib.setLight
import net.drleelihr.bot.projectPath
import net.drleelihr.bot.resourcePath
import net.mamoe.mirai.contact.PermissionDeniedException
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.awt.Color
import java.awt.Font
import java.awt.Image
import java.io.File
import javax.imageio.ImageIO


class MaiB40Result {

    var rating:Int=0
    var additionalRating:Int=0
    var nickname=""
    var plate=""
    var username=""
    var dxSongResultList:MutableList<MaiSongResult> = mutableListOf()
    var sdSongResultList:MutableList<MaiSongResult> = mutableListOf()


    inner class MaiSongResult(val json:JSONObject,val num:Int) {
        var id=0
        var achievements=0.0000F
        var ds=0.0F
        var difficultyIndex=0
        var rating=0
        var rate=""
        var type=""
        var song:MaiSong?=null
        var fc=""
        var fs=""

        init {
            val maiSongList=MaiSongList()
            id=json.getInt("song_id")
            song=maiSongList.id(id)
            achievements=json.getFloat("achievements")
            ds=json.getFloat("ds")
            difficultyIndex=json.getInt("level_index")
            rating=json.getInt("ra")
            rate=json.getString("rate")
            type=json.getString("type")
            fc=json.getString("fc")
            fs=json.getString("fs")
        }

        fun drawSong(): Image? {

            val bg = setLight(GaussianBlurUtil.blur(
                resizeImage(ImageIO.read(song!!.getImageFile()), 200, 200), 10),-80)
            val g = bg.createGraphics()

            g.font = Font("微软雅黑", Font.PLAIN, 25)
            g.color = Color.WHITE
            g.drawString(song!!.title, 10, 35)


            val rate = ImageIO.read(File("$resourcePath\\maimai\\${rate}.png"))
            val diff = ImageIO.read(File("$resourcePath\\maimai\\" +
                    "${difficultyFullLevelTransform(difficultyIndex).replace(":","").lowercase()}.png"))

            g.drawImage(diff,10,40,(diff.width*0.75).toInt(),(diff.height*0.75).toInt(),null)
            g.drawImage(rate, 10, 57, (73*0.75).toInt(),(49*0.75).toInt(),null)

            g.font=Font("Consolas",Font.PLAIN,20)
            g.drawString("%.4f".format(achievements)+"%",74,85)

            if(fc!=""){
                val file=ImageIO.read(File("$resourcePath\\maimai\\${fc}.png"))
                g.drawImage(file,10,90,file.width,file.height,null)
            }
            if(fs!=""){
                val file=ImageIO.read(File("$resourcePath\\maimai\\${fs}.png"))
                g.drawImage(file,70,90,file.width,file.height,null)
            }

            g.font=Font("Consolas",Font.PLAIN,17)
            g.drawString("Base:${"%.1f".format(ds)} -> $rating",10,155)

            g.font=Font("Consolas",Font.PLAIN,25)
            g.drawString("#${num+1} (${type})",10,190)

            return bg
        }
    }



    private fun getInfoFromProbe(requestBody:JSONObject): JSONObject {
        val mediaType: MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()

        val client= OkHttpClient()
        val request= Request.Builder()
            .url("https://www.diving-fish.com/api/maimaidxprober/query/player")
            .post(requestBody.toString().toRequestBody(mediaType))
            .build()
        val call=client.newCall(request)
        val response = call.execute()
        if(response.code==400){
            throw NoSuchElementException("未找到此玩家，请确保输入了正确的用户名或已在查分器绑定QQ号。")
        }
        if(response.code==403){
            throw PermissionDeniedException("该用户禁止了其他人获取数据。")
        }
        return JSONObject(response.body!!.string())
    }

    private fun parseResult(json: JSONObject){
        rating=json.getInt("rating")
        additionalRating=json.getInt("additional_rating")
        nickname=json.getString("nickname")
        try {
            plate = json.getString("plate")
        }
        catch (_:Exception){}
        username=json.getString("username")

        val charts=json.getJSONObject("charts")
        val dxArray=charts.getJSONArray("dx")
        for(i in dxArray.toList().indices){
            dxSongResultList.add(MaiSongResult(dxArray.getJSONObject(i),i))
        }

        val sdArray=charts.getJSONArray("sd")
        for(i in sdArray.toList().indices){
            sdSongResultList.add(MaiSongResult(sdArray.getJSONObject(i),i))
        }
    }

    constructor(qq:Long){
        val requestBody=JSONObject("{\"qq\":${qq}}")
        val json=getInfoFromProbe(requestBody)
        parseResult(json)
    }
    constructor(name:String){
        val requestBody=JSONObject("{\"username\":${name}}")
        val json=getInfoFromProbe(requestBody)
        parseResult(json)
    }

    fun drawB40Image(): File {
        var bg = ImageIO.read(File("$resourcePath\\maimai\\background.png"))
        val g=bg.createGraphics()

        g.font=Font("微软雅黑",Font.PLAIN,50)
        g.drawString("$nickname 的 Best40",25,160)

        g.font=Font("微软雅黑",Font.PLAIN,30)
        g.drawString("Rating: $rating (底分) + $additionalRating (段位分) = ${rating+additionalRating} " +
                "(${ratingColorTransform(rating+additionalRating)})",27,200)
        g.drawString("该用户${if(plate=="")"没有佩戴名牌板。" else "佩戴的名牌板是： $plate"}",800,200)

        val imageSize=200

        for(i in 0..4){
            for(j in 0..4){
                g.drawImage(sdSongResultList[i*5+j].drawSong(),25+j*imageSize,225+i*imageSize,null)
            }
        }
        for(i in 0..4){
            for(j in 0..2){
                g.drawImage(dxSongResultList[i*3+j].drawSong(),1050+j*imageSize,225+i*imageSize,null)
            }
        }


        val file=File("$projectPath\\cache\\b40Cache\\$nickname.png")
        ImageIO.write(bg,"png",file)
        return file
    }
}