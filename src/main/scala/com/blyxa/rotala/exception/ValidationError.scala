package com.blyxa.rotala.exception

import scala.collection.mutable.ListBuffer

object ValidationError{
  def apply(err:String): ValidationError = {
    val e = new ValidationError()
    e.add(err)
    e
  }
}

class ValidationError extends MessageException("validation_error") {
  private val _errors = ListBuffer[String]()
  def add(error:String): Unit ={
    _errors += error
  }

  override def getMessage: String = {
    s"Validation error found: ${errors().mkString(". ")}"
  }

  def hasError:Boolean = _errors.nonEmpty

  def errors(): List[String] ={
    _errors.toList
  }

  def dumpToString():String = _errors.mkString(",")

}