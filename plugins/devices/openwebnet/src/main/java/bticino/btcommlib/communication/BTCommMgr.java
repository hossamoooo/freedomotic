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

package bticino.btcommlib.communication;

import bticino.btcommlib.exceptions.LibException;

/**
 * Singleton class to manage communication channel
 */
public class BTCommMgr {
  /** the instance */
  private final static BTCommMgr instance = new BTCommMgr();

  private BTCommMgr() {
  }

  public static BTCommMgr getInstance() {
    return instance;
  }

  /**
   * Gives back a new communication channel, starting from ChanTypes
   * 
   * @param theChanType
   *          Channel Type ( Open, etc...)
   * @return returns an IBTCommChan concrete object instance or LibException if
   *         some errors occurred
   */
  public BTCommChan getCommChan(BTChanTypes theChanType) throws LibException {
    switch (theChanType) {
    case Open:
      return new BTOpenCommChan();
    default:
      throw new LibException("Chan Type not found!");
    }
  }
}
