package com.blyxa.rotala.util

import com.blyxa.rotala.exception.{MessageException, ValidationError}
import org.slf4j.LoggerFactory

class MessageProvider
{
  private val logger = LoggerFactory.getLogger(getClass)
  private var locale:String = _
  private val messages = new MainProperties()
  setLocale("en-US")

  def setLocale(newLocale:String): Unit ={
    locale = newLocale
    messages.load(s"i18n/messages_$locale.properties",false)
  }

  def message(key:String, params:Map[String,String]=Map()):String = {
    var msg:String = messages.getOrElse(key,()=>{
      if(params.nonEmpty){
        logger.warn(s"request to provide message with params but key[$key] not found")
      }
      key
    })
    params.foreach{ case (name, value) =>
      msg = msg.replace(s"{{$name}}", value)
    }
    msg
  }

  def message(e:MessageException):String = e match {
    case exception: ValidationError => exception.getMessage
    case _ =>message(e.key, e.params)
  }
}
