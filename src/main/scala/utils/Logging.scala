package utils

import org.slf4j.{Logger, LoggerFactory}

trait Logging {
  protected def log: Logger = LoggerFactory.getLogger(this.getClass)
}
