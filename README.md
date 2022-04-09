# Rotala is a web framework boilerplate for Scala
It is preconfigured with
* Scala
* akka-http
* akka-grpc
* pebble templates
* jackson json
* jsoup
* unirest
* HikariCP

# Quick demo
```bash
git clone git@github.com:blyxa/rotala.git
cd rotala
./gradlew shadowJar
java -jar build/libs/rotala-1.0.0-all.jar
```

In another window
```bash
curl http://localhost:8080/hello
```

# Development
ExampleMain.scala contains an example of a main entry point.
```scala
    val webModule = new WebModule {
      override implicit val webBootstrapper: WebBootstrap = new WebBootstrap{
        override def httpRoute: Route = concat(
          path("hello"){
            get(complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, messageProvider.message("hello",Map("name"->"foobar")))))
          }
        )
      }
    }
    webModule.lifeCycle.registerForShutdown(9999,"lastHook!!",()=>{println("bye bye")})
    webModule.start()
```