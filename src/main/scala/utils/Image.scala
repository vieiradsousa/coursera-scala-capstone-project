package utils

import java.io.File

import com.sksamuel.scrimage.writer
import observatory.{Color, Interaction, Location, Temperature, Tile, Year}

object Image {

  private val TILES_OUTPUT_PATH = "target/temperatures"

  private val COLORS_FROM_TO = Iterable[(Temperature, Color)](
    ( 60, Color(255, 255, 255)),
    ( 32, Color(255, 0  , 0  )),
    ( 12, Color(255, 255, 0  )),
    (  0, Color(0  , 255, 255)),
    (-15, Color(0  , 0  , 255)),
    (-27, Color(255, 0  , 255)),
    (-50, Color(33 , 0  , 107)),
    (-60, Color(0  , 0  , 0  ))
  )

  def generate[Data](year: Year, tile: Tile, temperatures: Iterable[(Location, Temperature)]): Unit = {
    //  STANDARD OUTPUT FORMAT = target/temperatures/2015/<zoom>/<x>-<y>.png
    val x = tile.x
    val y = tile.y
    val zoom = tile.zoom

    val output_path = s"$TILES_OUTPUT_PATH/$year/$zoom/$x-$y.png"

    Interaction.tile(temperatures, COLORS_FROM_TO, tile).output(new File(output_path))
  }

}
