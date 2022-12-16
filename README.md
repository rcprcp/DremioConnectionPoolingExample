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
Run the program
```shell
java -jar target/DremioConnectionPoolingExample-1.0-SNAPSHOT-jar-with-dependencies.jar -c 5 
```
--connections or -c to set the number of JDBC connections. 

## Todo list - 
- [ ] test with the Arrow-Flight driver, instead of the legacy driver. 
- [ ] better record per second statistics to see how scalable this is. 
- [ ] add a logger; remove the System.out.println's
- 