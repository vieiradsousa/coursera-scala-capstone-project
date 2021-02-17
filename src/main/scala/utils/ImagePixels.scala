package utils

import com.sksamuel.scrimage.{Pixel, RGBColor}
import observatory.Visualization.interpolateColor
import observatory.{Color, Location, Visualization}

object ImagePixels {

  def get(temperatures: Iterable[(Location, Double)], colors: Iterable[(Double, Color)]): Array[Pixel] = {
    (0 until (360 * 180))
      .map(index       => indexFromPixel(index))
      .map(location    => Visualization.predictTemperature(temperatures, location))
      .map(temperature => interpolateColor(colors, temperature))
      .map(color       => Pixel(RGBColor(color.red, color.green, color.blue)))
      .toArray
  }

  private def indexFromPixel(index: Int): Location = Location(90 - (index / 360), (index % 360) - 180)

}
