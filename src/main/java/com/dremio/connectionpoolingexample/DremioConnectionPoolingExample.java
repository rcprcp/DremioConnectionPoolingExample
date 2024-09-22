/*
 * Copyright (C) 2022 Dremio Corporation
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DremioConnectionPoolingExample {
  private static final Logger LOG = LogManager.getLogger(DremioConnectionPoolingExample.class);

  @Parameter(names = {"--connections", "-c"})
  int connections = 1;

  @Parameter(names = {"--host", "-h"})
  String host = "";

  @Parameter(names = {"--port", "-p"})
  int port = 0;

  @Parameter(names = {"--threads", "-t"})
  int threads = 1;

  // ArrayBlockingQueue<Record> theRecords = new ArrayBlockingQueue<>(100);
  public static void main(String... args) {

    DremioConnectionPoolingExample dcpe = new DremioConnectionPoolingExample();

    // parse start args...
    JCommander.newBuilder().addObject(dcpe).build().parse(args);

    dcpe.run();
  }

  void run() {

    final String arrowFlightURL =
        String.format("jdbc:arrow-flight-sql://%s:%d/?useEncryption=false", host, port);
    LOG.info(arrowFlightURL);

    // just to verify - print the available JDBC Drivers.
    LOG.info("Registered JDBC drivers:");
    try {
      Collections.list(DriverManager.getDrivers())
          .forEach(
              driver ->
                  LOG.info(
                      "Driver class {} (version {}.{})",
                      driver.getClass().getName(),
                      driver.getMajorVersion(),
                      driver.getMinorVersion()));
    } catch (Exception ex) {
      LOG.error("Exception: {}", ex.getMessage(), ex);
      System.exit(4);
    }

    HikariConfig config = new HikariConfig();

    config.setJdbcUrl(arrowFlightURL);

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
      this.hikariPool = hikariPool;
    }

    // invoked on thread.start()
    public void run() {

      try {
        Connection conn = hikariPool.getConnection();
        Statement stmt = conn.createStatement();
        String dataSource = "Samples.\"samples.dremio.com\".\"NYC-taxi-trips\"";
        final String sql = String.format("SELECT * FROM %s LIMIT 10", dataSource);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 10_000_000; i++) {
          try (ResultSet rs = stmt.executeQuery(sql)) {

            //            String cat = conn.getCatalog();
            //            DatabaseMetaData dbmd = conn.getMetaData();
            while (rs.next()) {
              rs.getString(1);
            }
            long end = System.currentTimeMillis();
            if (i % 1000 == 0) {
              System.out.println(
                  String.format(
                      "%s iteration: %d elapsed: %d",
                      Thread.currentThread().getName(), i, end - start));
            }
          }
        }

        stmt.close();
        conn.close();

      } catch (SQLException ex) {
        LOG.error("SQLException: {}", ex.getMessage(), ex);
        System.exit(5);
      }
    }
  }
}
