package io.github.blyxa.rotala.util

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

import java.util.concurrent.TimeUnit
import scala.concurrent.{Await, duration}
import scala.concurrent.duration.{TimeUnit, _}

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

  implicit val akkaActorSystem: ActorSystem[Nothing] = {

    logger.info(s"akkaActorSystem:init")
    ActorSystem[Nothing](
      Behaviors.empty,
      "web-server",
      ConfigFactory.load("rotala-akka.conf")
    )
  }

  lifeCycle.registerForShutdown(9999, "akkaActorSystem",()=>{
    akkaActorSystem.terminate()
  })

  def start(): (ServerBinding,Option[ServerBinding]) ={
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
