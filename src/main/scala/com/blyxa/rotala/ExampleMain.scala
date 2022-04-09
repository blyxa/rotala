package com.blyxa.rotala
import akka.grpc.scaladsl.Metadata
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{complete, extractRequestContext, get, handle, path, post}
import akka.http.scaladsl.server.{RequestContext, Route}
import com.blyxa.rotala.module.WebModule
import com.blyxa.rotala.proto.{ExampleServicePowerApi, ExampleServicePowerApiHandler, HelloRequest, HelloResponse}
import com.blyxa.rotala.route.HtmlRoute
import com.blyxa.rotala.util.WebBootstrap
import akka.http.scaladsl.server.Directives._

import scala.concurrent.Future

object ExampleMain {
  def main(args: Array[String]): Unit = {

    class GrpcService extends ExampleServicePowerApi{
      override def sayHello(in: HelloRequest,
                            metadata: Metadata): Future[HelloResponse] =
              Future.successful(HelloResponse(response = s"hello ${in.name}"))
    }

    val webModule = new WebModule {
      override implicit def routes:Set[Route] =  new HtmlRoute().routes
      override implicit val webBootstrapper: WebBootstrap = new WebBootstrap{
        override def grpcRoute: Option[Route] = Some(
          extractRequestContext { ctx: RequestContext =>
            post{
              handle(ExampleServicePowerApiHandler(new GrpcService))
            }
          }
        )
        override def httpRoute: Route = concat(
          path("hello"){
            get(complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, messageProvider.message("hello",Map("name"->"foobar")))))
          },
          path("testo"){
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
    webModule.lifeCycle.registerForShutdown(9999,"lastHook!!",()=>{println("bye bye")})
    webModule.start()
  }
}
