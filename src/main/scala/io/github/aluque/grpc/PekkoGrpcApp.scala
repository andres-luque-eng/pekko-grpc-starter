package io.github.aluque.grpc

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.{HttpRequest, HttpResponse}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.*
import scala.util.{Success, Failure}

abstract class PekkoGrpcApp(val systemName: String = "PekkoGrpcSystem") {

  /**
   * Child services implement only this method to register their gRPC service handler.
   */
  def buildHandler(system: ActorSystem[?]): HttpRequest => Future[HttpResponse]

  def main(args: Array[String]): Unit = {
    val parsedArgs = ServerArgs.parse(args) match {
      case Right(a) => a
      case Left(err) =>
        System.err.println(s"Error: $err")
        sys.exit(1)
    }

    val config = ConfigResolver.resolve(parsedArgs)
    val host = config.getString("grpc.server.host")
    val port = config.getInt("grpc.server.port")
    val loglevel = config.getString("pekko.loglevel")
    val http2Enabled = config.getBoolean("pekko.http.server.preview.enable-http2")

    val system: ActorSystem[Nothing] = ActorSystem[Nothing](Behaviors.setup[Nothing] { context =>
      val sys = context.system
      implicit val s: ActorSystem[?] = sys
      implicit val ec = sys.executionContext

      sys.log.info(s"[$systemName] Loaded configuration environment: ${parsedArgs.env}")
      sys.log.info(
        s"""Effective configuration:
           |  - Service Name  : $systemName
           |  - Environment   : ${parsedArgs.env}
           |  - gRPC Host     : $host
           |  - gRPC Port     : $port
           |  - Log Level     : $loglevel
           |  - HTTP/2 Enabled: $http2Enabled""".stripMargin
      )

      val handler = buildHandler(sys)
      val bindingFuture = Http().newServerAt(host, port).bind(handler)

      bindingFuture.onComplete {
        case Success(bound) =>
          sys.log.info(s"gRPC server online and listening on: http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}")
        case Failure(ex) =>
          sys.log.error(s"Critical error binding gRPC server to $host:$port!", ex)
          sys.terminate()
      }

      // Orderly shutdown hook
      Runtime.getRuntime.addShutdownHook(new Thread() {
        override def run(): Unit = {
          sys.log.info("Shutdown signal received. Shutting down sockets...")
          val unbound = bindingFuture.flatMap(_.unbind())
          unbound.foreach(_ => sys.terminate())
          Await.result(sys.whenTerminated, 15.seconds)
          sys.log.info("Shutdown completed successfully.")
        }
      })

      Behaviors.empty
    }, systemName, config)

    // Block main thread until ActorSystem terminates
    Await.result(system.whenTerminated, Duration.Inf)
  }
}
