package io.github.blyxa.rotala.module

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.zaxxer.hikari.HikariDataSource
import io.github.blyxa.rotala.util.{LifeCycle, MainProperties, MessageProvider, Version}
import kong.unirest.jackson.JacksonObjectMapper
import kong.unirest.{Unirest, UnirestInstance}
import org.slf4j.LoggerFactory

import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

/**
 * This class initializes common low level components
 * that can be used by other code.
 *
 * Common use scenario is a web application which would
 * need things like app configuration, database connection,
 * json parser, etc.
 *
 */
class CommonModule{
  private val logger = LoggerFactory.getLogger(getClass)
  implicit val lifeCycle: LifeCycle = new LifeCycle

  implicit val mp: MainProperties = {
    val path = Option(System.getProperty("mainProperties")).getOrElse("config/main.properties")
    val mp = new MainProperties(path,true)
    mp.offer("env","dev")
    logger.info(
      s"""
         |==============================================================================
         |= MainProperties
         |= load order is
         |=   - classpath:config/main.properties
         |=   - filepath:config/main.properties
         |=   - system property: -DmainProperties=/somewhere/in/the/file/system
         |= This mainProperties was loaded with [$path]
         |===============================================================================
         |""".stripMargin)
    logger.debug(
      s"""
         |MainProperties dump
         |--------------------------------------------
         |${mp.prettyString}
         |
         |""".stripMargin)
    mp
  }
  implicit val version:Version = {
    val v = Version.fetch()
    logger.info(s"version[$v]")
    v
  }
  implicit val messageProvider:MessageProvider = new MessageProvider
  implicit val om: ObjectMapper = {
    logger.info(s"init ObjectMapper")
    val om = new ObjectMapper()
    om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    om.registerModule(DefaultScalaModule)
    om
  }
  implicit val ds: Option[HikariDataSource] = {
    if(mp.containsKey("db.main.url")){
      logger.info(s"init dataSource[${mp.getRequiredProperty("db.main.url")}]")
      val ds = new HikariDataSource()
      ds.setJdbcUrl(mp.getRequiredProperty("db.main.url"))
      ds.setUsername(mp.getRequiredProperty("db.main.user"))
      ds.setPassword(mp.getRequiredProperty("db.main.password"))
      ds.setConnectionInitSql("SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci")
      lifeCycle.registerForShutdown(99,"hikari", ()=>{ds.close()})
      Some(ds)
    }else{
      logger.info(s"No db.main.url property set, no db initialized.")
      None
    }
  }

  implicit val ec: ExecutionContextExecutorService = {
    val name = "ExecutionContext[general]"
    val wc = mp.getProperty("executor.general.worker.count","2").toInt
    val es = Executors.newFixedThreadPool(wc)
    logger.info(s"created execution context[$name] with [$wc] workers")
    val ec = ExecutionContext.fromExecutorService(es)
    lifeCycle.registerForShutdown(80,name, ()=>{
      ec.shutdown()
      ec.awaitTermination(10,TimeUnit.SECONDS)
    })
    ec
  }

  implicit val uniRest: UnirestInstance = {
    val ur = Unirest.primaryInstance()
    ur.config().setObjectMapper(new JacksonObjectMapper(om))
    ur
  }
}