package utils

import observatory.{Location, Temperature}

import scala.math.{atan2, cos, pow, sin, sqrt}

object SpatialInterpolation {

  private val EARTH_RADIUS = 6371

  def get(temperatures: Iterable[(Location, Temperature)], locationToPredict: Location): Temperature = {
    def aggOp(res1: (Double, Double), res2: (Double, Double)): (Double, Double) = {
      (res1._1 + res2._1, res1._2 + res2._2)
    }

    val result = temperatures.map(temperature => {
      val idw = inverse_distance_weighting(temperature._1, locationToPredict)
      (temperature._2 * idw, idw)
    })
      .aggregate[(Double, Double)]((0.0, 0.0))(aggOp, aggOp)

    result._1 / result._2
  }

  private def great_circle_distance(loc1: Location, loc2: Location): Double = {
    val latitudeDistance = (loc1.lat - loc2.lat).toRadians
    val longitudeDistance = (loc1.lon - loc2.lon).toRadians

    val sinLatitude = sin(latitudeDistance / 2)
    val sinLongitude = sin(longitudeDistance / 2)

    val squaredSinLatitude = sinLatitude * sinLatitude
    val squaredSinLongitude = sinLongitude * sinLongitude

    val cosLoc1Latitude = cos(loc1.lat.toRadians)
    val cosLoc2Latitude = cos(loc2.lat.toRadians)

    val a = squaredSinLatitude + ((cosLoc1Latitude * cosLoc2Latitude) * squaredSinLongitude)

    val atan = atan2(sqrt(a), sqrt(1 - a))

    val c = 2 * atan

    EARTH_RADIUS * c
  }

  private def inverse_distance_weighting(locationKnown: Location, locationToPredict: Location): Double = {
    val p = 2

    val distance = great_circle_distance(locationKnown, locationToPredict)

    1 / pow(distance, p)
  }

}
