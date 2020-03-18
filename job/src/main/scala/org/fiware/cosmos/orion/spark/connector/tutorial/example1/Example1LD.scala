package org.fiware.cosmos.orion.spark.connector.tutorial.example1


import org.apache.spark._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.fiware.cosmos.orion.spark.connector._
/**
  * Example1 Orion Spark Tutorial
  * @author @Javierlj
  */
object Example1LD{

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[4]").setAppName("Example 1")
    val ssc = new StreamingContext(conf, Seconds(10))
    // Create Orion Source. Receive notifications on port 9001
    val eventStream = ssc.receiverStream(new NGSILDReceiver(9001))

    // Process event stream
    eventStream
      .flatMap(event => event.entities)
      .map(ent => {
        new Sensor(ent.`type`)
      })
      .countByValue()
      .window(Seconds(10))
      .print()


    ssc.start()
    ssc.awaitTermination()
  }
  case class Sensor(device: String)
}
