package io.github.aluque.grpc

class ServerArgsSpec extends munit.FunSuite {

  test("parse defaults to development when no arguments are passed") {
    val result = ServerArgs.parse(Array.empty)
    assertEquals(result, Right(ServerArgs(env = "development", port = None, host = None)))
  }

  test("parse accepts valid --config argument") {
    val result = ServerArgs.parse(Array("--config=staging"))
    assertEquals(result, Right(ServerArgs(env = "staging", port = None, host = None)))
  }

  test("parse accepts valid --grpc.port argument") {
    val result = ServerArgs.parse(Array("--grpc.port=4000"))
    assertEquals(result, Right(ServerArgs(env = "development", port = Some(4000), host = None)))
  }

  test("parse accepts valid --grpc.host argument") {
    val result = ServerArgs.parse(Array("--grpc.host=127.0.0.1"))
    assertEquals(result, Right(ServerArgs(env = "development", port = None, host = Some("127.0.0.1"))))
  }

  test("parse rejects invalid environment") {
    val result = ServerArgs.parse(Array("--config=invalid"))
    assert(result.isLeft)
    assert(result.left.getOrElse("").contains("Invalid environment"))
  }

  test("parse rejects invalid port") {
    val result = ServerArgs.parse(Array("--grpc.port=99999"))
    assert(result.isLeft)
    assert(result.left.getOrElse("").contains("Invalid port"))
  }
}
