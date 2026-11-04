package io.github.aluque.grpc

import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}

object ConfigResolver {

  def resolveOrError(args: ServerArgs): Either[String, Config] = {
    val resourceName = s"${args.env}.conf"
    val resource = getClass.getClassLoader.getResource(resourceName)

    if (resource == null) {
      val message = if (args.env == "local") {
        "Configuration file 'local.conf' was not found on classpath.\n" +
          "Create it from the template by running:\n" +
          "  cp src/main/resources/local.conf.template src/main/resources/local.conf"
      } else {
        s"Configuration resource '$resourceName' was not found on classpath."
      }
      Left(message)
    } else {
      val fileConfig: Config = ConfigFactory.load(resourceName)

      // CLI overrides have the highest precedence
      var cliOverrides = ConfigFactory.empty()
      args.port.foreach { p =>
        cliOverrides = cliOverrides.withValue("grpc.server.port", ConfigValueFactory.fromAnyRef(p))
      }
      args.host.foreach { h =>
        cliOverrides = cliOverrides.withValue("grpc.server.host", ConfigValueFactory.fromAnyRef(h))
      }

      Right(cliOverrides.withFallback(fileConfig).resolve())
    }
  }

  def resolve(args: ServerArgs): Config = {
    resolveOrError(args) match {
      case Right(config) => config
      case Left(error) =>
        System.err.println(s"Error: $error")
        sys.exit(1)
    }
  }
}
