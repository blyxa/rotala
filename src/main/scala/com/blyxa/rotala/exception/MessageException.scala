package com.blyxa.rotala.exception

/**
  * Represents a Throwable who's message can be displayed to
  * end user.
  *
  * @param key This string can be a key/token of i18n or a raw
  *            displayable message
  * @param params name/value pair for token replacement
  */
class MessageException(val key:String,val params:Map[String,String]=Map()) extends Throwable(key){
  override def getMessage: String = key
}

