package io.github.blyxa.rotala.util

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.loader.{ClasspathLoader, FileLoader}
import org.slf4j.LoggerFactory

import java.io.StringWriter
import scala.jdk.CollectionConverters._
class HtmlTemplateEngine
( implicit
  mp:MainProperties
) {
  private val logger = LoggerFactory.getLogger(getClass)

  private val isPRD = mp.getProperty("env","dev")=="prd"
  private val loader = if(isPRD){
    val loader = new ClasspathLoader()
    loader.setPrefix("tpl/")
    logger.info(s"html template engine pebble init with ClasspathLoader[$loader]")
    loader
  }else{
    val loader = new FileLoader()
    val prefix = s"${System.getProperty("user.dir")}/src/main/resources/tpl"
    loader.setPrefix(prefix)
    logger.info(s"html template engine pebble init with FileLoader prefix[$prefix]")
    loader
  }
  private val engine = new PebbleEngine.Builder().loader(loader).cacheActive(isPRD).build()
  def render(tpl:String, context:Map[String,Object]=Map()):HttpEntity.Strict = {
    val compiledTpl = engine.getTemplate(tpl)
    val writer = new StringWriter()
    compiledTpl.evaluate(writer, context.asJava)
    HttpEntity(ContentTypes.`text/html(UTF-8)`, writer.toString)
  }
}
