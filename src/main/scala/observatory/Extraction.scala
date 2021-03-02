package observatory

import java.time.LocalDate

import org.apache.spark.sql.functions._
import org.apache.spark.sql.SparkSession
import utils.StationTemperature

/**
  * 1st milestone: data extraction
  */
object Extraction extends ExtractionInterface {

  import org.apache.log4j.{Level, Logger}

  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  val spark: SparkSession = SparkSession.builder().appName("Data Extraction").master("local").config("spark.executor.memory", "1G").getOrCreate()

  import spark.implicits._

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {
    val stations     = StationTemperature.readStations(stationsFile).cache()
    val temperatures = StationTemperature.readTemperatures(temperaturesFile).cache()

    stations
      .join(temperatures, Seq("STN", "WBAN"))
      .withColumn("YEAR", lit(year))
      .as[StationData]
      .collect()
      .map(StationTemperature.dateLocationTemperature(_))
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    records
      .groupBy(record => (record._1.getYear, record._2))
      .mapValues(_.map(value => value._3)).toSeq
      .map(value => (value._1._2, value._2.sum / value._2.size))
  }

}
