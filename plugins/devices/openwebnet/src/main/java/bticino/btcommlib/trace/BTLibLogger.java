/* 
 * BtCommLib - the OpenWebNet Java Library
 *
 * Copyright (C) 2011 BTicino S.p.A.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package bticino.btcommlib.trace;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BTLibLogger A Logger class used to log messages. Use java
 * -Djava.util.logging.config.file=logging.properties to configure logging
 * properties
 * 
 */
public class BTLibLogger {
  private Logger _logger;

  private BTLibLogger(Logger logger) {
    this._logger = logger;
  }

  /**
   * Public static method to retrieve logger instance, if it's call for the
   * first time, create new istance of logger
   * 
   * @param classToLog
   *          The name of the class to log
   * 
   */
  public static BTLibLogger createLogger(String classToLog) {
    return new BTLibLogger(Logger.getLogger(classToLog));
  }

  /**
   * Write a trace, with TRACE level, into logger
   * 
   * @param msg
   *          Message to be trace
   */
  public void trace(String msg) {
    log(Level.ALL, msg, false);
  }

  /**
   * Write a trace, with TRACE level, into logger. Condition by
   * advancedMsgFormatting
   * 
   * @param msg
   *          Message to be trace
   * @param advancedMsgFormatting
   *          Conditional value, if TRUE the message is formatting else not
   */
  public void trace(String msg, boolean advancedMsgFormatting) {
    log(Level.ALL, msg, advancedMsgFormatting);
  }

  /**
   * Write a trace, with DEBUG level, into logger
   * 
   * @param msg
   *          Message to be trace
   */
  public void debug(String msg) {
    log(Level.FINE, msg, false);
  }

  /**
   * Write a trace, with DEBUG level, into logger. Condition by
   * advancedMsgFormatting
   * 
   * @param msg
   *          Message to be trace
   * @param name
   *          advancedMsgFormatting Conditional value, if TRUE the message is
   *          formatting else not
   */
  public void debug(String msg, boolean advancedMsgFormatting) {
    log(Level.FINE, msg, advancedMsgFormatting);
  }

  /**
   * Write a trace, with INFO level, into logger
   * 
   * @param msg
   *          Message to be trace
   */
  public void info(String msg) {
    log(Level.INFO, msg, false);
  }

  /**
   * Write a trace, with INFO level, into logger. Condition by
   * advancedMsgFormatting
   * 
   * @param msg
   *          Message to be trace
   * @param advancedMsgFormatting
   *          Conditional value, if TRUE the message is formatting else not
   */
  public void info(String msg, boolean advancedMsgFormatting) {
    log(Level.INFO, msg, advancedMsgFormatting);
  }

  /**
   * Write a trace, with WARNING level, into logger
   * 
   * @param msg
   *          Message to be trace
   */
  public void warn(String msg) {
    log(Level.WARNING, msg, false);
  }

  /**
   * Write a trace, with WARING level, into logger. Condition by
   * advancedMsgFormatting
   * 
   * @param msg
   *          Message to be trace
   * @param advancedMsgFormatting
   *          Conditional value, if TRUE the message is formatting else not
   */
  public void warn(String msg, boolean advancedMsgFormatting) {
    log(Level.WARNING, msg, advancedMsgFormatting);
  }

  /**
   * Write a trace, with ERROR level, into logger
   * 
   * @param msg
   *          Message to be trace
   * */
  public void error(String msg) {
    log(Level.SEVERE, msg, false);
  }

  /**
   * Write a trace, with ERROR level, into logger. Condition by
   * advancedMsgFormatting
   * 
   * @param msg
   *          Message to be trace
   * @param advancedMsgFormatting
   *          Conditional value, if TRUE the message is formatting else not
   * */
  public void error(String msg, boolean advancedMsgFormatting) {
    log(Level.SEVERE, msg, advancedMsgFormatting);
  }

  /**
   * Write a trace, with FATAL level, into logger
   * 
   * @param msg
   *          Message to be trace
   */
  public void fatal(String msg) {
    log(Level.SEVERE, msg, false);
  }

  /**
   * Write a trace, with FATAL level, into logger. Condition by
   * advancedMsgFormatting
   * 
   * @param msg
   *          Message to be trace
   * @param advancedMsgFormatting
   *          Conditional value, if TRUE the message is formatting else not
   */
  public void fatal(String msg, boolean advancedMsgFormatting) {
    log(Level.SEVERE, msg, advancedMsgFormatting);
  }

  /**
   * Add a log Handler to receive logging messages
   * 
   * @param handler
   *          a logging Handler
   */
  public void addFormatter(FileHandler handler) {
    _logger.addHandler(handler);
  }

  /**
   * Set the log level specifying which message levels will be logged by this
   * logger
   * 
   * @param newLevel
   *          the new value for the log level (may be null)
   */
  public void setLevel(Level newLevel) {
    _logger.setLevel(newLevel);
  }

  /**
   * Trace log to _logger instance. Condition by advancedMsgFormatting
   * 
   * @param level
   *          Log level
   * @param msg
   *          Message to be trace
   * @param advancedMsgFormatting
   *          Conditional value, if TRUE the message is formatting else not
   */
  private void log(Level level, String msg, boolean advancedMsgFormatting) {
    if (advancedMsgFormatting)
      msg = formatMsg(msg);

    _logger.log(level, msg);
  }

  /**
   * Formatting message adding some information like clock time, file name and
   * line number of the trace
   * 
   * @param msg
   *          Message to be trace
   * @return Message to be trace formatting
   */
  private String formatMsg(String msg) {

    StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
    String text = msg;
    if (callStack.length > 0) {
      // This will report the line number and file name of the original Call!
      text = msg + ", File: " + callStack[0].getFileName() + ", Line: "
          + callStack[0].getLineNumber();
    }
    return text;
  }

}
