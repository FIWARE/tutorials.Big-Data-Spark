
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

Smart solutions based on FIWARE are architecturally designed around microservices. They are therefore are designed to
scale-up from simple applications (such as the Supermarket tutorial) through to city-wide installations base on a large
array of IoT sensors and other context data providers.

The massive amount of data involved enventually becomes too much for a single machine to analyse, process and store, and
therefore the work must be delegated to additional distributed services. These distributed systems form the basis of
so-called **Big Data Analysis**. The distribution of tasks allows developers to be able to extract insights from huge
data sets which would be too complex to be dealt with using traditional methods. and uncover hidden patterns and
correlations.

As we have seen, context data is core to any Smart Solution, and the Context Broker is able to monitor changes of state
and raise [subscription events](https://github.com/Fiware/tutorials.Subscriptions) as the context changes. For smaller
installations, each subscription event can be processed one-by-one by a single receiving endpoint, however as the system
grows, another technique will be required to avoid overwhelming the listener, potentially blocking resources and missing
updates.

**Apache Spark** is a Java/Scala based stream-processing framework which enables the delegation of data-flow processes.
Therefore additional computational resources can be called upon to deal with data as events arrive. The **Cosmos Spark**
connector allows developers write custom business logic to listen for context data subscription events and then process
the flow of the context data. Spark is able to delegate these actions to other workers where they will be acted upon
either in sequentiallly or in parallel as required. The data flow processing itself can be arbitrarily complex.

Obviously in reality our existing Supermarket scenario is far too small to require the use of a Big Data solution, but
will serve as a basis for demonstrating the type of real-time processing which may be required in a larger solution
which is processing a continuous stream of context-data events.

 
# Architecture

This application builds on the components and dummy IoT devices created in
[previous tutorials](https://github.com/FIWARE/tutorials.IoT-Agent/). It will make use of three FIWARE components - the
[Orion Context Broker](https://fiware-orion.readthedocs.io/en/latest/), the
[IoT Agent for Ultralight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/), and the
[Cosmos Orion Spark Connector](https://fiware-cosmos-spark.readthedocs.io/en/latest/) for connecting Orion to an
[Apache Spar cluster](https://spark.apache.org/docs/latest/cluster-overview.html). The Spark cluster
itself will consist of a single **Cluster Manager** _master_ to coordinate execution and some **Worker Nodes** _worker_ to
execute the tasks.

Both the Orion Context Broker and the IoT Agent rely on open source [MongoDB](https://www.mongodb.com/) technology to
keep persistence of the information they hold. We will also be using the dummy IoT devices created in the
[previous tutorial](https://github.com/FIWARE/tutorials.IoT-Agent/).

Therefore the overall architecture will consist of the following elements:

-   Two **FIWARE Generic Enablers** as independent microservices:
    -   The FIWARE [Orion Context Broker](https://fiware-orion.readthedocs.io/en/latest/) which will receive requests
        using [NGSI](https://fiware.github.io/specifications/OpenAPI/ngsiv2)
    -   The FIWARE [IoT Agent for Ultralight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/) which will
        receive northbound measurements from the dummy IoT devices in
        [Ultralight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/usermanual/index.html#user-programmers-manual)
        format and convert them to [NGSI](https://fiware.github.io/specifications/OpenAPI/ngsiv2) requests for the
        context broker to alter the state of the context entities
-   An [Apache Spark cluster](https://spark.apache.org/docs/latest/cluster-overview.html) consisting
    of a single **ClusterManager** and **Worker Nodes**
    -   The FIWARE [Cosmos Orion Spark Connector](https://fiware-cosmos-spark.readthedocs.io/en/latest/) will be
        deployed as part of the dataflow which will subscribe to context changes and make operations on them in
        real-time
-   One [MongoDB](https://www.mongodb.com/) **database** :
    -   Used by the **Orion Context Broker** to hold context data information such as data entities, subscriptions and
        registrations
    -   Used by the **IoT Agent** to hold device information such as device URLs and Keys
-   Three **Context Providers**:
    -   A webserver acting as set of [dummy IoT devices](https://github.com/FIWARE/tutorials.IoT-Sensors) using the
        [Ultralight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/usermanual/index.html#user-programmers-manual)
        protocol running over HTTP.
    -   The **Stock Management Frontend** is not used in this tutorial. It does the following:
        -   Display store information and allow users to interact with the dummy IoT devices
        -   Show which products can be bought at each store
        -   Allow users to "buy" products and reduce the stock count.
    -   The **Context Provider NGSI** proxy is not used in this tutorial. It does the following:
        -   receive requests using [NGSI](https://fiware.github.io/specifications/OpenAPI/ngsiv2)
        -   makes requests to publicly available data sources using their own APIs in a proprietary format
        -   returns context data back to the Orion Context Broker in
            [NGSI](https://fiware.github.io/specifications/OpenAPI/ngsiv2) format.

## Spark Cluster Configuration

```yaml
spark-master:
    image: bde2020/spark-master:2.4.5-hadoop2.7
    container_name: spark-master
    ports:
      - "8080:8080"
      - "7077:7077"
      - "9001:9001"
    environment:
      - INIT_DAEMON_STEP=setup_spark
      - "constraint:node==spark-master"
```

```yaml
spark-worker-1:
    image: bde2020/spark-worker:2.4.5-hadoop2.7
    container_name: spark-worker-1
    depends_on:
      - spark-master
    ports:
      - "8081:8081"
    environment:
      - "SPARK_MASTER=spark://spark-master:7077"
      - "constraint:node==spark-master"
```

The `spark-master` container is listening on three ports:

-   Port `8080` is exposed so we can see the web frontend of the Apache Spark-Master Dashboard.
-   Port `9001` is exposed so that the installation can receive context data subscriptions.
-   Port `7070` is used for internal communications.

The `spark-worker-1` container is listening on one port:

-   Ports `8081` is exposed so we can see the web frontend of the Apache Spark-Worker-1 Dashboard.

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


# Real-time Processing Operations

Dataflow within **Apache Flink** is defined within the
[Flink documentation](https://ci.apache.org/projects/flink/flink-docs-release-1.9/concepts/programming-model.html) as
follows:

> "The basic building blocks of Flink programs are streams and transformations. Conceptually a stream is a (potentially
> never-ending) flow of data records, and a transformation is an operation that takes one or more streams as input, and
> produces one or more output streams as a result.
>
> When executed, Flink programs are mapped to streaming dataflows, consisting of streams and transformation operators.
> Each dataflow starts with one or more sources and ends in one or more sinks. The dataflows resemble arbitrary directed
> acyclic graphs (DAGs). Although special forms of cycles are permitted via iteration constructs, for the most part this
> can be glossed over this for simplicity."

![](https://fiware.github.io/tutorials.Big-Data-Analysis/img/streaming-dataflow.png)

This means that to create a streaming data flow we must supply the following:

-   A mechanism for reading Context data as a **Source Operator**
-   Business logic to define the transform operations
-   A mechanism for pushing Context data back to the context broker as a **Sink Operator**

The `orion-flink.connect.jar` offers both **Source** and **Sink** operations. It therefore only remains to write the
necessary Scala code to connect the streaming dataflow pipeline operations together. The processing code can be complied
into a JAR file which can be uploaded to the flink cluster. Two examples will be detailed below, all the source code for
this tutorial can be found within the
[cosmos-examples](https://github.com/FIWARE/tutorials.Big-Data-Analysis/tree/master/cosmos-examples) directory.

Further Flink processing examples can be found on the
[Apache Flink site](https://ci.apache.org/projects/flink/flink-docs-release-1.9/getting-started) and
[Flink Connector Examples](https://fiware-cosmos-flink-examples.readthedocs.io/).

### Compiling a JAR file for Spark

An existing `pom.xml` file has been created which holds the necessary prerequisites to build the examples JAR file

In order to use the Orion Spark Connector we first need to manually install the connector JAR as an artifact using
Maven:

```console
cd cosmos-examples
mvn install:install-file \
  -Dfile=./orion.spark.connector-1.2.1.jar \
  -DgroupId=org.fiware.cosmos \
  -DartifactId=orion.spark.connector \
  -Dversion=1.2.1 \
  -Dpackaging=jar
```

Thereafter the source code can be compiled by running the `mvn package` command within the same directory:

```console
cd cosmos-examples
mvn package
```

A new JAR file called `cosmos-examples-1.1.jar` will be created within the `cosmos-examples/target` directory.

### Generating a stream of Context Data

For the purpose of this tutorial, we must be monitoring a system in which the context is periodically being updated. The
dummy IoT Sensors can be used to do this. Open the device monitor page at `http://localhost:3000/device/monitor` and
unlock a **Smart Door** and switch on a **Smart Lamp**. This can be done by selecting an appropriate the command from
the drop down list and pressing the `send` button. The stream of measurements coming from the devices can then be seen
on the same page:

![](https://fiware.github.io/tutorials.Historic-Context-NIFI/img/door-open.gif)
  

## Example 1: Receiving data and performing operations

The first example makes use of the `OrionSource` operator in order to receive notifications from the Orion Context
Broker. Specifically, the example counts the number notifications that each type of device sends in one minute. You can
find the source code of the example in
[org/fiware/cosmos/tutorial/Logger.scala](https://github.com/ging/fiware-cosmos-orion-spark-connector-tutorial/blob/master/cosmos-examples/src/main/scala/org/fiware/cosmos/tutorial/Logger.scala)

### Logger - Installing the JAR


### Logger - Subscribing to context changes

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
    "url": "http://spark-master:9001"
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
        "url": "http://spark-master:9001"
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

### Logger - Checking the Output

Leave the subscription running for **one minute**, then run the following:

```console
docker logs spark-master -f --until=60s > stdout.log 2>stderr.log
cat stderr.log
```

After creating the subscription, the output on the console will be like the following:

```text
Sensor(Bell,3)
Sensor(Door,4)
Sensor(Lamp,7)
Sensor(Motion,6)
```

### Logger - Analyzing the Code

```scala 

package org.fiware.cosmos.orion.spark.connector.tutorial

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.fiware.cosmos.orion.spark.connector.OrionReceiver

/**
  * Example1 Orion Spark Tutorial
 *
  * @author @Javierlj
  */
object Logger{

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[4]").setAppName("Logger")
    val ssc = new StreamingContext(conf, Seconds(5))
    // Create Orion Source. Receive notifications on port 9001
    val eventStream = ssc.receiverStream(new OrionReceiver(9001))

    // Process event stream
    val processedDataStream= eventStream
      .flatMap(event => event.entities)
      .map(ent => {
        new Sensor(ent.`type`)
      })
      .countByValue()
      .window(Seconds(10))

    processedDataStream.print()

    ssc.start()
    ssc.awaitTermination()
  }
  case class Sensor(device: String)
}
```

The first lines of the program are aimed at importing the necessary dependencies, including the connector. The next step
is to create an instance of the `OrionReceiver` using the class provided by the connector and to add it to the environment
provided by Flink.

The `OrionSource` constructor accepts a port number (`9001`) as a parameter. This port is used to listen to the
subscription notifications coming from Orion and converted to a `DataStream` of `NgsiEvent` objects. The definition of
these objects can be found within the
[Orion-Spark Connector documentation](https://github.com/ging/fiware-cosmos-orion-spark-connector/blob/master/README.md#orionsource).

The stream processing consists of five separate steps. The first step (`flatMap()`) is performed in order to put
together the entity objects of all the NGSI Events received in a period of time. Thereafter the code iterates over them
(with the `map()` operation) and extracts the desired attributes. In this case, we are interested in the sensor `type`
(`Door`, `Motion`, `Bell` or `Lamp`).

Within each iteration, we create a custom object with the property we need: the sensor `type. For this purpose, we can define a case class as shown:

```scala
case class Sensor(device: String)
```

Therefter can count the created objects by the type of device (`countByValue()`) and perform operations such as
`window()` on them.

After the processing, the results are output to the console:

```scala
processedDataStream.print()
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
