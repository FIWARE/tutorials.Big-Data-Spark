[![FIWARE Banner](https://fiware.github.io/tutorials.Big-Data-Spark/img/fiware.png)](https://www.fiware.org/developers)

[![FIWARE Core Context Management](https://nexus.lab.fiware.org/static/badges/chapters/core.svg)](https://github.com/FIWARE/catalogue/blob/master/core/README.md)
[![License: MIT](https://img.shields.io/github/license/fiware/tutorials.Big-Data-Spark.svg)](https://opensource.org/licenses/MIT)
[![Support badge](https://img.shields.io/badge/tag-fiware-orange.svg?logo=stackoverflow)](https://stackoverflow.com/questions/tagged/fiware)

This tutorial is an introduction to the [FIWARE Cosmos Orion Spark Connector](http://fiware-cosmos-spark.rtfd.io), which
enables easier Big Data analysis over context, integrated with one of the most popular BigData platforms:
[Apache Spark](https://spark.apache.org/). Apache Spark is a framework and distributed processing engine for stateful
computations over unbounded and bounded data streams. Spark has been designed to run in all common cluster environments,
perform computations at in-memory speed and at any scale.

The tutorial uses [cUrl](https://ec.haxx.se/) commands throughout, but is also available as
[Postman documentation](https://www.postman.com/downloads/)

# Start-Up

## NGSI-v2 Smart Supermarket

**NGSI-v2** offers JSON based interoperability used in individual Smart Systems. To run this tutorial with **NGSI-v2**, use the `NGSI-v2` branch.

```console
git clone https://github.com/FIWARE/tutorials.Big-Data-Spark.git
cd tutorials.Big-Data-Spark
git checkout NGSI-v2

./services create
./services start
```

| [![NGSI v2](https://img.shields.io/badge/NGSI-v2-5dc0cf.svg)](https://fiware-ges.github.io/orion/api/v2/stable/) | :books: [Documentation](https://github.com/FIWARE/tutorials.Big-Data-Spark/tree/NGSI-v2) | <img src="https://cdn.jsdelivr.net/npm/simple-icons@v3/icons/postman.svg" height="15" width="15"> [Postman Collection](https://fiware.github.io/tutorials.Big-Data-Spark/) |  ![](https://img.shields.io/github/last-commit/fiware/tutorials.Big-Data-Spark/NGSI-v2) 
| --- | --- | --- | --- |

## NGSI-LD Smart Farm

**NGSI-LD** offers JSON-LD based interoperability used for Federations and Data Spaces. To run this tutorial with **NGSI-LD**, use the `NGSI-LD` branch.

```console
git clone https://github.com/FIWARE/tutorials.Big-Data-Spark.git
cd tutorials.Big-Data-Spark
git checkout NGSI-LD

./services create
./services start
```

| [![NGSI LD](https://img.shields.io/badge/NGSI-LD-d6604d.svg)](https://cim.etsi.org/NGSI-LD/official/front-page.html) | :books: [Documentation](https://github.com/FIWARE/tutorials.Big-Data-Spark/tree/NGSI-LD) | <img  src="https://cdn.jsdelivr.net/npm/simple-icons@v3/icons/postman.svg" height="15" width="15"> [Postman Collection](https://fiware.github.io/tutorials.Big-Data-Spark/ngsi-ld.html) |  ![](https://img.shields.io/github/last-commit/fiware/tutorials.Big-Data-Spark/NGSI-LD)
| --- | --- | --- | --- |

---

## License

[MIT](LICENSE) © 2020-2024 FIWARE Foundation e.V.
