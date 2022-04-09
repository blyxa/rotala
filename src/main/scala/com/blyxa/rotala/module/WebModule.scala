package com.blyxa.rotala.module

import akka.http.scaladsl.server.Route
import com.blyxa.rotala.util.{HtmlTemplateEngine, WebBootstrap}

/**
 * This class extends the CommonModule and provides additional
 * components relevant to a web application
 */
abstract class WebModule extends CommonModule {

  /**
   * html template engine (Pebble)
   */
  implicit val htmlTemplateEngine: HtmlTemplateEngine = new HtmlTemplateEngine()(mp)

  /**
   * Initialize the akka http server
   */
  implicit val webBootstrapper: WebBootstrap = new WebBootstrap

  def start(): Unit ={
    lifeCycle.start(()=>webBootstrapper.start())
  }

  /**
   * Declare all akka routes
   */
  implicit def routes:Set[Route]

}
