package com.flipkart.connekt.commons.tests.connections

import java.util.Properties
import javax.sql.DataSource

import com.couchbase.client.java.Cluster
import com.flipkart.connekt.commons.connections.TConnectionProvider
import com.flipkart.connekt.commons.tests.connections.couchbase.CouchbaseMockCluster
import org.apache.commons.dbcp2.BasicDataSourceFactory
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.{HConnectionManager, HConnection}

/**
  * Created by kinshuk.bairagi on 27/01/16.
  */
class MockConnectionProvider extends TConnectionProvider{

  override def createCouchBaseConnection(nodes: List[String]): Cluster = new CouchbaseMockCluster

  override def createHbaseConnection(hConnConfig: Configuration): HConnection = HConnectionManager.createConnection(hConnConfig)

  override def createDatasourceConnection(mySQLProperties: Properties): DataSource = BasicDataSourceFactory.createDataSource(mySQLProperties)

}