package net.drleelihr.bot.lib

import java.awt.Image
import kotlin.Throws
import java.io.IOException
import java.awt.image.BufferedImage


fun resizeImage(originalImage: BufferedImage, targetWidth: Int, targetHeight: Int): BufferedImage {
    val resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_AREA_AVERAGING)
    val outputImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
    outputImage.graphics.drawImage(resultingImage, 0, 0, null)
    return outputImage
}


private fun clamp(rgb: Int): Int {
    if (rgb > 255) return 255
    return if (rgb < 0) 0 else rgb
}

fun setLight(image:BufferedImage,param:Int): BufferedImage {
    var rgb: Int
    var R: Int
    var G: Int
    var B: Int
    for (i in 0 until image.width) {
        for (j in 0 until image.height) {
            rgb = image.getRGB(i, j)
            R = (rgb shr 16 and 0xff) + param
            G = (rgb shr 8 and 0xff) + param
            B = (rgb and 0xff) + param
            rgb =
                255 and 0xff shl 24 or (clamp(R) and 0xff shl 16) or (clamp(G) and 0xff shl 8) or (clamp(B) and 0xff)
            image.setRGB(i, j, rgb)
        }
    }
    return image
}