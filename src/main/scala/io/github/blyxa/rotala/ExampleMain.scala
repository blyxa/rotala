package io.github.blyxa.rotala

import akka.grpc.scaladsl.Metadata
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{RequestContext, Route}
import io.github.blyxa.rotala.module.WebModule
import io.github.blyxa.rotala.proto.{ExampleServicePowerApi, ExampleServicePowerApiHandler, HelloRequest, HelloResponse}
import io.github.blyxa.rotala.route.HtmlRoute
import io.github.blyxa.rotala.util.WebBootstrap
import org.slf4j.LoggerFactory

import scala.concurrent.Future
object ExampleMain {
  private val logger = LoggerFactory.getLogger(getClass)
  def main(args: Array[String]): Unit = {
    class GrpcService extends ExampleServicePowerApi{
      override def sayHello(in: HelloRequest, metadata: Metadata): Future[HelloResponse] =
        Future.successful(HelloResponse(response = s"hello ${in.name}"))
    }
    val webModule = new WebModule {
      override implicit def routes:Set[Route] =  new HtmlRoute().routes
      override lazy val webBootstrapper: WebBootstrap = new WebBootstrap{
        override def grpcRoute: Option[Route] = Some(
          extractRequestContext { _: RequestContext =>
            post{
              handle(ExampleServicePowerApiHandler(new GrpcService))
            }
          }
        )
        override def httpRoute: Route = {
          concat(
            path("blocking"){
              get {
                complete {
                  Future {
                    Thread.sleep(5000)
                    System.currentTimeMillis().toString
                  }
                }
              }
            },
            path("hello"){
              get(complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, messageProvider.message("hello",Map("name"->"foobar")))))
            },
            path("fast"){
              get(complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "fast")))
            },
            path("html"){
              get(
                complete{
                  htmlTemplateEngine.render("index.peb",Map(
                    "version"->mp.getProperty("version","none"),
                    "env"->mp.getProperty("env","none")
                  ))
                }
              )
            }
          )
        }
      }
    }
    webModule.lifeCycle.registerForShutdown(9999,"lastHook!!",()=>{logger.info("bye bye")})
    webModule.lifeCycle.start(()=>webModule.webBootstrapper.start())
  }
}
