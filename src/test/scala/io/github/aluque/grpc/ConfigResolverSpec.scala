package io.github.aluque.grpc

class ConfigResolverSpec extends munit.FunSuite {

  test("resolveOrError fails if requested environment conf file is missing") {
    val args = ServerArgs(env = "nonexistent")
    val result = ConfigResolver.resolveOrError(args)
    assert(result.isLeft)
    assert(result.left.getOrElse("").contains("Configuration resource 'nonexistent.conf' was not found"))
  }
}
