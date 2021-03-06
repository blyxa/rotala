package io.github.blyxa.rotala.route

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.github.blyxa.rotala.util.MessageProvider
import io.github.blyxa.rotala.util.{HtmlTemplateEngine, MainProperties, MessageProvider}

class HtmlRoute
(implicit
 htmlTemplateEngine: HtmlTemplateEngine,
 messageProvider: MessageProvider,
 mp:MainProperties
){
  def routes:Set[Route] = Set(
    path("hello"){
      get(complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "hello!")))
    },
    pathSingleSlash{
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

