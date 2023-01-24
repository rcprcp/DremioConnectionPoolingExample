# Dremio Connection Pooling Example


This very simple program demonstrates connection pooling with the [Hikari Pool](https://github.com/brettwooldridge/HikariCP) connection pool library.

This program requires two environment variables, DREMIO_USER, and DREMIO_PASSWORD.  If you cannot access Dremio's internal test machines, you'll need to modify the code and update the JDBC URL as well.

Currently this program is using the "legacy" Dremio JDBC Driver.  

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
java --illegal-access=warn -jar target/DremioConnectionPoolingExample-1.0-SNAPSHOT-jar-with-dependencies.jar -c 5 
```
--connections or -c to set the number of JDBC connections.

--threads or -t to set the number of worker threads.

If you set the connections to be less than the number of threads, some threads will wait for db connections from the Hikari Pool.  
For example, if you set `-c 2 -t 7` you might see output like this: 
```shell
2023-01-24 17:07:40.463 [INFO ] [Thread-3] DremioConnectionPoolingExample - 30000 records 40 records per ms.  pickup_datetime 2014-02-26 23:43:00.0
2023-01-24 17:07:41.891 [INFO ] [Thread-0] DremioConnectionPoolingExample - 30000 records 13 records per ms.  pickup_datetime 2014-08-07 18:47:00.0
2023-01-24 17:07:43.122 [INFO ] [Thread-3] DremioConnectionPoolingExample - 60000 records 17 records per ms.  pickup_datetime 2013-05-31 08:55:00.0
2023-01-24 17:07:44.907 [INFO ] [Thread-0] DremioConnectionPoolingExample - 60000 records 11 records per ms.  pickup_datetime 2014-08-07 20:47:00.0
2023-01-24 17:07:45.660 [INFO ] [Thread-3] DremioConnectionPoolingExample - 90000 records 15 records per ms.  pickup_datetime 2014-02-27 01:24:00.0
2023-01-24 17:07:48.062 [INFO ] [Thread-0] DremioConnectionPoolingExample - 90000 records 10 records per ms.  pickup_datetime 2013-05-27 20:56:00.0
2023-01-24 17:07:48.064 [INFO ] [Thread-3] DremioConnectionPoolingExample - 120000 records 14 records per ms.  pickup_datetime 2014-02-27 07:29:00.0
```
In which thread 0 and 3 are actively using the 2 connections, the other 5 threads are waiting. 

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

## Todo list - 
- [x] test with the Arrow-Flight driver, in addition to the legacy driver. 
- [ ] better record per second statistics to see how scalable this is. 
- [x] add a logger; remove the System.out.println's
- [ ] check into whether using Java 11 will remove the requirement for  `--illegal-access=warn`
- 