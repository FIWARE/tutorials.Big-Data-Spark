# Big Data Analysis (Spark)[<img src="https://img.shields.io/badge/NGSI-LD-d6604d.svg" width="90"  align="left" />](https://www.etsi.org/deliver/etsi_gs/CIM/001_099/009/01.08.01_60/gs_cim009v010801p.pdf)[<img src="https://fiware.github.io/tutorials.Big-Data-Flink/img/fiware.png" align="left" width="162">](https://www.fiware.org/)<br/>

[![FIWARE Core Context Management](https://nexus.lab.fiware.org/static/badges/chapters/core.svg)](https://github.com/FIWARE/catalogue/blob/master/processing/README.md)
[![License: MIT](https://img.shields.io/github/license/fiware/tutorials.Big-Data-Spark.svg)](https://opensource.org/licenses/MIT)
[![Support badge](https://nexus.lab.fiware.org/repository/raw/public/badges/stackoverflow/fiware.svg)](https://stackoverflow.com/questions/tagged/fiware)
<br/> [![Documentation](https://img.shields.io/readthedocs/fiware-tutorials.svg)](https://fiware-tutorials.rtfd.io)

このチュートリアルは、[FIWARE Cosmos Orion Spark Connector](http://fiware-cosmos-spark.rtfd.io) の概要です。これにより、
最も人気のある BigData プラットフォームの1つである [Apache Spark](https://spark.apache.org/) と統合され、
コンテキスト全体でビッグデータ分析が容易になります。Apache Spark は、制限のないデータ・ストリームと制限のあるデータ・
ストリームでステートフルな計算を行うためのフレームワークおよび分散処理エンジンです。Spark は、すべての一般的な
クラスタ環境で実行され、メモリ内の速度と任意のスケールで計算を実行するように設計されています。

チュートリアルでは全体で [cUrl](https://ec.haxx.se/) コマンドを使用しますが、Postman のドキュメントとしても利用できます:

[![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/e7c16fce79fa081ba529)
[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/FIWARE/tutorials.Big-Data-Spark/tree/NGSI-LD)

## コンテンツ

<details>
<summary><strong>詳細</strong></summary>

-   [リアルタイム処理とビッグデータ分析](#real-time-processing-and-big-data-analysis)
-   [アーキテクチャ](#architecture)
    -   [Spark Cluster の構成](#spark-cluster-configuration)
-   [前提条件](#prerequisites)
    -   [Docker および Docker Compose](#docker-and-docker-compose)
    -   [Maven](#maven)
    -   [Cygwin for Windows](#cygwin-for-windows)
    -   [Java JDK](#java-jdk)
    -   [Scala](#scala)
-   [起動](#start-up)
-   [リアルタイム処理操作](#real-time-processing-operations)
    -   [Spark 用の JAR ファイルのコンパイル](#compiling-a-jar-file-for-spark)
    -   [コンテキスト・データのストリームの生成](#generating-a-stream-of-context-data)
    -   [ロガー - コンテキスト・データのストリームの読み取り](#logger---reading-context-data-streams)
        -   [ロガー - JAR のインストール](#logger---installing-the-jar)
        -   [ロガー - コンテキスト変更のサブスクライブ](#logger---subscribing-to-context-changes)
        -   [ロガー - 出力の確認](#logger---checking-the-output)
        -   [ロガー - コードの分析](#logger---analyzing-the-code)
    -   [フィードバック・ループ - コンテキスト・データの永続化](#feedback-loop---persisting-context-data)
        -   [フィードバック・ループ - JAR のインストール](#feedback-loop---installing-the-jar)
        -   [フィードバック・ループ - コンテキスト変更のサブスクライブ](#feedback-loop---subscribing-to-context-changes)
        -   [フィードバック・ループ - 出力の確認](#feedback-loop---checking-the-output)
        -   [フィードバック・ループ - コードの分析](#feedback-loop---analyzing-the-code)

</details>

<a name="real-time-processing-and-big-data-analysis"/>

# リアルタイム処理とビッグデータ分析

> "You have to find what sparks a light in you so that you in your own way can illuminate the world."
>
> — Oprah Winfrey

FIWARE に基づくスマート・ソリューションは、マイクロサービスを中心に設計されています。したがって、シンプルな
アプリケーション (スーパーマーケット・チュートリアルなど) から、IoT センサやその他のコンテキスト・データ・プロバイダの
大規模な配列に基づく都市全体のインストールにスケールアップするように設計されています。

関与する膨大な量のデータは、1台のマシンで分析、処理、保存するには膨大な量になるため、追加の分散サービスに作業を委任する
必要があります。これらの分散システムは、いわゆる **ビッグデータ分析** の基礎を形成します。タスクの分散により、開発者は、
従来の方法では処理するには複雑すぎる巨大なデータ・セットから洞察を抽出することができます。隠れたパターンと相関関係を
明らかにします。

これまで見てきたように、コンテキスト・データはすべてのスマート・ソリューションの中核であり、Context Broker は状態の変化を
監視し、コンテキストの変化に応じて、[サブスクリプション・イベント](https://github.com/Fiware/tutorials.Subscriptions)
を発生させることができます。小規模なインストールの場合、各サブスクリプション・イベントは単一の受信エンドポイントで1つずつ
処理できますが、システムが大きくなると、リスナーを圧倒し、潜在的にリソースをブロックし、更新が失われないようにするために
別の手法が必要になります。

**Apache Spark** は、オープンソースの分散型汎用クラスタ・コンピューティング・フレームワークです。 暗黙的なデータ並列性と
フォールト・トレランスを備えたクラスタ全体をプログラミングするためのインターフェイスを提供します。 **Cosmos Spark**
コネクタを使用すると、開発者はカスタム・ビジネス・ロジックを記述して、コンテキスト・データのサブスクリプション・イベント
をリッスンし、コンテキスト・データのフローを処理できます。Spark は、これらのアクションを他のワーカーに委任して、
必要に応じて順次または並行してアクションを実行することができます。 データ・フロー処理自体は、任意に複雑になる可能性が
あります。

実際には、明らかに、既存のスーパーマーケットのシナリオは小さすぎて、ビッグデータ・ソリューションの使用を必要としません。
しかし、コンテキスト・データ・イベントの連続ストリームを処理する大規模なソリューションで必要になる可能性のある
リアルタイム処理のタイプを示すための基礎として機能します。

<a name="architecture"/>

# アーキテクチャ

このアプリケーションは、[以前のチュートリアル](https://github.com/FIWARE/tutorials.IoT-Agent/)で作成されたコンポーネントと
ダミー IoT デバイス上に構築されます。 3つの FIWARE コンポーネントを使用します。
[Orion Context Broker](https://fiware-orion.readthedocs.io/en/latest/),
[IoT Agent for Ultralight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/) および Orion を
[Apache Spark cluster](https://spark.apache.org/docs/latest/cluster-overview.html) に接続するための
[Cosmos Orion Spark Connector](https://fiware-cosmos-spark.readthedocs.io/en/latest/) です。Spark クラスタ自体は、
実行を調整する単一の **Cluster Manager** _master_ と、タスクを実行する単一の **Worker Nodes** _worker_ で構成されます。

Orion Context Broker と IoT Agent はどちらも、オープンソースの [MongoDB](https://www.mongodb.com/) テクノロジーに依存して、
保持している情報の永続性を維持しています。また、[以前のチュートリアル](https://github.com/FIWARE/tutorials.IoT-Agent/)で
作成したダミー IoT デバイスを使用します。

したがって、全体的なアーキテクチャは次の要素で構成されます :

-   独立したマイクロサービスとしての2つの **FIWARE Generic Enablers** :
    -   FIWARE [Orion Context Broker](https://fiware-orion.readthedocs.io/en/latest/) は、
        [NGSI-LD](https://forge.etsi.org/swagger/ui/?url=https://forge.etsi.org/rep/NGSI-LD/NGSI-LD/raw/master/spec/updated/generated/full_api.json)
        を使用してリクエストを受信します
    -   FIWARE [IoT Agent for UltraLight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/) は、
        [NGSI-LD](https://forge.etsi.org/swagger/ui/?url=https://forge.etsi.org/rep/NGSI-LD/NGSI-LD/raw/master/spec/updated/generated/full_api.json)
        を使用してサウスバウンド・リクエストを受信し、それらをデバイス用の
        [UltraLight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/usermanual/index.html#user-programmers-manual)
        コマンドに変換します
-   [Apache Spark cluster](https://spark.apache.org/docs/latest/cluster-overview.html) は、
    単一の **ClusterManager** と **Worker Nodes** で構成されます
    -   FIWARE [Cosmos Orion Spark Connector](https://fiware-cosmos-spark.readthedocs.io/en/latest/) は、
        コンテキストの変更をサブスクライブし、リアルタイムで操作を行うデータフローの一部としてデプロイされます
-   1つの [MongoDB](https://www.mongodb.com/) **データベース** :
    -   **Orion Context Broker** がデータ・エンティティ、サブスクリプション、レジストレーションなどの
        コンテキスト・データ情報を保持するために使用します
    -   **IoT Agent** がデバイスの URL やキーなどのデバイス情報を保持するために使用します
-   HTTP **Web-Server** は、システム内のコンテキスト・エンティティを定義する静的な `@context` ファイルを提供します
-   **チュートリアル・アプリケーション** は次のことを行います:
    -   [UltraLight 2.0](https://fiware-iotagent-ul.readthedocs.io/en/latest/usermanual/index.html#user-programmers-manual)
        を使用してダミーの[農業用 IoT デバイス](https://github.com/FIWARE/tutorials.IoT-Sensors/tree/NGSI-LD)
        のセットとして機能します

全体のアーキテクチャを以下に示します :

![](https://fiware.github.io/tutorials.Big-Data-Spark/img/Tutorial%20FIWARE%20Spark.png)

<a name="spark-cluster-configuration"/>

## Spark Cluster の構成

```yaml
spark-master:
    image: bde2020/spark-master:2.4.5-hadoop2.7
    container_name: spark-master
    expose:
        - "8080"
        - "9001"
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

`spark-master` コンテナは、三つのポートでリッスンしています:

-   ポート `8080` は、Apache Spark-Master ダッシュボードの Web フロントエンドを見ることができる
    ように、公開されます

-   ポート `7070` は内部通信に使用されます

`spark-worker-1` コンテナは、1つのポートで待機しています:

-   ポート `9001` は、インストレーションがコンテキスト・データのサブスクリプションを受信できるように、
    公開されます
-   ポート `8081` は、Apache Spark-Worker-1 ダッシュボードの Web フロントエンドを見ることができる
　　ように、公開されます

<a name="prerequisites"/>

# 前提条件

<a name="docker-and-docker-compose"/>

## Docker および Docker Compose

物事を単純にするために、すべてのコンポーネントは [Docker](https://www.docker.com) を使用して実行されます。
**Docker** は、さまざまなコンポーネントをそれぞれの環境に分離できるようにするコンテナ・テクノロジーです。

-   Windows に Docker をインストールするには、[こちら](https://docs.docker.com/docker-for-windows/)の指示に従って
    ください
-   Mac に Docker をインストールするには、[こちら](https://docs.docker.com/docker-for-mac/)の指示に従ってください
-   Linux に Docker をインストールするには、[こちら](https://docs.docker.com/install/)の指示に従ってください

**Docker Compose** は、マルチ・コンテナ Docker アプリケーションを定義および実行するためのツールです。一連の
[YAML files](https://github.com/FIWARE/tutorials.Big-Data-Spark/blob/NGSI-LD/docker-compose.yml) は、アプリケーション
に必要なサービスを構成するために使用されます。これは、すべてのコンテナ・サービスを単一のコマンドで起動できることを
意味します。Docker Compose は、デフォルトで Docker for Windows および Docker for Mac の一部としてインストール
されますが、Linux ユーザは[こちら](https://docs.docker.com/compose/install/)にある指示に従う必要があります。

次のコマンドを使用して、現在の **Docker** および **Docker Compose** バージョンを確認できます :

```console
docker-compose -v
docker version
```

Docker バージョン 20.10 以降および Docker Compose 1.29 以降を使用していることを確認し、必要に応じてアップグレード
してください。

<a name="maven"/>

## Maven

[Apache Maven](https://maven.apache.org/download.cgi) は、ソフトウェア・プロジェクト管理ツールです。プロジェクト・
オブジェクト・モデル (POM) の概念に基づいて、Maven は情報の中心部分からプロジェクトのビルド、レポート、および
ドキュメントを管理できます。Maven を使用して、依存関係を定義およびダウンロードし、コードをビルドして JAR ファイルに
パッケージ化します。

<a name="cygwin-for-windows"/>

## Cygwin for Windows

簡単な Bash スクリプトを使用してサービスを開始します。Windows ユーザは、[cygwin](http://www.cygwin.com/) を
ダウンロードして、Windows 上の Linux ディストリビューションに類似したコマンドライン機能を提供する必要があります。

<a name="java-jdk"/>

## Java JDK

Apache Spark コネクタの現在のバージョンは、Apache Spark v2.4.5 に基づいています。Spark2.x は **Scala 2.11** で
事前に構築されていることに注意してください。このバージョンの Scala は、**Java 8 JDK** または **Java 11 JDK**を
使用します。詳細については、
[Scala JDKの互換性](https://docs.scala-lang.org/overviews/jdk-compatibility/overview.html?_ga=2.173507616.2062103704.1616863323-566380632.1616863323)
を参照してください

次のコマンドを実行するだけで、インストールされている Java の現在のバージョンを確認できます:

```console
java -version
```

Java 8 JDK をインストールするには、
[Java SE Development Kit 8 Downloads](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html)
を確認してください。Java の複数のバージョンをすでにインストールしている場合は、**`JAVA_HOME`** 変数をインストールした
フォルダに変更するだけで、それらを切り替えることができます。MacOS で次のコマンドを実行すると、システムで使用可能な
さまざまなバージョンを確認できます:

```console
/usr/libexec/java_home -V
```

次の情報を取得できます:

```console
Matching Java Virtual Machines (2):
11.0.1, x86_64: "Java SE 11.0.1" /Library/Java/JavaVirtualMachines/jdk-11.0.1.jdk/Contents/Home
1.8.0_201, x86_64: "Java SE 8" /Library/Java/JavaVirtualMachines/jdk1.8.0_201.jdk/Contents/Home
```

ほとんどの Linux ディストリビューションでは、次のような update-alternatives を使用できます:

```console
sudo update-alternatives --config java
```

次のような情報を取得できます:

```console
There is only one alternative in link group java (providing /usr/bin/java): /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java
Nothing to configure.
```

バージョンを選択するには、パスの値を `**JAVA_HOME**` 変数に割り当てるだけです。

```console
export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
```

<a name="scala"/>

## Scala

前述のとおり、Apache Spark コネクタの現在のバージョンは Apache Spark v2.4.5 に基づいています。 Spark2.x は
**Scala 2.11** で事前に構築されていることに注意してください。CLI から scala で作業を実行するには、sbt を
インストールすることをお勧めします。詳細は、
[Installing Scala 2.11.12](https://www.scala-lang.org/download/2.11.12.html)
を参照ください。

次のコマンドを実行して、scala のバージョンを確認できます:

```console
scala --version
```

<a name="start-up"/>

# 起動

開始する前に、必要な Docker イメージをローカルで取得または構築したことを確認する必要があります。以下に示すコマンド
を実行して、リポジトリを複製し、必要なイメージを作成してください。いくつかのコマンドを特権ユーザとして実行する
必要がある場合があることに注意してください :

```console
git clone https://github.com/FIWARE/tutorials.Big-Data-Spark.git
cd tutorials.Big-Data-Spark
git checkout NGSI-LD
./services create
```

このコマンドは、以前のチュートリアルからシードデータをインポートし、起動時にダミー IoT センサをプロビジョニング
します。

システムを起動するには、次のコマンドを実行します:

```console
./services [orion|scorpio|stellio]
```

> :information_source: **注 :** クリーンアップしてやり直す場合は、次のコマンドを使用します :
>
> ```console
> ./services stop
> ```

<a name="real-time-processing-operations"/>

# リアルタイム処理操作

[Apache Spark のドキュメント](https://spark.apache.org/documentation.html) によると、Spark Streaming はコア Spark API
の拡張であり、ライブ・データ・ストリームのスケーラブルで高スループットのフォールト・トレラントなストリーム処理を
可能にします。データは、Kafka, Flume, Kinesis, TCP ソケットなどの多くのソースから取り込むことができ、map, reduce, join,
window などの高レベル関数で表現された複雑なアルゴリズムを使用して処理できます。最後に、処理されたデータを
ファイルシステム、データベース、およびライブ・ダッシュボードにプッシュできます。
実際、Spark の機械学習とグラフ処理アルゴリズムをデータ・ストリームに適用できます。

![](https://spark.apache.org/docs/latest/img/streaming-arch.png)

内部的には、次のように動作します。Spark Streaming は、ライブ入力データ・ストリームを受信し、データをバッチに分割します。
バッチは、Spark エンジンによって処理され、結果の最終ストリームがバッチで生成されます。

![](https://spark.apache.org/docs/latest/img/streaming-flow.png)

これは、ストリーミング・データフローを作成するには、以下を提供する必要があることを意味します:

-   **Source Operator** としてコンテキスト・データを読み取るためのメカニズム
-   変換操作 (transform operations) を定義するビジネスロジック
-   **Sink Operator** としてコンテキスト・データを Context Broker にプッシュバックするメカニズム

**Cosmos Spark** connector - `orion.spark.connector-1.2.2.jar` は、**Source** および **Sink** operators の両方を
提供します。したがって、ストリーミング・データフロー・パイプライン操作を相互に接続するために必要な Scala コードを
記述するだけです。処理コードは、Spark クラスタにアップロードできる JAR ファイルにコンパイルできます。
以下に2つの例を詳しく説明します。このチュートリアルのすべてのソースコードは、
[cosmos-examples](https://github.com/ging/fiware-cosmos-orion-spark-connector-tutorial/tree/master/cosmos-examples)
ディレクトリにあります。

その他の Spark 処理の例は、
[Spark Connector の例](https://fiware-cosmos-spark-examples.readthedocs.io) にあります。

<a name="compiling-a-jar-file-for-spark"/>

### Spark 用の JAR ファイルのコンパイル

サンプル JAR ファイルをビルドするために必要な前提条件を保持する既存の `pom.xml` ファイルが作成されました。

Orion Spark Connector を使用するには、最初に Maven を使用してアーティファクトとしてコネクタ JAR
を手動でインストールする必要があります:

```console
cd cosmos-examples
curl -LO https://github.com/ging/fiware-cosmos-orion-spark-connector/releases/download/FIWARE_7.9.2/orion.spark.connector-1.2.2.jar
mvn install:install-file \
  -Dfile=./orion.spark.connector-1.2.2.jar \
  -DgroupId=org.fiware.cosmos \
  -DartifactId=orion.spark.connector \
  -Dversion=1.2.2 \
  -Dpackaging=jar
```

> :information_source: **注:** コマンド `./services create` を実行すると、スクリプトは対応する
> `orion.spark.connector-1.2.2.jar` ファイルを `cosmos-example` フォルダに自動的にダウンロードします。

その後、同じディレクトリ (`cosmos-examples`) 内で `mvn package` コマンドを実行することにより、
ソースコードをコンパイルできます:

```console
mvn package
```

`cosmos-examples-1.2.2.jar` と呼ばれる新しい JAR ファイルが `cosmos-examples/target` ディレクトリ内に作成されます。

<a name="generating-a-stream-of-context-data"/>

### コンテキスト・データのストリームの生成

このチュートリアルでは、コンテキストが定期的に更新されているシステムを監視する必要があります。 ダミー IoT センサを
使用してこれを行うことができます。`http://localhost:3000/device/monitor` でデバイス・モニタ・ページを開き、
**Tractor** の移動を開始します。これは、ドロップ・ダウン・リストから適切なコマンド (**Start Tractor**) を選択し、
`send` ボタンを押すことで実行できます。デバイスからの測定値の流れは、同じページで見ることができます:

![](https://fiware.github.io/tutorials.Big-Data-Spark/img/farm-devices.png)

> :information_source: **注:** デフォルトでは、ポート 3000 を使用してダミー IoT センサにアクセスしています。
> この情報は、`.env` 構成ファイルで詳しく説明されています。このポートですでにサービスを実行している場合は、
> このポートを変更できます。

<a name="logger---reading-context-data-streams"/>

## ロガー - コンテキスト・データのストリームの読み取り

最初の例では、Orion Context Broker からノーティフィケーションを受信するために `OrionReceiver` operator を使用します。
具体的には、この例では、各タイプのデバイスが1分間に送信するノーティフィケーションの数をカウントします。
この例のソースコードは、
[org/fiware/cosmos/tutorial/Logger.scala](https://github.com/ging/fiware-cosmos-orion-spark-connector-tutorial/blob/master/cosmos-examples/src/main/scala/org/fiware/cosmos/tutorial/Logger.scala)
にあります。

<a name="logger---installing-the-jar"/>

### ロガー - JAR のインストール

必要に応じてコンテナを再起動し、ワーカー・コンテナにアクセスします:

```console
docker exec -it spark-worker-1 bin/bash
```

そして、次のコマンドを実行して、生成された JAR パッケージを Spark クラスタで実行します:

```console
/spark/bin/spark-submit \
--class  org.fiware.cosmos.tutorial.LoggerLD \
--master  spark://spark-master:7077 \
--deploy-mode client /home/cosmos-examples/target/cosmos-examples-1.2.2.jar \
--conf "spark.driver.extraJavaOptions=-Dlog4jspark.root.logger=WARN,console"
```

<a name="logger---subscribing-to-context-changes"/>

### ロガー - コンテキスト変更のサブスクライブ

動的コンテキスト・システムが稼働し始めたら (Spark クラスタに `Logger` ジョブをデプロイしました)、コンテキストの変更を
**Spark** にノーティフィケーションする必要があります。

これは、Orion Context Broker の `/v2/subscription` エンドポイントに POST リクエストを行うことによって行われます。

-   `NGSILD-Tenant` ヘッダは、これらの設定を使用してプロビジョニングされているため、接続された IoT センサからの測定値
    のみをリッスンするようにサブスクリプションをフィルタリングするために使用されます

-   ノーティフィケーション `uri` は、Spark プログラムがリッスンしているものと一致する必要があります

-   この `throttling` 値は、変更がサンプリングされるレートを定義します

別のターミナルを開き、次のコマンドを実行します:

#### 1️⃣ リクエスト:

```console
curl -L -X POST 'http://localhost:1026/ngsi-ld/v1/subscriptions/' \
-H 'Content-Type: application/ld+json' \
-H 'NGSILD-Tenant: openiot' \
--data-raw '{
  "description": "Notify Spark of all animal and farm vehicle movements",
  "type": "Subscription",
  "entities": [{"type": "Tractor"}, {"type": "Device"}],
  "watchedAttributes": ["location"],
  "notification": {
    "attributes": ["location"],
    "format": "normalized",
    "endpoint": {
      "uri": "http://spark-worker-1:9001",
      "accept": "application/json"
    }
  },
   "@context": "http://context/ngsi-context.jsonld"
}'
```

レスポンスは、**`201 - Created`** です。

サブスクリプションが作成されている場合は、`/ngsi-ld/v1/subscriptions/` エンドポイントに対して GET リクエストを行うことで、
サブスクリプションが起動しているかどうかを確認できます。

#### 2️⃣ リクエスト:

```console
curl -X GET \
'http://localhost:1026/ngsi-ld/v1/subscriptions/' \
-H 'NGSILD-Tenant: openiot'
```

#### レスポンス:

```json
[
    {
        "id": "urn:ngsi-ld:Subscription:605f91e42bccb32d0b6b44ed",
        "type": "Subscription",
        "description": "Notify Spark of all animal and farm vehicle movements",
        "entities": [
            {
                "type": "Tractor"
            },
            {
                "type": "Device"
            }
        ],
        "watchedAttributes": ["location"],
        "notification": {
            "attributes": ["location"],
            "format": "normalized",
            "endpoint": {
                "uri": "http://spark-worker-1:9001",
                "accept": "application/json"
            },
            "timesSent": 47,
            "lastNotification": "2021-03-27T20:13:52.668Z"
        },
        "@context": "http://context/ngsi-context.jsonld"
    }
]
```

レスポンスの `notification` セクション内に、サブスクリプションの状態を説明するいくつかの追加 `attributes` が表示されます。

サブスクリプションの基準が満たされている場合は、`timesSent` は、`0` より大きい必要があります。ゼロの場合は、
サブスクリプションの `subject` が正しくないか、サブスクリプションが間違った `NGSILD-Tenant` ヘッダで作成されたことを
示します。

`lastNotification` は、最近のタイムスタンプでなければなりません。そうでない場合は、デバイスが定期的にデータを
送信していません。**Tractor** を動かしてスマート・ファームをアクティブ化することを忘れないでください。

`lastSuccess` は、`lastNotification` date に一致している必要があります。そうでない場合は、**Cosmos** は、
適切にサブスクリプションを受信していません。ホスト名とポートが正しいことを確認してください。

最後に、サブスクリプションの `status` が `active` であるかどうかを確認します。期限切れのサブスクリプションは起動しません。

<a name="logger---checking-the-output"/>

### ロガー - 出力の確認

サブスクリプションを**1分間**実行したままにします。次に、Spark ジョブを実行したコンソールでの出力は次のようになります:

```text
Sensor(Tractor,19)
Sensor(Device,49)
```

<a name="logger---analyzing-the-code"/>

### ロガー - コードの分析

```scala
package org.fiware.cosmos.tutorial
import org.apache.spark._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.fiware.cosmos.orion.spark.connector._


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
```

プログラムの最初の行は、コネクタを含む必要な依存関係をインポートすることを目的としています。次のステップは、
コネクタによって提供されるクラスを使用して `NGSILDReceiver` のインスタンスを作成し、それを Spark
によって提供される環境に追加することです。

`NGSILDReceiver` コンストラクタは、パラメータとしてポート番号 (`9001`) を受け入れます。このポートは、Orion から来る
サブスクリプションのノーティフィケーションをリッスンするために使用され、`NgsiEvent` オブジェクトの `DataStream`
に変換されます。これらのオブジェクトの定義は、
[Orion-Spark Connector ドキュメント](https://github.com/ging/fiware-cosmos-orion-spark-connector/blob/master/README.md#orionreceiver)
に記載されています。

ストリーム処理は、5つの別々のステップで構成されています。最初のステップ (`flatMap()`) は、一定期間に受信したすべての
NGSI イベントのエンティティ・オブジェクトをまとめるために実行されます。その後、コードは (`map()` 操作を使用して)
それらを反復処理し、目的の属性を抽出します。この場合、我々は、センサ `type` (`Device` または `Tractor`)
に興味があります。

各反復内で、必要なプロパティであるセンサ `type` を使用してカスタム・オブジェクトを作成します。この目的のために、
次のようにケース class を定義できます:

```scala
case class Sensor(device: String)
```

その後、作成されたオブジェクトをデバイスのタイプ (`countByValue()`) でカウントし、それらに対して `window()`
などの操作を実行できます。

処理後、結果はコンソールに出力されます:

```scala
processedDataStream.print()
```

<a name="feedback-loop---persisting-context-data"/>

## フィードバック・ループ - コンテキスト・データの永続化

2番目の例では、土壌湿度が低すぎる場合に水栓をオンにし、土壌湿度が通常のレベルに戻ったときに水栓をオフに戻します。
このようにして、土壌の湿度は常に適切なレベルに保たれます。


データフロー・ストリームは、ノーティフィケーションを受信するために `NGSILDReceiver` operator を使用し、入力を
フィルタリングして **soil humidity sensor** にのみレスポンスするように入力をフィルタ処理し、次に `OrionSink`
を使用して処理されたコンテキストを Context Broker にプッシュバックします。この例のソースコードは
[org/fiware/cosmos/tutorial/Feedback.scala](https://github.com/ging/fiware-cosmos-orion-spark-connector-tutorial/blob/master/cosmos-examples/src/main/scala/org/fiware/cosmos/tutorial/FeedbackLD.scala)
にあります。

<a name="feedback-loop---installing-the-jar"/>

### フィードバック・ループ - JAR のインストール

```console
/spark/bin/spark-submit  \
--class  org.fiware.cosmos.tutorial.FeedbackLD \
--master  spark://spark-master:7077 \
--deploy-mode client /home/cosmos-examples/target/cosmos-examples-1.2.2.jar \
--conf "spark.driver.extraJavaOptions=-Dlog4jspark.root.logger=WARN,console"
```

<a name="feedback-loop---subscribing-to-context-changes"/>

### フィードバック・ループ - コンテキスト変更のサブスクライブ

この例を実行するには、新しいサブスクリプションを設定する必要があります。 サブスクリプションは、
土壌湿度センサのコンテキストの変化をリッスンしています。

#### 3️⃣ Request:

```console
curl -L -X POST 'http://localhost:1026/ngsi-ld/v1/subscriptions/' \
-H 'Content-Type: application/ld+json' \
-H 'NGSILD-Tenant: openiot' \
--data-raw '{
  "description": "Notify Spark of changes of Soil Humidity",
  "type": "Subscription",
  "entities": [{"type": "SoilSensor"}],
  "watchedAttributes": ["humidity"],
  "notification": {
    "attributes": ["humidity"],
    "format": "normalized",
    "endpoint": {
      "uri": "http://spark-worker-1:9001",
      "accept": "application/json"
    }
  },
   "@context": "http://context/ngsi-context.jsonld"
}'
```

サブスクリプションが作成されている場合は、`/ngsi-ld/v1/subscriptions/` エンドポイントに GET
リクエストを送信することで、サブスクリプションが起動しているかどうかを確認できます。

#### 4️⃣ Request:

```console
curl -X GET \
'http://localhost:1026/ngsi-ld/v1/subscriptions/' \
-H 'NGSILD-Tenant: openiot'
```

<a name="feedback-loop---checking-the-output"/>

### フィードバック・ループ - 出力の確認

`http://localhost:3000/device/monitor` に移動します。

Farm001 の温度を上げ、湿度値が35を下回るまで待ちます。そうすると、水栓が自動的にオンになり、
土壌の湿度が上がります。 湿度が50を超えると、水栓も自動的にオフになります。

<a name="feedback-loop---analyzing-the-code"/>

### フィードバック・ループ - コードの分析

```scala
package org.fiware.cosmos.tutorial

import org.apache.spark._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.fiware.cosmos.orion.spark.connector._

object FeedbackLD {
  final val CONTENT_TYPE = ContentType.JSON
  final val METHOD = HTTPMethod.PATCH
  final val CONTENT = "{\n  \"type\" : \"Property\",\n  \"value\" : \" \" \n}"
  final val HEADERS = Map(
    "NGSILD-Tenant" -> "openiot",
    "Link" -> "<http://context/ngsi-context.jsonld>; rel=\"http://www.w3.org/ns/json-ld#context\"; type=\"application/ld+json\""
  )
  final val LOW_THRESHOLD = 35
  final val HIGH_THRESHOLD = 50
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("Feedback")
    val ssc = new StreamingContext(conf, Seconds(10))

    // Create Orion Receiver. Receive notifications on port 9001
    val eventStream = ssc.receiverStream(new NGSILDReceiver(9001))

    // Process event stream
    val processedDataStream = eventStream.flatMap(event => event.entities)
      .filter(ent => ent.`type` == "SoilSensor")

    /* High humidity */
    val highHumidity = processedDataStream
      .filter(ent =>  (ent.attrs("humidity") != null) && (ent.attrs("humidity")("value").asInstanceOf[BigInt] > HIGH_THRESHOLD))
      .map(ent => (ent.id,ent.attrs("humidity")("value")))

    val highSinkStream= highHumidity.map(sensor => {
      OrionSinkObject(CONTENT,"http://orion:1026/ngsi-ld/v1/entities/urn:ngsi-ld:Device:water"+sensor._1.takeRight(3)+"/attrs/off",CONTENT_TYPE,METHOD,HEADERS)
    })

    highHumidity.map(sensor => "Sensor" + sensor._1 + " has detected a humidity level above " + HIGH_THRESHOLD + ". Turning off water faucet!").print()
    OrionSink.addSink( highSinkStream )


    /* Low humidity */
    val lowHumidity = processedDataStream
      .filter(ent => (ent.attrs("humidity") != null) && (ent.attrs("humidity")("value").asInstanceOf[BigInt] < LOW_THRESHOLD))
      .map(ent => (ent.id,ent.attrs("humidity")("value")))

    val lowSinkStream= lowHumidity.map(sensor => {
      OrionSinkObject(CONTENT,"http://orion:1026/ngsi-ld/v1/entities/urn:ngsi-ld:Device:water"+sensor._1.takeRight(3)+"/attrs/on",CONTENT_TYPE,METHOD,HEADERS)
    })

    lowHumidity.map(sensor => "Sensor" + sensor._1 + " has detected a humidity level below " + LOW_THRESHOLD + ". Turning on water faucet!").print()
    OrionSink.addSink( lowSinkStream )

    ssc.start()
    ssc.awaitTermination()
  }
}
```

ご覧のとおり、前の例と似ています。主な違いは、処理されたデータを **`OrionSink`** を介して Context Broker
に書き戻すことです。

**`OrionSinkObject`** の引数は次のとおりです:

-   **Message**: `"{\n \"on\": {\n \"type\" : \"command\",\n \"value\" : \"\"\n }\n}"`. 'on' コマンドを送信します
-   **URL**: `"http://orion:1026/ngsi-ld/v1/entities/urn:ngsi-ld:Device:water"+sensor._1.takeRight(3)+"/attrs/on"` or `"http://orion:1026/ngsi-ld/v1/entities/urn:ngsi-ld:Device:water"+sensor._1.takeRight(3)+"/attrs/off"`, depending on whether we are turning on or off the water faucet. TakeRight(3) gets the number of
    the sensor, for example '001'.
-   **Content Type**: `ContentType.JSON`.
-   **HTTP Method**: `HTTPMethod.PATCH`.
-   **Headers**: `Map("NGSILD-Tenant" -> "openiot", "Link" -> "<http://context/ngsi-context.jsonld>; rel=\"http://www.w3.org/ns/json-ld#context\"; type=\"application/ld+json\"" )`.
    オプション・パラメータ。HTTP リクエストに必要なヘッダを追加します。

# 次のステップ

データ処理エンジンとして Flink を使用したい場合は、
[この Flink 用チュートリアル](https://github.com/FIWARE/tutorials.Big-Data-Flink) も利用できます。

このチュートリアルでデータに対して実行される操作は非常に単純でした。機械学習を使用してリアルタイム予測を実行する
シナリオを設定する方法を知りたい場合は、ベルリンで開催された FIWARE Global Summit (2019) で発表された
[デモ](https://github.com/ging/fiware-global-summit-berlin-2019-ml) をご覧ください。

高度な機能を追加することで、アプリケーションに複雑さを加える方法を知りたいですか？ このシリーズの
[他のチュートリアル](https://www.letsfiware.jp/ngsi-ld-tutorials)を読むことで見つけることができます

---

## License

[MIT](LICENSE) © 2021-2024 FIWARE Foundation e.V.
