package org.fiware.cosmos.tutorial


import org.apache.spark._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.fiware.cosmos.orion.spark.connector._

/**
  * Feedback Example Orion Connector
  * @author @Javierlj
  */
object Feedback {
  final val CONTENT_TYPE = ContentType.JSON
  final val METHOD = HTTPMethod.PATCH
  final val CONTENT = "{\n  \"on\": {\n      \"type\" : \"command\",\n      \"value\" : \"\"\n  }\n}"
  final val HEADERS = Map("fiware-service" -> "openiot","fiware-servicepath" -> "/","Accept" -> "*/*")

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("Temperature")
    val ssc = new StreamingContext(conf, Seconds(60))
    // Create Orion Receiver. Receive notifications on port 9001
    val eventStream = ssc.receiverStream(new OrionReceiver(9001))

    // Process event stream
    val processedDataStream = eventStream
      .flatMap(event => event.entities)
      .filter(entity=>(entity.attrs("count").value == "1"))
      .map(entity=> new Sensor(entity.id))
      .window(Seconds(60))

    val sinkStream= processedDataStream.map(sensor => {
      OrionSinkObject(CONTENT,"http://orion:1026/v2/entities/Lamp:"+sensor.id.takeRight(3)+"/attrs",CONTENT_TYPE,METHOD,HEADERS)
    });


    // Add Orion Sink
    OrionSink.addSink( sinkStream )

    // print the results with a single thread, rather than in parallel
    processedDataStream.print()
    ssc.start()

    ssc.awaitTermination()
  }

  case class Sensor(id: String)
}
