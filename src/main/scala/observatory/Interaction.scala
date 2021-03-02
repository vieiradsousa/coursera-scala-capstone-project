package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Visualization.{interpolateColor, predictTemperature}

import scala.math.{Pi, atan, pow, sinh} //, Pixel}

/**
  * 3rd milestone: interactive visualization
  */
object Interaction extends InteractionInterface {

  private val IMAGE_HEIGHT = 256
  private val IMAGE_WIDTH = 256

  private val ALPHA = 127

  /**
    * @param tile Tile coordinates
    * @return The latitude and longitude of the top-left corner of the tile, as per http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    */
  def tileLocation(tile: Tile): Location = {
    val latitude  = atan(sinh(Pi - (Pi * 2 * tile.y))).toDegrees / pow(2, tile.zoom)
    val longitude = (tile.x * 360) / pow(2, tile.zoom) - 180
    Location(latitude, longitude)
  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @param tile Tile coordinates
    * @return A 256×256 image showing the contents of the given tile
    */
  def tile(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)], tile: Tile): Image = {
    val xOff = tile.x * IMAGE_WIDTH
    val yOff = tile.y * IMAGE_HEIGHT
    val zOff = tile.zoom

    val coordinates = for {
      i <- 0 until IMAGE_HEIGHT
      j <- 0 until IMAGE_WIDTH
    } yield (i, j)

    val pixels = coordinates
      .map({ case (y,x) => Tile(x + xOff, y + yOff, 8 + zOff)})
      .map(tile        => tileLocation(tile))
      .map(location    => predictTemperature(temperatures, location))
      .map(temperature => interpolateColor(colors, temperature))
      .map(color       => Pixel(color.red, color.green, color.blue, ALPHA))
      .toArray

    Image(IMAGE_WIDTH, IMAGE_HEIGHT, pixels)
  }

  /**
    * Generates all the tiles for zoom levels 0 to 3 (included), for all the given years.
    * @param yearlyData Sequence of (year, data), where `data` is some data associated with
    *                   `year`. The type of `data` can be anything.
    * @param generateImage Function that generates an image given a year, a zoom level, the x and
    *                      y coordinates of the tile and the data to build the image from
    */
  def generateTiles[Data](yearlyData: Iterable[(Year, Data)], generateImage: (Year, Tile, Data) => Unit): Unit = {
    for {
      zoom <- 0 to 3
      x <- 0 until pow(2, zoom).toInt
      y <- 0 until pow(2, zoom).toInt
      (year, data) <- yearlyData
    } yield {
      generateImage(year, Tile(x, y, zoom), data)
    }
  }



}
