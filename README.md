# pekko-grpc-starter

A reusable microservice chassis library for **Apache Pekko gRPC** services written in **Scala 3**.

---

## What It Provides

- **`PekkoGrpcApp`**: Base class handling the full Pekko gRPC lifecycle (`ActorSystem`, HTTP/2 socket binding, English logging, graceful JVM shutdown hooks).
- **`reference.conf`**: Default configurations (HTTP/2 preview enabled, default host/port/log level) automatically inherited by any downstream service.
- **`ServerArgs` & `ConfigResolver`**: CLI argument parsing (`--config`, `--grpc.port`, `--grpc.host`) with strict 4-level precedence:
  ```
  CLI arguments > Environment variables > <env>.conf > reference.conf
  ```
- **`GrpcError`**: Helper methods for standard gRPC status codes (`invalidArgument`, `notFound`, `internal`, `permissionDenied`).

---

## Local Development & Publishing

### 1. Compile & Test
```bash
sbt test
```

### 2. Publish Locally (Offline, $0 Cost)
To make this library available to other projects on your local machine:
```bash
sbt publishLocal
```
This publishes to `~/.ivy2/local/io.github.andres-luque-eng/pekko-grpc-starter_3/0.1.0-SNAPSHOT/`.

### 3. Release to GitHub Packages (Free via GitHub Actions)
Push a git tag to trigger the automatic GitHub Actions publishing workflow:
```bash
git tag v0.1.0
git push origin v0.1.0
```

---

## Consuming in a Microservice

### In `build.sbt`:
```scala
resolvers += "GitHub Packages" at "https://maven.pkg.github.com/andres-luque-eng/*"

libraryDependencies += "io.github.andres-luque-eng" %% "pekko-grpc-starter" % "0.1.0-SNAPSHOT"
```

### In `Server.scala`:
Downstream services only need to implement `buildHandler`:
```scala
package com.example.myservice

import io.github.aluque.grpc.PekkoGrpcApp
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.http.scaladsl.model.{HttpRequest, HttpResponse}
import scala.concurrent.Future

object MyServer extends PekkoGrpcApp("MyServiceSystem") {
  override def buildHandler(system: ActorSystem[?]): HttpRequest => Future[HttpResponse] = {
    val service = new MyServiceImpl(system)
    MyServiceHandler.withServerReflection(service)(system)
  }
}
```
