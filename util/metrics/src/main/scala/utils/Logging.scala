package utils

import org.slf4j.LoggerFactory
import play.api.Logger
import utils.metrics.Instrumented

object Logging extends Instrumented {
  private[this] val traceMeter = metrics.meter("log.trace")
  private[this] val debugMeter = metrics.meter("log.debug")
  private[this] val infoMeter = metrics.meter("log.info")
  private[this] val warnMeter = metrics.meter("log.warn")
  private[this] val errorMeter = metrics.meter("log.error")

  private[this] var callback: Option[(Int, String) => Unit] = None

  def setCallback(f: (Int, String) => Unit) = callback = Some(f)

  case class CustomLogger(name: String) extends Logger(LoggerFactory.getLogger(name)) {
    def trace(message: => String) = {
      traceMeter.mark()
      super.trace(message)
    }
    def trace(message: => String, error: => Throwable) = {
      traceMeter.mark()
      super.trace(message, error)
    }
    def debug(message: => String) = {
      debugMeter.mark()
      super.debug(message)
    }
    def debug(message: => String, error: => Throwable) = {
      debugMeter.mark()
      super.debug(message, error)
    }
    def info(message: => String) = {
      callback.foreach(_(1, message))
      infoMeter.mark()
      super.info(message)
    }
    def info(message: => String, error: => Throwable) = {
      callback.foreach(_(1, message))
      infoMeter.mark()
      super.info(message, error)
    }
    def warn(message: => String) = {
      callback.foreach(_(2, message))
      warnMeter.mark()
      super.warn(message)
    }
    def warn(message: => String, error: => Throwable) = {
      callback.foreach(_(2, message))
      warnMeter.mark()
      super.warn(message, error)
    }
    def error(message: => String) = {
      callback.foreach(_(3, message))
      errorMeter.mark()
      super.error(message)
    }
    def error(message: => String, error: => Throwable) = {
      callback.foreach(_(3, message))
      errorMeter.mark()
      super.error(message, error)
    }
    def errorThenThrow(message: => String) = {
      this.error(message)
      throw new IllegalStateException(message)
    }
    def errorThenThrow(message: => String, error: => Throwable) = {
      this.error(message, error)
      throw error
    }
  }
}

trait Logging {
  protected[this] val log = Logging.CustomLogger(s"scala-js-typescript.${this.getClass.getSimpleName.replace("$", "")}")
}