package observatory

import java.time.LocalDate

import org.junit._

import scala.collection.mutable.ArraySeq

trait ExtractionTest extends MilestoneSuite {
  private val milestoneTest = namedMilestoneTest("data extraction", 1) _

  val STATIONS_SCHEMA = Extraction.STATION_SCHEMA
  val TEMPERATURE_SCHEMA = Extraction.TEMPERATURES_SCHEMA

  val STATIONS_FILE: String = "/stations.csv"
  val TEMPERATURES_FILE: String = "/2015.csv"

  val stationTemperatures = ArraySeq(
    (LocalDate.of(2015, 8, 11), Location(37.35, -78.433), 27.3),
    (LocalDate.of(2015, 12, 6), Location(37.358, -78.438), 0.0),
    (LocalDate.of(2015, 1, 29), Location(37.358, -78.438), 2.0)
  )

  @Test def `locateTemperaturesTest`: Unit = {
    val actualResult = Extraction.locateTemperatures(2015, STATIONS_FILE, TEMPERATURES_FILE)
    val expectedResult = stationTemperatures

    assert(expectedResult == actualResult)
  }

  @Test def `locationYearlyAverageRecordsTest`: Unit = {
    val actualResult = Extraction.locationYearlyAverageRecords(stationTemperatures)
    val expectedResult = ArraySeq((Location(37.35, -78.433), 27.3), (Location(37.358, -78.438), 1.0))

    assert(expectedResult == actualResult)
  }

}
