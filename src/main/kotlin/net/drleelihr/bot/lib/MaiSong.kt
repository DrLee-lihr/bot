package net.drleelihr.bot.lib

import net.drleelihr.bot.projectPath
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.json.JSONObject
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt

class MaiSong(json:JSONObject) {

    inner class Chart(json: JSONObject){
        val song=this@MaiSong
        var difficulty:Int=0 //谱面颜色
        var tap:Int=0
        var hold:Int=0
        var slide:Int=0
        var Break:Int=0
        var touch:Int?=null
        var ds:Float=0.0F   //谱面定数
        var cid:Int=0       //谱面id
        var charter:String

        var playerCount:Int=0
        var average:Double=0.0
        lateinit var tag:String
        var difficultyRankInSameLevel:Int=0
        var songCountInSameLevel:Int=0
        var sssCount:Int=0
        var chartProbeSummary=""

        init{
            difficulty=json.getInt("difficulty")
            cid=json.getInt("cid")
            ds=json.getFloat("ds")
            charter=json.getString("charter")
            val notes=json.getJSONArray("notes")
            tap= notes[0] as Int
            hold= notes[1] as Int
            slide= notes[2] as Int
            if(notes.length()==5){
                Break= notes[4] as Int
                touch= notes[3] as Int
            }
            else Break= notes[3] as Int
        }

        fun getProbeData(): Chart {
            val data=JSONObject(httpRequest("https://maimai.ohara-rinne.tech/api/chart/${song.id}/${difficulty}"))
                .getJSONObject("data")
            playerCount=data.getInt("playerCount")
            average=data.getDouble("average")
            tag=data.getString("tag")
            difficultyRankInSameLevel=data.getInt("difficultyRankInSameLevel")
            songCountInSameLevel=data.getInt("songCountInSameLevel")
            sssCount=data.getInt("ssscount")
            chartProbeSummary="""
                tag:$tag
                共有${playerCount}名玩家游玩了该谱面，平均达成率：${average}
                其中${sssCount}人（${((sssCount.toDouble() / playerCount.toDouble())*10000).toInt().toDouble()/100}%）达成SSS
                SSS人数在同级别曲目中排名：（${difficultyRankInSameLevel}/${songCountInSameLevel}）
            """.trimIndent()
            return this
        }

        var chartNotesInfo="""
                        TAP: $tap
                        HOLD: $hold
                        SLIDE: $slide
                        ${if(touch!=null)"TOUCH: $touch\n                        " else ""}BREAK: $Break
                        charter: $charter
                    """.trimIndent()
        var chartSummary="${id}.${title}(${typeStr})[${difficultyLevelTransform(difficulty)}]"
        var chartSummaryWithBase="${id}.${title}(${typeStr})[${difficultyLevelTransform(difficulty)}](${ds})"
        var chartBaseSummary="${difficultyFullLevelTransform(difficulty)} ${dxs2LevelTransform(ds)}(${ds})"
    }

    var id: Int=589
    var title:String
    var basicInfo:JSONObject
    var charts:MutableList<Chart> = mutableListOf()
    var artist:String
    var genre:String
    var hasReM:Boolean=false
    var bpm:Int=0
    var version:String
    var isNew:Boolean=false
    var type:Int=0
    var ds:MutableList<Float> = mutableListOf()
    var typeStr:String

    init {
        id=json.getString("id").toInt()
        title=json.getString("title")
        typeStr=json.getString("type")
        type=if(typeStr=="SD")0 else 1
        basicInfo=json.getJSONObject("basic_info")
        artist=basicInfo.getString("artist")
        genre=basicInfo.getString("genre")
        bpm=basicInfo.getInt("bpm")
        version=basicInfo.getString("from")
        isNew=basicInfo.getBoolean("is_new")
        ds=json.getJSONArray("ds").toMutableList() as MutableList<Float>
        val chartsJson=json.getJSONArray("charts")
        hasReM=chartsJson.length()==5
        for(i in 0 until chartsJson.length()){
            charts.add(i,Chart(chartsJson.getJSONObject(i)
                .put("ds",json.getJSONArray("ds").getFloat(i))
                .put("cid",json.getJSONArray("cids").getFloat(i))
                .put("difficulty",i)
            ))
        }
    }

    var songInfoSummary="${id}.${title}(${typeStr})"

    var songDifficultSummary="${ds[0]}/${ds[1]}/${ds[2]}/${ds[3]}${
        try{"/${ds[4]}"}catch(_:Exception){""}
    }"

    fun getImageFile(): File {
        val songImageFile = File("$projectPath\\cache\\pictures\\${id}.jpg")
        return if (!songImageFile.exists())
            downloadImage("https://www.diving-fish.com/covers/${id}.jpg", songImageFile)
        else songImageFile
    }

    suspend fun getImageFileAsMessage(event:MessageEvent): Image {
        return  getImageFile().uploadAsImage(event.subject)
    }
}