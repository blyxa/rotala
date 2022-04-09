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

# Quick start
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

