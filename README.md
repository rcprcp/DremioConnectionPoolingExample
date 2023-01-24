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

If you set the connections to be less than the nu,ber of threads, some threads will wait for db connections from the Hikari Pool.  
For example, if you set `-c 2 -t 7` you might see output like this: 
```shell
2023-01-24 16:57:32.087 [INFO ] [Thread-0] DremioConnectionPoolingExample - Thread-0 -  30000 records 14 records per ms.  pickup_datetime 2013-05-31 18:32:00.0
2023-01-24 16:57:33.189 [INFO ] [Thread-4] DremioConnectionPoolingExample - Thread-4 -  30000 records 10 records per ms.  pickup_datetime 2014-02-26 23:43:00.0
2023-01-24 16:57:34.165 [INFO ] [Thread-0] DremioConnectionPoolingExample - Thread-0 -  60000 records 14 records per ms.  pickup_datetime 2014-02-26 23:13:00.0
2023-01-24 16:57:36.566 [INFO ] [Thread-4] DremioConnectionPoolingExample - Thread-4 -  60000 records 9 records per ms.  pickup_datetime 2014-02-27 04:00:00.0
2023-01-24 16:57:36.749 [INFO ] [Thread-0] DremioConnectionPoolingExample - Thread-0 -  90000 records 13 records per ms.  pickup_datetime 2014-02-26 20:08:00.0
```
In which thread 0 and 4 have the 2 connections, the other 5 threads are waiting. 

## Todo list - 
- [ ] test with the Arrow-Flight driver, instead of the legacy driver. 
- [ ] better record per second statistics to see how scalable this is. 
- [ ] add a logger; remove the System.out.println's
- 