package io.github.aluque.grpc

import io.grpc.{Status, StatusRuntimeException}

object GrpcError {

  def invalidArgument(message: String): StatusRuntimeException =
    Status.INVALID_ARGUMENT.withDescription(message).asRuntimeException()

  def notFound(message: String): StatusRuntimeException =
    Status.NOT_FOUND.withDescription(message).asRuntimeException()

  def internal(message: String, cause: Throwable = null): StatusRuntimeException =
    Status.INTERNAL.withDescription(message).withCause(cause).asRuntimeException()

  def permissionDenied(message: String): StatusRuntimeException =
    Status.PERMISSION_DENIED.withDescription(message).asRuntimeException()
}
