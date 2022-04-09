package com.blyxa.rotala.util

import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

/**
 * LifeCycle controls the start and stop lifecycle of an application.
 * You can register callback functions into this class to perform
 * clean shutdown.
 */
class LifeCycle{
  private val logger = LoggerFactory.getLogger(getClass)

  private val shutdownFuncs = ListBuffer[(Int, String, ()=>Unit)]()
  def registerForShutdown(order:Int,name:String,func:()=>Unit):Unit = {
    shutdownFuncs.addOne((order, name, func))
  }

  def shutdown(): Unit ={
    val start = System.currentTimeMillis()
    logger.info(s"shutting down [${shutdownFuncs.size}] items")
    shutdownFuncs.sortBy(_._1).foreach{case (order, name,func)=>
      logger.info(s"shutting down order[$order] name[$name] [$func]")
      func()
    }
    logger.info(s"shutdown took [${System.currentTimeMillis()-start}]ms. Good bye!")
  }

  def start(init:()=>Unit): Unit ={
    val startMs = System.currentTimeMillis()
    Try {
      // hook into shutdown event
      Runtime.getRuntime.addShutdownHook(new Thread() {
        override def run(): Unit = shutdown()
      })
      // start the webserver
      init()
      logger.info(s"booted in [${System.currentTimeMillis() - startMs}]ms")
    } match {
      case Failure(e) =>
        logger.error(s"Failed to start err[${e.getMessage}]", e)
        shutdown()
      case Success(_) =>
    }
  }


}