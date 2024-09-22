# Dremio Connection Pooling Example


This very simple program demonstrates connection pooling with the [Hikari Pool](https://github.com/brettwooldridge/HikariCP) connection pool library.

This program requires two environment variables, DREMIO_USER, and DREMIO_PASSWORD.  If you cannot access Dremio's internal test machines, you'll need to modify the code and update the JDBC_URL as well.

Currently this program is using the Apache JDBC SQL Arrow Flight JDBC Driver.  

By default, the program creates 5 connections, currently this can be changed with --connections or -c

The program is SELECTing from the same table in all the threads, therefore, it's not realistic, but offers the opportunity to see parallel behavior via multiple threads.  

## Building and run the program
download: 
``` shell
git clone https://github.com/rcprcp/DremioConnectionPoolingExample.git
cd DremioConnectionPoolingExample
mvn clean package 
```
Run the program:
```shell
export _JAVA_OPTIONS="--illegal-access=warn --add-opens=java.base/java.nio=ALL-UNNAMED"
java  -jar target/DremioConnectionPoolingExample-1.0-SNAPSHOT-jar-with-dependencies.jar -c 5 
```
--connections or -c to set the number of JDBC connections.

--threads or -t to set the number of worker threads.

If you set the connections to be less than the number of threads, some threads will wait for db connections from the Hikari Pool.  

With the default configuration a thread will only wait for 30 seconds, if a connection is not available, the thread will get an SQLException: 
```shell
2023-01-24 17:09:10.188 [ERROR] [Thread-1] DremioConnectionPoolingExample - SQLException: HikariPool-1 - Connection is not available, request timed out after 30014ms.
java.sql.SQLTransientConnectionException: HikariPool-1 - Connection is not available, request timed out after 30014ms.
	at com.zaxxer.hikari.pool.HikariPool.createTimeoutException(HikariPool.java:696) ~[DremioConnectionPoolingExample-1.0-SNAPSHOT-jar-with-dependencies.jar:?]
	at com.zaxxer.hikari.pool.HikariPool.getConnection(HikariPool.java:197) ~[DremioConnectionPoolingExample-1.0-SNAPSHOT-jar-with-dependencies.jar:?]
	at com.zaxxer.hikari.pool.HikariPool.getConnection(HikariPool.java:162) ~[DremioConnectionPoolingExample-1.0-SNAPSHOT-jar-with-dependencies.jar:?]
	at com.zaxxer.hikari.HikariDataSource.getConnection(HikariDataSource.java:100) ~[DremioConnectionPoolingExample-1.0-SNAPSHOT-jar-with-dependencies.jar:?]
	at com.dremio.connectionpoolingexample.DremioConnectionPoolingExample$WorkerThread.run(DremioConnectionPoolingExample.java:110) ~[DremioConnectionPoolingExample-1.0-SNAPSHOT-jar-with-dependencies.jar:?]
```