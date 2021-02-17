package org.fiware.cosmos.tutorial
import org.apache.spark._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.fiware.cosmos.orion.spark.connector._
/**
  * Logger example NGSILD Connector
  * @author @Javierlj
  */
object LoggerLD{

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("Example 1")
    val ssc = new StreamingContext(conf, Seconds(60))
    // Create Orion Receiver. Receive notifications on port 9001
    val eventStream = ssc.receiverStream(new NGSILDReceiver(9001))

    // Process event stream
    eventStream
      .flatMap(event => event.entities)
      .map(ent => {
        new Sensor(ent.`type`)
      })
      .countByValue()
      .window(Seconds(60))
      .print()


    ssc.start()
    ssc.awaitTermination()
  }
  case class Sensor(device: String)
}