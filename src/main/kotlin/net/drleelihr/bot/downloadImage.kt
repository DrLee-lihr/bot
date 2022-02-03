package net.drleelihr.bot

import java.io.File
import java.net.URL
import java.util.zip.GZIPInputStream

fun downloadImage(url: String, file: File): File {
    val openConnection = URL(url).openConnection()
    openConnection.addRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
    val bytes = if (openConnection.contentEncoding == "gzip")
                    GZIPInputStream(openConnection.getInputStream()).readBytes()
                else openConnection.getInputStream().readBytes()
    file.writeBytes(bytes)
    return file
}