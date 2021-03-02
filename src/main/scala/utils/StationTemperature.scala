package utils

import java.time.LocalDate

import observatory.Extraction.spark
import observatory.{Location, StationData, Temperature}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.{DoubleType, IntegerType, StringType, StructField, StructType}

import scala.io.Source

object StationTemperature {

  val stnCol       : StructField = StructField("STN"       , StringType, true)
  val wbanCol      : StructField = StructField("WBAN"      , StringType, true)
  val latCol       : StructField = StructField("LATITUDE"  , DoubleType , true)
  val longCol      : StructField = StructField("LONGITUDE" , DoubleType , true)
  val monthCol     : StructField = StructField("MONTH"     , IntegerType, true)
  val dayCol       : StructField = StructField("DAY"       , IntegerType, true)
  val fahrenheitCol: StructField = StructField("FAHRENHEIT", DoubleType , true)

  val STATION_SCHEMA     : StructType = StructType(List(stnCol, wbanCol, latCol, longCol))
  val TEMPERATURES_SCHEMA: StructType = StructType(List(stnCol, wbanCol, monthCol, dayCol, fahrenheitCol))

  import spark.implicits._

  private def readFile(file: String, schema: StructType): DataFrame = {
    val fileStream = Source.getClass.getResourceAsStream(file)
    val csvFile = Source.fromInputStream(fileStream).getLines().toList
    val csvDs = spark.sparkContext.parallelize(csvFile).toDS
    spark.read.schema(schema).csv(csvDs)
  }

  def readStations(file: String): DataFrame = {
    readFile(file, STATION_SCHEMA)
      .filter($"LATITUDE".isNotNull && $"LONGITUDE".isNotNull)
      .na.fill("-")
  }

  def readTemperatures(file: String): DataFrame = {
    readFile(file, TEMPERATURES_SCHEMA)
      .filter($"MONTH".isNotNull && $"DAY".isNotNull)
      .filter($"FAHRENHEIT".isNotNull && $"FARENHEIT" != 9999.9)
      .na.fill("-")
  }

  def dateLocationTemperature(row: StationData): (LocalDate, Location, Temperature) = {
    (
      LocalDate.of(row.year, row.month, row.day),
      Location(row.latitude, row.longitude),
      convertFTempToCTemp(row.fahrenheit)
    )
  }

  private def convertFTempToCTemp(f_temp: Double): Double = ((BigDecimal(f_temp) - 32) / 1.8).toDouble

}
