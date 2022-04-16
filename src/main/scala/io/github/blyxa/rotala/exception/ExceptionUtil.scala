package io.github.blyxa.rotala.exception

/**
  * Util to help create various exceptions
  */
object ExceptionUtil {
  def messageException(key: String, params: Map[String, String] = Map()): MessageException = {
    new MessageException(key, params)
  }

  def messageLeft[R](key: String, params: Map[String, String] = Map()): Either[MessageException, R] = {
    Left(messageException(key, params))
  }

  def throwableLeft[R](msg: String): Either[Throwable, R] = {
    Left(new Throwable(msg))
  }

  def notFoundException: NotFoundException = new NotFoundException
  def accessDeniedException: AccessDeniedException = new AccessDeniedException

  def notFoundLeft[R]: Either[NotFoundException, R] = Left(notFoundException)

  def accessDeniedLeft[R]: Either[AccessDeniedException, R] = Left(accessDeniedException)
}
