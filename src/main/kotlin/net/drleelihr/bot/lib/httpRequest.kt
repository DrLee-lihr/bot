package net.drleelihr.bot.lib

import okhttp3.OkHttpClient
import okhttp3.Request

fun httpRequest(host:String): String? {
    val client= OkHttpClient()
    val request= Request.Builder()
        .url(host)
        .get()
        .build()
    val call=client.newCall(request)
    val response = call.execute()
    if(response.isSuccessful) {
        val body = response.body
        //println(body?.string)
        return body?.string()
    }
    else throw(NullPointerException("Request failed,status code:${response.code}"))
}