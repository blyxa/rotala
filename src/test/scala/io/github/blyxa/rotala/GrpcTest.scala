package io.github.blyxa.rotala

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.grpc.GrpcClientSettings
import com.typesafe.config.ConfigFactory
import io.github.blyxa.rotala.proto.{ExampleServiceClient, HelloRequest, HelloResponse}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
object GrpcTest{
  def main(args:Array[String]): Unit ={
    val conf = ConfigFactory.defaultApplication()
      .withFallback(ConfigFactory.defaultApplication())
    implicit val system: ActorSystem[Nothing] = ActorSystem[Nothing](Behaviors.empty, "Grpc", conf)

    val client = ExampleServiceClient(GrpcClientSettings
      .connectToServiceAt("localhost",9090)
      .withTls(false))

    val res: HelloResponse = Await.result(
      client.sayHello(HelloRequest(name="testclient")),
      Duration.Inf
    )
    println(s"res.response[${res.response}]")
  }
}
