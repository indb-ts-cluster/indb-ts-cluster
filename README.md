# In-Database Time Series Clustering

- The code of all experiments can be found in the folder ***src***. 

- All opensource data can be found in the folder ***opensource-data***. 

- A well complied database IoTDB is given in the folder ***apache-iotdb-0.13.3-all-bin***, which is built from the [source code](https://github.com/apache/iotdb/tree/research/indb-ts-cluster).
- The main code of in-database K-shape and in-database Mediod Shape can be found in [KShapeExecutor](https://github.com/apache/iotdb/blob/research/outlier/server/src/main/java/org/apache/iotdb/db/query/executor/KShapeExecutor.java) and [KShapeMExecutor](https://github.com/apache/iotdb/blob/research/outlier/server/src/main/java/org/apache/iotdb/db/query/executor/KShapeMExecutor.java).

## Steps to Run

1. Compile the time series database [IoTDB](https://github.com/apache/iotdb/tree/research/indb-ts-cluster) by the following command. There is also a well compiled database in the folder ***apache-iotdb-0.13.3-all-bin***.

```
mvn clean package -DskipTests -Dcheckstyle.skip=True
```

2. Start a server and a client. Detailed steps can be found in the [online documentation](https://iotdb.apache.org/UserGuide/V0.13.x/QuickStart/QuickStart.html#use-cli).

3. Import the dataset UCR-Air (*./opensource-data/air.csv*) by calling IoTDB importing csv tool (*./apache-iotdb-0.13.3-all-bin/tools/import-csv.sh*) in terminal with the following command. 

```c
bash import-csv.sh -h localhost -p 6667 -u root -pw root -f ./opensource-data/air.csv
```

4. Execute in-database time series clustering with the following SQL statements in the client.

```sql
SELECT lsmKShape(s0) from root.air.d0;
SELECT lsmMShape(s0) from root.air.d0;
```

Note: The subsequence length and cluster number are set as database configuration, which can be tuned in the end of the configuration file (*./apache-iotdb-0.13.3-all-bin/conf/iotdb-engine.properties*).

```
# The number of the clusters in a page
# Datatype: int
cluster_num = 3

# The length of a subsequence
# Datatype: int
seq_length = 166

# The maximum number of data points in a page, default 1024*1024
# Datatype: int
max_number_of_points_in_page = 10240
```
