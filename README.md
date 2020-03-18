
[![FIWARE Banner](https://fiware.github.io/tutorials.Historic-Context-NIFI/img/fiware.png)](https://www.fiware.org/developers)

[![FIWARE Context processing, analysis and visualisation](https://nexus.lab.fiware.org/static/badges/chapters/processing.svg)](https://github.com/FIWARE/catalogue/blob/master/processing/README.md)
[![License: MIT](https://img.shields.io/github/license/fiware/tutorials.Historic-Context-NIFI.svg)](https://opensource.org/licenses/MIT)
[![NGSI v2](https://img.shields.io/badge/NGSI-v2-blue.svg)](https://fiware-ges.github.io/orion/api/v2/stable/)
[![Support badge](https://nexus.lab.fiware.org/repository/raw/public/badges/stackoverflow/fiware.svg)](https://stackoverflow.com/questions/tagged/fiware)
<br/>  [![Documentation](https://img.shields.io/readthedocs/fiware-tutorials.svg)](https://fiware-tutorials.rtfd.io)

  

This tutorial is an introduction to the [FIWARE Cosmos Orion Spark Connector](http://fiware-cosmos-spark.rtfd.io), which enables easier Big Data analysis over context, integrated with one of the most popular BigData platforms: [Apache Spark](https://spark.apache.org/). Apache Spark is a framework and distributed processing engine for stateful computations over unbounded and bounded data streams. Spark has been designed to run in all common cluster environments, perform computations at in-memory speed and at any scale.

  
The tutorial uses [cUrl](https://ec.haxx.se/) commands throughout, but is also available as [Postman documentation](https://fiware.github.io/tutorials.Historic-Context-NIFI/)

  
[![Run in Postman](https://run.pstmn.io/button.svg)](https://www.getpostman.com/collections/b8bd8e88b69c49fdfbc7)

  
## Contents

<details>

<summary><strong>Details</strong></summary>

  
-  [Real-time Processing of Historic Context Information using Apache Spark](#real-time-processing-of-historic-context-information-using-apache-spark)
-  [Architecture](#architecture)
-  [Prerequisites](#prerequisites)
-  [Docker and Docker Compose](#docker-and-docker-compose)
-  [Cygwin for Windows](#cygwin-for-windows)
-  [Start Up](#start-up)
-  [Example 1: Receiving data and preforming operations](#example-1-receiving-data-and-performing-operations)
-  [Example 2: Receiving data, performing operations and writing back to the Context Broker](#example-2--receiving-data-performing-operations-and-writing-back-to-the-context-broker)

</details>

  

# Real-time Processing of Historic Context Information using Apache Spark


> "Who controls the past controls the future: who controls the present controls the past."
>
> — George Orwell. "1984" (1949)

[FIWARE Cosmos](https://fiware-cosmos-spark.readthedocs.io/en/latest/) is a Generic Enabler that allows for an easier Big Data analysis over context integrated with some of the most popular Big Data platforms, such as [Apache Flink](https://flink.apache.org/) and [Apache Spark](https://spark.apache.org/).

The [FIWARE Cosmos Orion Spark Connector](http://fiware-cosmos-spark.rtfd.io) is a software tool that enables a direct ingestion of the context data coming from the notifications sent by **Orion Context Broker** to the Apache Spark processing engine. This allows to aggregate data in a time window in order to extract value from them in real-time.

#### Device Monitor


For the purpose of this tutorial, a series of dummy IoT devices have been created, which will be attached to the context broker. Details of the architecture and protocol used can be found in the [IoT Sensors tutorial](https://github.com/FIWARE/tutorials.IoT-Sensors). The state of each device can be seen on the UltraLight device monitor web page found at: `http://localhost:3000/device/monitor`


![FIWARE Monitor](https://fiware.github.io/tutorials.Historic-Context-NIFI/img/device-monitor.png)
 
# Architecture


This application builds on the components and dummy IoT devices created in [previous tutorials](https://github.com/FIWARE/tutorials.IoT-Agent/). It will make use of three FIWARE components - the [Orion Context Broker](https://fiware-orion.readthedocs.io/en/latest/), the [IoT Agent for Ultralight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/), and the [Cosmos Orion Spark Connector](https://fiware-cosmos-spark.readthedocs.io/en/latest/) for connecting Orion to an Apache Spark cluster. Additional databases are now involved - both the Orion Context Broker and the IoT Agent rely on [MongoDB](https://www.mongodb.com/) technology to keep persistence of the information they hold

  

Therefore the overall architecture will consist of the following elements:

- Three **FIWARE Generic Enablers**:
    - The FIWARE [Orion Context Broker](https://fiware-orion.readthedocs.io/en/latest/) which will receive requests using [NGSI](https://fiware.github.io/specifications/OpenAPI/ngsiv2)
    - The FIWARE [IoT Agent for Ultralight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/) which will receive northbound measurements from the dummy IoT devices in [Ultralight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/usermanual/index.html#user-programmers-manual) format and convert them to [NGSI](https://fiware.github.io/specifications/OpenAPI/ngsiv2) requests for the context broker to alter the state of the context entities
    - The FIWARE [Cosmos Orion Spark Connector](https://fiware-cosmos-spark.readthedocs.io/en/latest/) which will subscribe to context changes and make operations on them in real-time
- One **Database**:
  - The underlying [MongoDB](https://www.mongodb.com/) database :
      - Used by the **Orion Context Broker** to hold context data information such as data entities, subscriptions and registrations
      - Used by the **IoT Agent** to hold device information such as device URLs and Keys
- Three **Context Providers**:
  - The **Stock Management Frontend** is not used in this tutorial. It does the following:
    - Display store information and allow users to interact with the dummy IoT devices
    - Show which products can be bought at each store
    - Allow users to "buy" products and reduce the stock count.
    - A webserver acting as set of [dummy IoT devices](https://github.com/FIWARE/tutorials.IoT-Sensors) using the [Ultralight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/usermanual/index.html#user-programmers-manual) protocol running over HTTP.
  - The **Context Provider NGSI** proxy is not used in this tutorial. It does the following:
    - receive requests using [NGSI](https://fiware.github.io/specifications/OpenAPI/ngsiv2)
    - makes requests to publicly available data sources using their own APIs in a proprietary format
    - returns context data back to the Orion Context Broker in
 [NGSI](https://fiware.github.io/specifications/OpenAPI/ngsiv2) format.

  
Since all interactions between the elements are initiated by HTTP requests, the entities can be containerized and run from exposed ports.

The specific architecture of each section of the tutorial is discussed below.

# Prerequisites

## Docker and Docker Compose

To keep things simple, all components will be run using [Docker](https://www.docker.com). **Docker** is a container
technology which allows to different components isolated into their respective environments.

-   To install Docker on Windows follow the instructions [here](https://docs.docker.com/docker-for-windows/)
-   To install Docker on Mac follow the instructions [here](https://docs.docker.com/docker-for-mac/)
-   To install Docker on Linux follow the instructions [here](https://docs.docker.com/install/)

**Docker Compose** is a tool for defining and running multi-container Docker applications. A series of
[YAML files](https://github.com/FIWARE/tutorials.Historic-Context-NIFI/tree/master/docker-compose) are used to configure
the required services for the application. This means all container services can be brought up in a single command.
Docker Compose is installed by default as part of Docker for Windows and Docker for Mac, however Linux users will need
to follow the instructions found [here](https://docs.docker.com/compose/install/)

You can check your current **Docker** and **Docker Compose** versions using the following commands:

```bash
docker-compose -v
docker version
```

Please ensure that you are using Docker version 18.03 or higher and Docker Compose 1.21 or higher and upgrade if
necessary.

## Maven
[Apache Maven](https://maven.apache.org/download.cgi) is a software project management and comprehension tool. Based on the concept of a project object model (POM), Maven can manage a project's build, reporting and documentation from a central piece of information. We will use Maven to define and download our dependencies and to build and package our code into a JAR file. 

## IntelliJ (optional)
[IntelliJ](https://www.jetbrains.com/idea/) is an IDE that eases the development of Scala programs. We will use it to write and run our code.

## Cygwin for Windows

We will start up our services using a simple Bash script. Windows users should download [cygwin](http://www.cygwin.com/)
to provide a command-line functionality similar to a Linux distribution on Windows.


# Start Up

  

Before you start, you should ensure that you have obtained or built the necessary Docker images locally. Please clone the repository and create the necessary images by running the commands shown below. Note that you might need to run some of the commands as a privileged user:

  
```bash

git clone https://github.com/ging/fiware-cosmos-orion-spark-connector-tutorial.git
cd fiware-cosmos-orion-spark-connector-tutorial
./services create

```
This command will also import seed data from the previous tutorials and provision the dummy IoT sensors on startup.

To start the system, run the following command:
 
```bash
./services start
```

> :information_source: **Note:** If you want to clean up and start over again you can do so with the following command:
>
> ```bash
> ./services stop
> ```
  

Next, in order to use the Orion Spark Connector we need to install the JAR using Maven:

```
cd job
mvn install:install-file -Dfile=./orion.spark.connector-1.2.1.jar -DgroupId=org.fiware.cosmos -DartifactId=orion.spark.connector -Dversion=1.2.1 -Dpackaging=jar
```

### Generating Context Data

For the purpose of this tutorial, we must be monitoring a system in which the context is periodically being updated. The
dummy IoT Sensors can be used to do this. Open the device monitor page at `http://localhost:3000/device/monitor` and
unlock a **Smart Door** and switch on a **Smart Lamp**. This can be done by selecting an appropriate the command from
the drop down list and pressing the `send` button. The stream of measurements coming from the devices can then be seen
on the same page:

![](https://fiware.github.io/tutorials.Historic-Context-NIFI/img/door-open.gif)
  
### Running examples locally

For running locally we should download [IntelliJ](https://www.jetbrains.com/idea/download) and open the `job` directory of the project using [Maven](https://www.jetbrains.com/help/idea/maven-support.html#maven_import_project_start). Use JDK 1.8 

#### Example 1: Receiving data and performing operations

The first example makes use of the OrionSource in order to receive notifications from the Orion Context Broker. Specifically, the example counts the number notifications that each type of device sends in one minute. You can find the code of Example 1 in `job/src/main/scala/org/fiware/cosmos/orion/spark/connector/tutorial/example1/Example1.scala`:

```scala 

package org.fiware.cosmos.orion.spark.connector.tutorial.example1


import org.apache.spark._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.fiware.cosmos.orion.spark.connector._
/**
  * Example1 Orion Spark Tutorial
  * @author @Javierlj
  */
object Example1{

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[4]").setAppName("Example 1")
    val ssc = new StreamingContext(conf, Seconds(10))
    // Create Orion Source. Receive notifications on port 9001
    val eventStream = ssc.receiverStream(new OrionReceiver(9001))

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

```
After importing the necessary dependencies, the first step is creating the source and adding it to the environment.

```scala
val eventStream = ssc.receiverStream(new OrionReceiver(9001))
```

The `OrionSource` accepts a port number as a parameter. The connector will be listening through this port to data coming from Orion. These data will be in the form of a `DataStream` of `NgsiEvent` objects.

You can check the details of this object in the [connector docs](https://github.com/ging/fiware-cosmos-orion-spark-connector/blob/master/README.md#orionsource).

In the example, the first step of the processing is flat-mapping the entities. This operation is performed in order to put together the entity objects of all the NGSI Events received in a period of time.

```scala
eventStream
.flatMap(event => event.entities)
```

Once we have all the entities together, you can iterate over them (with `map`) and extract the desired attributes. In this case, we are interested in the sensor type (Door, Motion, Bell or Lamp).

```scala
// ...
.map(ent => {
        new Sensor(ent.`type`)
})
```

In each iteration, we create a custom object with the properties we need: the sensor type . For this purpose, we can define a case class like so:

```scala
case class Sensor(device: String)
```

Now we can group the created objects by the type of device and perform operations on them:

```scala
// ...
.countByValue()
```

We can provide a custom processing window, like so:

```scala
// ...
.window(Seconds(10))
```

After the processing, we can print the results on the console:

```scala
// ...
.print()
```

Or we can persist them using the sink of our choice.
Now we can run our code by hitting the play button on IntelliJ.


##### Subscribing to context changes

Once a dynamic context system is up and running (execute Example1), we need to inform **Spark** of changes in context.

This is done by making a POST request to the `/v2/subscription` endpoint of the Orion Context Broker.

- The `fiware-service` and `fiware-servicepath` headers are used to filter the subscription to only listen to measurements from the attached IoT Sensors, since they had been provisioned using these settings

- The notification `url` must match the one our Spark program is listening to. Substiture ${MY_IP} for your machine's IP address in the docker0 network (must be accesible from the docker container). You can get this IP like so (maybe yo need to use sudo):
```bash
docker network inspect bridge --format='{{(index .IPAM.Config 0).Gateway}}'
```

- The `throttling` value defines the rate that changes are sampled.

###### :one: Request:

  

```bash
curl -iX POST \
  'http://localhost:1026/v2/subscriptions' \
  -H 'Content-Type: application/json' \
  -H 'fiware-service: openiot' \
  -H 'fiware-servicepath: /' \
  -d '{
  "description": "Notify Spark of all context changes",
  "subject": {
    "entities": [
      {
      "idPattern": ".*"
      }
    ]
  },
  "notification": {
    "http": {
    "url": "http://${MY_IP}:9001"
    }
  },
  "throttling": 5
}'
```

  

The response will be `**201 - Created**`

If a subscription has been created, we can check to see if it is firing by making a GET request to the `/v2/subscriptions` endpoint.


###### :two: Request:

```bash
curl -X GET \
'http://localhost:1026/v2/subscriptions/' \
-H 'fiware-service: openiot' \
-H 'fiware-servicepath: /'
```

###### Response:


```json
[
  {
    "id": "5d76059d14eda92b0686f255",
    "description": "Notify Spark of all context changes",
    "status": "active",
    "subject": {
      "entities": [
      {
        "idPattern": ".*"
      }
      ],
      "condition": {
        "attrs": []
      }
    },
    "notification": {
      "timesSent": 362,
      "lastNotification": "2019-09-09T09:36:33.00Z",
      "attrs": [],
      "attrsFormat": "normalized",
      "http": {
        "url": "http://${MY_IP}:9001"
      },
      "lastSuccess": "2019-09-09T09:36:33.00Z",
      "lastSuccessCode": 200
    },
    "throttling": 5
  }
]
```

Within the `notification` section of the response, you can see several additional `attributes` which describe the health of the subscription

If the criteria of the subscription have been met, `timesSent` should be greater than `0`. A zero value would indicate that the `subject` of the subscription is incorrect or the subscription has created with the wrong `fiware-service-path` or `fiware-service` header

The `lastNotification` should be a recent timestamp - if this is not the case, then the devices are not regularly sending data. Remember to unlock the **Smart Door** and switch on the **Smart Lamp**

The `lastSuccess` should match the `lastNotification` date - if this is not the case then **Cosmos** is not receiving the subscription properly. Check that the hostname and port are correct.

Finally, check that the `status` of the subscription is `active` - an expired subscription will not fire.

After creating the subscription, the output on the IntelliJ console will be like the following:
```
(Sensor(Bell),1)
(Sensor(Motion),1)
(Sensor(Lamp),1)
```

#### Example 1 with NGSI-LD:

This example makes use of the NGSILDReceiver in order to receive notifications from the Orion Context Broker. Instead of NGSI v2 messages now it will log NGSI-LD messages. There is only change:

```scala
...
val eventStream = env.addSource(new NGSILDReceiver(9001))
...
```

#### Example 2:  Receiving data, performing operations and writing back to the Context Broker
The second example switches on a lamp when its motion sensor detects movement.


##### Switching on a lamp
Let's take a look at the Example2 code now:
```scala
package org.fiware.cosmos.orion.spark.connector.tutorial.example2


import org.apache.spark._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.fiware.cosmos.orion.spark.connector._
/**
  * Example2 Orion Spark Connector Tutorial
  * @author @Javierlj
  */
object Example2 {
  final val CONTENT_TYPE = ContentType.JSON
  final val METHOD = HTTPMethod.PATCH
  final val CONTENT = "{\n  \"on\": {\n      \"type\" : \"command\",\n      \"value\" : \"\"\n  }\n}"
  final val HEADERS = Map("fiware-service" -> "openiot","fiware-servicepath" -> "/","Accept" -> "*/*")

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[4]").setAppName("Temperature")
    val ssc = new StreamingContext(conf, Seconds(10))
    // Create Orion Source. Receive notifications on port 9001
    val eventStream = ssc.receiverStream(new OrionReceiver(9001))

    // Process event stream
    val processedDataStream = eventStream
      .flatMap(event => event.entities)
      .filter(entity=>(entity.attrs("count").value == "1"))
      .map(entity=> new Sensor(entity.id))
      .window(Seconds(10))

    val sinkStream= processedDataStream.map(sensor => {
      val url="http://localhost:1026/v2/entities/Lamp:"+sensor.id.takeRight(3)+"/attrs"
      OrionSinkObject(CONTENT,url,CONTENT_TYPE,METHOD,HEADERS)
    })
    // Add Orion Sink
    OrionSink.addSink( sinkStream )

    // print the results with a single thread, rather than in parallel
    processedDataStream.print()
    ssc.start()

    ssc.awaitTermination()
  }

  case class Sensor(id: String)
}
```
As you can see, it is similar to the previous example. The main difference is that it writes the processed data back in the Context Broker through the  **`OrionSink`**. 
```scala
val sinkStream= processedDataStream.map(sensor => {
      val url="http://localhost:1026/v2/entities/Lamp:"+sensor.id.takeRight(3)+"/attrs"
      OrionSinkObject(CONTENT,url,CONTENT_TYPE,METHOD,HEADERS)
})

OrionSink.addSink(sinkStream)
```
The arguments of the **`OrionSinkObject`** are:
-   **Message**: ```"{\n  \"on\": {\n      \"type\" : \"command\",\n      \"value\" : \"\"\n  }\n}"```. We send 'on' command 
-   **URL**: ```"http://localhost:1026/v2/entities/Lamp:"+node.id.takeRight(3)+"/attrs"```. TakeRight(3) gets the number of the room, for example '001')
-   **Content Type**: `ContentType.Plain`.
-   **HTTP Method**: `HTTPMethod.POST`.
-   **Headers**: `Map("fiware-service" -> "openiot","fiware-servicepath" -> "/","Accept" -> "*/*")`. Optional parameter. We add the headers we need in the HTTP Request.

##### Setting up the scenario
First we need to delete the subscription we created before:

```bash
curl -X DELETE   'http://localhost:1026/v2/subscriptions/$subscriptionId'   -H 'fiware-service: openiot'   -H 'fiware-servicepath: /'
```
You can obtain the id of your subscription by performing a GET request to the `/v2/subscriptions` endpoint.

```bash
curl -X GET   'http://localhost:1026/v2/subscriptions/'   -H 'fiware-service: openiot'   -H 'fiware-servicepath: /'
```

Now we create other subscription that will only trigger a notification when a motion sensor detects movement. Do not forget to change $MY_IP to your machine's IP address in the docker0 network as indicated earlier.

```bash
curl -iX POST \
  'http://localhost:1026/v2/subscriptions' \
  -H 'Content-Type: application/json' \
  -H 'fiware-service: openiot' \
  -H 'fiware-servicepath: /' \
  -d '{
  "description": "Notify Spark of all context changes",
  "subject": {
    "entities": [
      {
        "idPattern": "Motion.*"
      }
    ]
  },
  "notification": {
    "http": {
      "url": "http://${MY_IP}:9001/v2/notify"
    }
  },
  "throttling": 5
}'
```

You can open a door and the lamp will switch on.

### Example 3: Packaging the code and submitting it to the Spark Job Manager
In the previous examples, we've seen how to get the connector up and running from an IDE like IntelliJ. In a real case scenario, we might want to package our code and submit it to a Spark cluster in order to run our operations in parallel.

Follow the [**Setting up the scenario**](#setting-up-the-scenario) section if you haven't already in order to deploy the containers needed.
After that, we need to make some changes to our code.


##### Subscribing to notifications
First, we need to change the notification URL of our subscription to point to our Spark node like so:

```bash
curl -iX POST \
  'http://localhost:1026/v2/subscriptions' \
  -H 'Content-Type: application/json' \
  -H 'fiware-service: openiot' \
  -H 'fiware-servicepath: /' \
  -d '{
  "description": "Notify Spark of all context changes",
  "subject": {
    "entities": [
      {
        "idPattern": "Motion.*"
      }
    ]
  },
  "notification": {
    "http": {
      "url": "http://spark-master:9001"
    }
  },
  "throttling": 5
}'
```

##### Changing the code

We should replace localhost in example 2 with the orion container hostname:

* Example 2
```scala
      new OrionSinkObject(CONTENT, "http://localhost:1026/v2/entities/Lamp:"+node.id.takeRight(3)+"/attrs", CONTENT_TYPE, METHOD, HEADERS)

```
* Example 3
```scala
      new OrionSinkObject(CONTENT, "http://orion:1026/v2/entities/Lamp:"+node.id.takeRight(3)+"/attrs", CONTENT_TYPE, METHOD, HEADERS)

```

##### Packaging the code

Let's build a JAR package of the example. In it, we need to include all the dependencies we have used, such as the connector, but exclude some of the dependencies provided by the environment (Spark, Scala...).
This can be done through the `maven package` command without the `add-dependencies-for-IDEA` profile checked.
This will build a JAR file under `target/orion.spark.connector.tutorial-2.0-SNAPSHOT.jar`.


##### Submitting the job

Let's submit the Example 3 code to the Spark cluster we have deployed. In order to do this you can use the spark-submit command provided by [Spark](https://spark.apache.org/docs/latest/index.html).

```bash
./bin/spark-submit --class package org.fiware.cosmos.orion.spark.connector.tutorial.example2 /path/to/fiware-cosmos-orion-spark-connector-tutorial/job/target/orion.spark.connector.tutorial-1.2.1.jar
```

Now we can open a door and see the lamp turning on.
