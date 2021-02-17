package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import utils.{ImagePixels, LinearInterpolation, SpatialInterpolation}

/**
  * 2nd milestone: basic visualization
  */
object Visualization extends VisualizationInterface {

  private val IMAGE_HEIGHT = 180
  private val IMAGE_WIDTH  = 360

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature =
    temperatures.find(_._1 == location).map(_._2).getOrElse(SpatialInterpolation.get(temperatures, location))

  /**
    * @param points Pairs containing a value and its associated color
    * @param value The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color =
    points.find(_._1 == value).map(_._2).getOrElse({
      val (bottom, up) = points.toSeq.sortBy(_._1).partition(_._1 < value)
      LinearInterpolation.get(bottom.reverse.headOption, up.headOption, value)
    })

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    val pixels: Array[Pixel] = ImagePixels.get(temperatures, colors)
    Image(IMAGE_WIDTH, IMAGE_HEIGHT, pixels)
  }

}
