package com.blyxa.rotala.util

import akka.actor.ClassicActorSystemProvider
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, concat, extractRequestContext, getFromResourceDirectory, handleExceptions, handleRejections, pathPrefix}
import akka.http.scaladsl.server.{ExceptionHandler, MethodRejection, RejectionHandler, RequestContext, Route}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * This is where the akka http web server is configured
 *
 */
class WebBootstrap
(implicit
  lifeCycle: LifeCycle,
  mp:MainProperties,
  routes: Set[Route]
){
  private val logger = LoggerFactory.getLogger(getClass)

  def httpRoute:Route = {
    val defaultExceptionHandler: ExceptionHandler =
      ExceptionHandler {
        case e: Throwable  =>
          logger.error(s"default exception handler got error[$e]",e)
          complete {HttpResponse(StatusCodes.InternalServerError,entity="error")}
      }
    val methodRejectionHandler:RejectionHandler =RejectionHandler.newBuilder()
      .handleAll[MethodRejection] { methodRejections =>
        // send error if http request doesn't match. ie POST != GET
        complete(StatusCodes.MethodNotAllowed, s"method not allowed")
      }.result()
    extractRequestContext{ ctx: RequestContext =>
      concat(
        // HTTP routes
        handleExceptions(defaultExceptionHandler){
          handleRejections(methodRejectionHandler){
            concat(
              List(
                pathPrefix("s"){
                  getFromResourceDirectory("static")
                }
              ) ++
                routes.toList:_*
            )
          }
        }
      )
    }
  }

  def grpcRoute:Option[Route] = None

  implicit val akkaActorSystem:ClassicActorSystemProvider = {
    val conf = ConfigFactory
      .parseString(
        s"""akka.http.server.preview.enable-http2 = on
           |# turn this off so we can use LifeCycle to shutdown in desired order
           |akka.jvm-shutdown-hooks = off
           |# for security reasons, do not expose server/version
           |akka.http.server.server-header = http/private
           |""".stripMargin)
      .withFallback(ConfigFactory.defaultApplication())
    ActorSystem[Nothing](Behaviors.empty, "web-server", conf)
  }

  def start(): (ServerBinding,Option[ServerBinding]) ={
    lifeCycle.registerForShutdown(999, "akkaActorSystem",()=>{
      akkaActorSystem.classicSystem.terminate()
    })
    val httpPort =  mp.getRequiredProperty("http.port").toInt
    val bindAddress = if(mp.getRequiredProperty("env").contentEquals("prd"))
      "0.0.0.0" else "localhost"
    val grpcBinding = grpcRoute.map{route=>
      val grpcPort = mp.getRequiredProperty("grpc.port").toInt
      logger.info(s"grpc.port[$grpcPort]")
      Await.result(Http().newServerAt(bindAddress, grpcPort).bind(route), 10.seconds)
    }
    val http = Http().newServerAt(bindAddress, httpPort).bind(httpRoute)
    val httpBinding = Await.result(http, 10.seconds)
    logger.info(s"http.port[$httpPort]")

    (httpBinding,grpcBinding)
  }
}
