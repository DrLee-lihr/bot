package net.drleelihr.bot.lib

import net.drleelihr.bot.lib.GaussianBlurUtil.blur
import net.drleelihr.bot.lib.maimai.MaiSong
import java.awt.Color
import java.awt.Font
import java.io.File
import javax.imageio.ImageIO


fun drawSong(song: MaiSong) {
    val resourcePath="D:\\Dev\\bot\\src\\main\\resources"

    //获取一张图片做底图
    val bg = blur(resizeImage(ImageIO.read(song.getImageFile()),400,400),7)
    val g = bg.createGraphics()

    //设置字体/颜色
    val font = Font("Consolas", Font.PLAIN, 50)
    g.font = font
    g.color = Color.WHITE
    g.drawString("Panopticon", 20, 55)

    //把另外一个图描画在底图上
    val rate = ImageIO.read(File("$resourcePath\\maimai\\sss.jpg"))
    val diff = ImageIO.read(File("$resourcePath\\maimai\\master.jpg"))
    //这个方法画全图，按照后两个参数设置的宽高缩放
    g.drawImage(diff,0,70,diff.width*2,diff.height*2,null)
    g.drawImage(rate, 0, 125, 146,98,null)

    //截取图片的一部分
    //val subImg = img.getSubimage(70, 70, 120, 120)
    //g.drawImage(subImg, 20, 20, 60, 60, null)

    //保存图片
    ImageIO.write(bg, "jpg", File("newimg.jpg"))
}



fun main(){

}







