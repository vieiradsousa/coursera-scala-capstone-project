package utils

import observatory.{Color, Temperature}

object LinearInterpolation {
  def get(point1: Option[(Temperature, Color)], point2: Option[(Temperature, Color)], value: Temperature): Color =
    (point1, point2) match {
      case (Some(p1), None) => p1._2
      case (None, Some(p2)) => p2._2
      case (Some((p1Temp, p1Color)), Some((p2Temp, p2Color))) => {
        val liv = getValue(value, p1Temp, p2Temp) _
        Color(
          liv(p1Color.red, p2Color.red),
          liv(p1Color.green, p2Color.green),
          liv(p1Color.blue, p2Color.blue)
        )
      }
      case _ => Color(0, 0, 0)
    }

  private def getValue(value: Temperature, p1Temp: Temperature, p2Temp: Temperature)(p1Color: Int, p2Color: Int): Int = {
    val factor = (value - p1Temp) / (p2Temp - p1Temp)
    (p1Color + (p2Color - p1Color) * factor).round.toInt
  }

}
