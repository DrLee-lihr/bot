package net.drleelihr.bot.lib

import net.drleelihr.bot.projectPath
import org.json.JSONObject
import java.io.File

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
    lateinit var title:String
    lateinit var basicInfo:JSONObject
    var charts:MutableList<Chart> = mutableListOf()
    lateinit var artist:String
    lateinit var genre:String
    var hasReM:Boolean=false
    var bpm:Int=0
    lateinit var version:String
    var isNew:Boolean=false
    var type:Int=0
    var ds:MutableList<Float> = mutableListOf()
    lateinit var typeStr:String

    private fun parseJSON(json:JSONObject){
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
    init {
        parseJSON(json)
    }
    var songInfoSummary="${id}.${title}(${typeStr})"

    var songDifficultSummary="${ds[0]}/${ds[1]}/${ds[2]}/${ds[3]}${
        try{ds[5]}catch(_:Exception){""}
    }"

    fun getImageFile(): File {
        val songImageFile = File("$projectPath\\cache\\${id}.jpg")
        return if (!songImageFile.exists())
            downloadImage("https://www.diving-fish.com/covers/${id}.jpg", songImageFile)
        else songImageFile
    }
}