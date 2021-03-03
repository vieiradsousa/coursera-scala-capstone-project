package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Interaction.tileLocation
import observatory.Visualization.interpolateColor

/**
  * 5th milestone: value-added information visualization
  */
object Visualization2 extends Visualization2Interface {

  private val IMAGE_HEIGHT = 256
  private val IMAGE_WIDTH = 256

  private val ALPHA = 127

  /**
    * @param point (x, y) coordinates of a point in the grid cell
    * @param d00 Top-left value
    * @param d01 Bottom-left value
    * @param d10 Top-right value
    * @param d11 Bottom-right value
    * @return A guess of the value at (x, y) based on the four known values, using bilinear interpolation
    *         See https://en.wikipedia.org/wiki/Bilinear_interpolation#Unit_Square
    */
  def bilinearInterpolation(point: CellPoint, d00: Temperature, d01: Temperature, d10: Temperature, d11: Temperature): Temperature = {
    val x = point.x
    val y = point.y
    d00 * (1 - x) * (1 - y) + d10 * x * (1 - y) + d01 * (1 - x) * y + d11 * x * y
  }

  /**
    * @param grid Grid to visualize
    * @param colors Color scale to use
    * @param tile Tile coordinates to visualize
    * @return The image of the tile at (x, y, zoom) showing the grid using the given color scale
    */
  def visualizeGrid(grid: GridLocation => Temperature, colors: Iterable[(Temperature, Color)], tile: Tile): Image = {
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
      .map(location    => predictTemperature(grid, location))
      .map(temperature => interpolateColor(colors, temperature))
      .map(color       => Pixel(color.red, color.green, color.blue, ALPHA))
      .toArray

    Image(IMAGE_WIDTH, IMAGE_HEIGHT, pixels)
  }

  private def predictTemperature(gridLocation: GridLocation => Temperature, location: Location): Temperature = {
    val locationLatInt = location.lat.toInt
    val locationLonInt = location.lon.toInt

    val pointD00 = GridLocation(locationLatInt    , locationLonInt    )
    val pointD01 = GridLocation(locationLatInt + 1, locationLonInt    )
    val pointD10 = GridLocation(locationLatInt    , locationLonInt + 1)
    val pointD11 = GridLocation(locationLatInt + 1, locationLonInt + 1)

    val cellPoint = CellPoint(location.lon - locationLonInt, location.lat - locationLatInt)

    bilinearInterpolation(cellPoint, gridLocation(pointD00), gridLocation(pointD01), gridLocation(pointD10), gridLocation(pointD11))
  }

}
