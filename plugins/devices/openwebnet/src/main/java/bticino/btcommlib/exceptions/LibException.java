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
package bticino.btcommlib.exceptions;

/**
 * A custom exception class
 */

@SuppressWarnings("serial")
public class LibException extends Exception {

  /**
   * Empty constructor, Initializes a new instance of the LibException class
   */
  public LibException() {
  }

  /**
   * Initializes a new instance of the LibException class with a specified error
   * message
   * 
   * @param message
   *          The error message that explains the reason for the exception
   */
  public LibException(String message) {
    super(message);
  }

  /**
   * Initializes a new instance of the LibException class with a specified error
   * message and a reference to the inner exception that is the cause of this
   * exception.
   * 
   * @param message
   *          The error message that explains the reason for the exception
   * @param name
   *          inner The exception that is the cause of the current exception, or
   *          a null reference if no inner exception is specified.
   */
  public LibException(String message, Throwable cause) {
    super(message, cause);
  }
}
