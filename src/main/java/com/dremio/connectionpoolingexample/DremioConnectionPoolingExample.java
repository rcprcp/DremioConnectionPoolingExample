/*
 * Copyright (C) 2017-2022 Dremio Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dremio.connectionpoolingexample;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.Collections;

public class DremioConnectionPoolingExample {
  @Parameter(names = {"--connections", "-c"})
  int connections = 5;

  @Parameter(names = {"--threads", "-t"})
  int threads = connections;
  public static void main(String... args) {

    DremioConnectionPoolingExample dcpe = new DremioConnectionPoolingExample();

    //parse start args...
    JCommander.newBuilder().addObject(dcpe).build().parse(args);

    dcpe.run();
  }

  void run() {
    System.out.println("connections: " + connections);

    final String jdbcUrl = "jdbc:dremio:direct=automaster:31010;schema=Samples.samples.dremio.com;";
    final String arrowFlightURL = "jdbc:arrow-flight-sql://automaster:32010/?useEncryption=false?schema=Samples.samples.dremio.com;";
    System.out.println("Currently Registered JDBC drivers:");

    // just to verify - print the available JDBC Drivers.
    Collections.list(DriverManager.getDrivers()).forEach(driver -> {
      System.out.println(String.format("Driver class %s (version %s.%s)", driver.getClass().getName(), driver.getMajorVersion(), driver.getMinorVersion()));
    });

    // create a connection pool.
    HikariConfig config = new HikariConfig();

    // switch JDBC connection string as needed.
    config.setJdbcUrl(arrowFlightURL);
//    config.setJdbcUrl(jdbcUrl);

    config.setUsername(System.getenv("DREMIO_USER"));
    config.setPassword(System.getenv("DREMIO_PASSWORD"));
    config.setMaximumPoolSize(connections);
    HikariDataSource ds = new HikariDataSource(config);

    // create threads that will run SQL statements.
    for (int i = 0; i < threads; i++) {
      WorkerThread w = new WorkerThread(ds);
      w.start();
    }
  }

  class WorkerThread extends Thread {
    HikariDataSource hikariPool;

    WorkerThread(HikariDataSource hikariPool) {
      System.out.println("create WorkerThread");

      this.hikariPool = hikariPool;
    }

    public void run() {
      System.out.println("start WorkerThread");

      try {
        Connection conn = hikariPool.getConnection();
        Statement stmt = conn.createStatement();
        String dataSource = "Samples.\"samples.dremio.com\".\"NYC-taxi-trips\"";
        final String sql = String.format("SELECT * FROM %s", dataSource);
        ResultSet rs = stmt.executeQuery(sql);

        int i = 0;
        long startTime = System.currentTimeMillis();
        while (rs.next()) {
          i++;
          if (i % 30000 == 0) {
            System.out.println(String.format("%s -  %d records %d records per ms.  pickup_datetime %s",
                    Thread.currentThread().getName(), i, i / (System.currentTimeMillis() - startTime), rs.getString("pickup_datetime")));
          }
        }
        System.out.println(String.format("%s read %d records in %d ms.",
                Thread.currentThread().getName(), i, System.currentTimeMillis() - startTime));

        rs.close();
        stmt.close();
        conn.close();

      } catch (SQLException ex) {
        System.out.println("SQLException: " + ex.getMessage());
        ex.printStackTrace();
      }
    }
  }
}
