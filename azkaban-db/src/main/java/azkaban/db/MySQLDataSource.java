/*
 * Copyright 2017 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package azkaban.db;

import azkaban.utils.Props;

import java.sql.*;
import java.util.Base64;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Singleton
public class MySQLDataSource extends AzkabanDataSource {

  private static final Logger logger = LoggerFactory.getLogger(MySQLDataSource.class);
  private final DBMetrics dbMetrics;

  @Inject
  public MySQLDataSource(final Props props, final DBMetrics dbMetrics) {
    super();
    this.dbMetrics = dbMetrics;

    final int port = props.getInt("mysql.port");
    final String host = props.getString("mysql.host");
    final String dbName = props.getString("mysql.database");
    final String user = props.getString("mysql.user");
	  // FIXME The database password needs base64 decoding.
    String pwd = null;
    try {
      pwd = new String(Base64.getDecoder().decode(props.getString("mysql.password").getBytes()),"UTF-8");
    } catch (Exception e){
      logger.error("password decore failed", e);
    }
    final String password = pwd;
    final int numConnections = props.getInt("mysql.numconnections");

    final String url = "jdbc:mysql://" + (host + ":" + port + "/" + dbName);
    addConnectionProperty("useUnicode", "yes");
    addConnectionProperty("characterEncoding", "UTF-8");
    addConnectionProperty("autoReconnect", "true");
    setDriverClassName("com.mysql.jdbc.Driver");
    setUsername(user);
    setPassword(password);
    setUrl(url);
    setMaxTotal(numConnections);
    setValidationQuery("/* ping */ select 1");
    setTestOnBorrow(true);
    setTestWhileIdle(true);
    setTimeBetweenEvictionRunsMillis(1000*60*1);
    setMinEvictableIdleTimeMillis(1000*60*5);
    setMaxConnLifetimeMillis(1000*60*10);
    this.initSettingsFromProps(props);
  }

  private void initSettingsFromProps(Props props){

    if(props.containsKey("mysql.initialSize")){
      setInitialSize(props.getInt("mysql.initialSize"));
    }

    if(props.containsKey("mysql.maxTotal")){
      setMaxTotal(props.getInt("mysql.maxTotal"));
    }

    if(props.containsKey("mysql.c")){
      setMaxIdle(props.getInt("mysql.maxIdle"));
    }

    if(props.containsKey("mysql.minIdle")){
      setMinIdle(props.getInt("mysql.minIdle"));
    }

    if(props.containsKey("mysql.maxWaitMillis")){
      setMaxWaitMillis(props.getLong("mysql.maxWaitMillis"));
    }

    if(props.containsKey("mysql.validationQuery")){
      setValidationQuery(props.getString("mysql.validationQuery"));
    }

    if(props.containsKey("mysql.validationQueryTimeout")){
      setValidationQueryTimeout(props.getInt("mysql.validationQueryTimeout"));
    }

    if(props.containsKey("mysql.testOnCreate")){
      setTestOnCreate(props.getBoolean("mysql.testOnCreate"));
    }

    if(props.containsKey("mysql.testOnBorrow")){
      setTestOnBorrow(props.getBoolean("mysql.testOnBorrow"));
    }

    if(props.containsKey("mysql.testOnReturn")){
      setTestOnReturn(props.getBoolean("mysql.testOnReturn"));
    }

    if(props.containsKey("mysql.testWhileIdle")){
      setTestWhileIdle(props.getBoolean("mysql.testWhileIdle"));
    }

    if(props.containsKey("mysql.timeBetweenEvictionRunsMillis")){
      setTimeBetweenEvictionRunsMillis(props.getLong("mysql.timeBetweenEvictionRunsMillis"));
    }

    if(props.containsKey("mysql.numTestsPerEvictionRun")){
      setNumTestsPerEvictionRun(props.getInt("mysql.numTestsPerEvictionRun"));
    }

    if(props.containsKey("mysql.minEvictableIdleTimeMillis")){
      setMinEvictableIdleTimeMillis(props.getLong("mysql.minEvictableIdleTimeMillis"));
    }

    if(props.containsKey("mysql.softMinEvictableIdleTimeMillis")){
      setSoftMinEvictableIdleTimeMillis(props.getLong("mysql.softMinEvictableIdleTimeMillis"));
    }

    if(props.containsKey("mysql.maxConnLifetimeMillis")){
      setMaxConnLifetimeMillis(props.getLong("mysql.maxConnLifetimeMillis"));
    }

  }
  /**
   * This method overrides {@link BasicDataSource#getConnection()}, in order to have retry logics.
   * We don't make the call synchronized in order to guarantee normal cases performance.
   */
  @Override
  public Connection getConnection() throws SQLException {

    this.dbMetrics.markDBConnection();
    final long startMs = System.currentTimeMillis();
    Connection connection = null;
    int retryAttempt = 1;
    while (retryAttempt < AzDBUtil.MAX_DB_RETRY_COUNT) {
      try {
        /**
         * when DB connection could not be fetched (e.g., network issue), or connection can not be validated,
         * {@link BasicDataSource} throws a SQL Exception. {@link BasicDataSource#dataSource} will be reset to null.
         * createDataSource() will create a new dataSource.
         * Every Attempt generates a thread-hanging-time, about 75 seconds, which is hard coded, and can not be changed.
         */
        connection = createDataSource().getConnection();

        /**
         * If connection is null or connection is read only, retry to find available connection.
         * When DB fails over from master to slave, master is set to read-only mode. We must keep
         * finding correct data source and sql connection.
         */
        if (connection == null || isReadOnly(connection)) {
          throw new SQLException("Failed to find DB connection Or connection is read only. ");
        } else {

          // Evalaute how long it takes to get DB Connection.
          this.dbMetrics.setDBConnectionTime(System.currentTimeMillis() - startMs);
          return connection;
        }
      } catch (final SQLException ex) {

        /**
         * invalidate connection and reconstruct it later. if remote IP address is not reachable,
         * it will get hang for a while and throw exception.
         */
        this.dbMetrics.markDBFailConnection();
        try {
          invalidateConnection(connection);
        } catch (final Exception e) {
          logger.error( "can not invalidate connection.", e);
        }
        logger.error( "Failed to find write-enabled DB connection. Wait 15 seconds and retry."
            + " No.Attempt = " + retryAttempt, ex);
        /**
         * When database is completed down, DB connection fails to be fetched immediately. So we need
         * to sleep 15 seconds for retry.
         */
        sleep(1000L * 15);
        retryAttempt++;
      }
    }
    return connection;
  }

  private boolean isReadOnly(final Connection conn) throws SQLException {
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = conn.prepareStatement("SELECT @@global.read_only");
      rs = ps.executeQuery();
      if (rs.next()) {
        final int value = rs.getInt(1);
        return value != 0;
      }
    } catch (SQLException e) {
      throw new SQLException("can not fetch read only value from DB");
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
      } catch (SQLException e) {
        logger.error("SQLException in execute query, caused by:", e);
      }

      try {
        if (ps != null) {
          ps.close();
        }
      } catch (SQLException e) {
        logger.error("SQLException in execute query, caused by:", e);
      }
    }
    return false;
  }

  private void sleep(final long milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (final InterruptedException e) {
      logger.error("Sleep interrupted", e);
    }
  }

  @Override
  public String getDBType() {
    return "mysql";
  }

  @Override
  public boolean allowsOnDuplicateKey() {
    return true;
  }
}
