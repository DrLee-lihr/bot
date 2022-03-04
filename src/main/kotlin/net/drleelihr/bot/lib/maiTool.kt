package net.drleelihr.bot.lib

import kotlin.math.floor

fun difficultyLevelTransform(a:Int):String{
    return when (a) {
        0 -> "BSC"
        1 -> "ADV"
        2 -> "EXP"
        3 -> "MST"
        4 -> "ReM"
        else -> "Original"
    }
}

fun difficultyFullLevelTransform(a:Int):String {
    return when (a) {
        0 -> "Basic"
        1 -> "Advanced"
        2 -> "Expert"
        3 -> "Master"
        4 -> "Re:Master"
        else -> "Original"
    }
}

val dxs2LevelTransform: (Float) -> String = { "${floor(it).toInt()}${if(it- floor(it) >=0.65)"+" else ""}" }

infix fun Float.inLevel(s:String) : Boolean  {
    val baseLevel:Int=s.split("+","＋")[0].toInt()
    return if(baseLevel<=6&&(s.contains("+")||s.contains("＋"))) false
    else if(s.contains("+")||s.contains("＋")) (this>=baseLevel+0.65&&this<=baseLevel+0.95)
    else (this>=baseLevel-0.05&&this<=baseLevel+0.65)
}

fun difficultyIDTransform(a:String): Int {
    return when (a.lowercase()) {
        "绿","bsc","basic","bas" -> 0
        "黄","adv","advanced" -> 1
        "红","exp","expert" -> 2
        "紫","mst","master","mas" -> 3
        "白","rem","re:master","remaster" -> 4
        else -> -1
    }
}