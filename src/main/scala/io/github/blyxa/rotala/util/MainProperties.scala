package io.github.blyxa.rotala.util

import java.io.FileInputStream
import java.util.Properties
import scala.io.Source
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

class MainProperties(configPath:String, required:Boolean=false) extends Properties{
  if(configPath!=null) load(configPath,required)

  def this() = this(null)

  /**
   * Loads args passed into the static main(args:Array[String]) method
   * Only -Pname=value paired params are supported
   */
  private val paramNamePrefix = "-P"
  def loadArgs(args:Array[String]): Unit ={
    args.foreach{param=>
      val nv = param.split('=')
      if(nv.head.startsWith(paramNamePrefix)) {
        if(nv.length==1){
          setProperty(nv.head.substring(paramNamePrefix.length), "")
        }else{
          setProperty(nv.head.substring(paramNamePrefix.length), nv.last)
        }
      }
    }
  }

  def loadFromClasspath(path:String): Either[Throwable, MainProperties] ={
    Try(getClass.getClassLoader.getResourceAsStream(path)) match {
      case Failure(e) => Left(e)
      case Success(stream) if stream != null => Try(load(stream)) match {
        case Failure(e) => Left(e)
        case Success(_) => Right(this)
      }
      case _ => Left(new Throwable(s"failed to get resource[$path] as stream"))
    }
  }

  def loadFromFileSys(path:String):Either[Throwable, MainProperties] = {
    Try(new FileInputStream(path)) match {
      case Failure(e) => Left(e)
      case Success(stream) => Try(load(stream)) match {
        case Failure(e) => Left(e)
        case Success(_) => Right(this)
      }
    }
  }

  def load(resource:String,required:Boolean): Unit ={
    loadFromClasspath(resource) match {
      case Left(cpe) => loadFromFileSys(resource) match {
        case Left(fse) =>
          val msg = s"""Failed to load properties tried classpath and filesys
                       |file[$resource]
                       |classpath err[${cpe.getMessage}]
                       |filesys err[${fse.getMessage}]
                       |""".stripMargin
          if(required) throw new Throwable(msg)
        case Right(_) =>
      }
      case Right(_) =>
    }
  }

  def getRequiredProperty(key:String ):String = {
    val value = getProperty(key)
    if(value==null) throw new RuntimeException(s"Required property[$key] not found")
    value
  }

  /**
   * Some properties are special in that it points to a file that contains the actual value.
   * eg. Passwords should be stored in a file and referenced by file only.
   * Then this method should be used to retrieve the content of the file
   */
  def getRequiredPropertyContent(key:String):String = {
    val keyFile = getRequiredProperty(key)
    val contentKey = s"$key.content"
    if(!containsKey(contentKey)) {
      val src = Source.fromFile(keyFile)
      val contents = src.getLines().mkString("\n")
      src.close()
      setProperty(contentKey, contents)
    }
    getProperty(contentKey)
  }

  def getOrElse(key:String,elseFunc:()=>String):String = {
    val value = getProperty(key)
    if(value==null) elseFunc()
    else value
  }

  def getOrDefault(key:String,default:String):String = {
    val value = getProperty(key)
    if(value==null) default
    else value
  }

  def getOrUpdate(key:String,default:String):String = {
    val value = getProperty(key)
    if(value==null) {
      setProperty(key,default); default
    }
    else value
  }

  def toMap:Map[String,String]={
    keys().asScala.map(k=>{
      val kk = k.asInstanceOf[String]
      (kk, getProperty(kk))
    }).toMap
  }
  def strip(prefix:String):MainProperties = {
    val mainProperties = new MainProperties
    keys().asScala
      .map(_.asInstanceOf[String])
      .filter(_.startsWith(prefix))
      .foreach(k=>{
        mainProperties.setProperty(k.substring(prefix.length+1), getProperty(k))
      })
    mainProperties
  }

  def offer(key:String, value:String):Boolean = {
    if(!containsKey(key)){
      setProperty(key, value)
      true
    }else{
      false
    }
  }
  def prettyString:String = {
    keys().asScala.map(_.asInstanceOf[String]).toList.sorted.map(k=>s"$k:${getProperty(k)}").mkString("\n")
  }
}

