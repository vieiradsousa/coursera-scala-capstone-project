package utils

import observatory.{GridLocation, Location, Temperature, Visualization}

class Grid {

  private val HEIGHT_TOP    =  90
  private val HEIGHT_BOTTOM = -90

  private val WIDTH_TOP    =  180
  private val WIDTH_BOTTOM = -180

  private var TEMPERATURES: Array[Temperature] = new Array[Temperature](360*180)

  def getIndex(lat: Int, lon: Int): Int = (lat + 89) * 360 + (lon + 180)

  def setTemperature(index: Int, temp: Temperature): Unit = TEMPERATURES(index) = temp

  def getTemperature(index: Int): Temperature = TEMPERATURES(index)

  def sum(that: Grid): Grid = {
    TEMPERATURES.indices.foreach(idx => this.TEMPERATURES(idx) += that.TEMPERATURES(idx))
    this
  }

  def divide(denominator: Double): Grid = {
    TEMPERATURES = TEMPERATURES.map(_ / denominator)
    this
  }

  def subtract(that: GridLocation => Temperature): Grid = {
    for {
      lat <- Range(HEIGHT_TOP, HEIGHT_BOTTOM, -1)
      lon <- WIDTH_BOTTOM until WIDTH_TOP
    } yield {
      val idx  = getIndex(lat, lon)
      val temp = getTemperature(idx) - that(GridLocation(lat, lon))

      setTemperature(idx, temp)
    }
    this
  }

  def setupTemperatures(temps: Iterable[(Location, Temperature)]): Grid = {
    for {
      lat <- Range(HEIGHT_TOP, HEIGHT_BOTTOM, -1)
      lon <- WIDTH_BOTTOM until WIDTH_TOP
    } yield {
      val idx  = getIndex(lat, lon)
      val temp = Visualization.predictTemperature(temps, Location(lat, lon))

      setTemperature(idx, temp)
    }
    this
  }

}
