package observatory

import java.time.LocalDate

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.io.Source

/**
  * 1st milestone: data extraction
  */
object Extraction extends ExtractionInterface {

  import org.apache.log4j.{Level, Logger}

  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  val spark: SparkSession = SparkSession.builder().appName("Data Extraction").master("local").config("spark.executor.memory", "1G").getOrCreate()

  import spark.implicits._

  val stnCol       : StructField = StructField("STN"       , StringType, true)
  val wbanCol      : StructField = StructField("WBAN"      , StringType, true)
  val latCol       : StructField = StructField("LATITUDE"  , DoubleType , true)
  val longCol      : StructField = StructField("LONGITUDE" , DoubleType , true)
  val monthCol     : StructField = StructField("MONTH"     , IntegerType, true)
  val dayCol       : StructField = StructField("DAY"       , IntegerType, true)
  val fahrenheitCol: StructField = StructField("FAHRENHEIT", DoubleType , true)

  val STATION_SCHEMA     : StructType = StructType(List(stnCol, wbanCol, latCol, longCol))
  val TEMPERATURES_SCHEMA: StructType = StructType(List(stnCol, wbanCol, monthCol, dayCol, fahrenheitCol))

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {
    val stations     = readStations(stationsFile)
    val temperatures = readTemperatures(temperaturesFile)

    val stationsTemperatures = stations
      .join(temperatures, Seq("STN", "WBAN"))
      .withColumn("YEAR", lit(year))
      .as[StationData]
      .collect()

    stationsTemperatures.map(dateLocationTemperature(_))
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

  private def readFile(file: String, schema: StructType): DataFrame = {
    val fileStream = Source.getClass.getResourceAsStream(file)
    val csvFile = Source.fromInputStream(fileStream).getLines().toList
    val csvDs = spark.sparkContext.parallelize(csvFile).toDS
    spark.read.schema(schema).csv(csvDs)
  }

  private def readStations(file: String): DataFrame = {
    readFile(file, STATION_SCHEMA)
      .filter($"LATITUDE".isNotNull && $"LONGITUDE".isNotNull)
      .na.fill("-")
  }

  private def readTemperatures(file: String): DataFrame = {
    readFile(file, TEMPERATURES_SCHEMA)
      .filter($"MONTH".isNotNull && $"DAY".isNotNull)
      .filter($"FAHRENHEIT".isNotNull && $"FARENHEIT" != 9999.9)
      .na.fill("-")
  }

  private def convertFTempToCTemp(f_temp: Double): Double = ((BigDecimal(f_temp) - 32) / 1.8).toDouble

  private def dateLocationTemperature(row: StationData): (LocalDate, Location, Temperature) = {
    (
      LocalDate.of(row.year, row.month, row.day),
      Location(row.latitude, row.longitude),
      convertFTempToCTemp(row.fahrenheit)
    )
  }

}
