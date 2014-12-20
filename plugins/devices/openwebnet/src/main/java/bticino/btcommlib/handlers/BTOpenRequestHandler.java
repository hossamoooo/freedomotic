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

package bticino.btcommlib.handlers;

import bticino.btcommlib.communication.BTOpenProtocolAdapter;
import bticino.btcommlib.domain.lowlevel.BTOpenMessage;
import bticino.btcommlib.domain.util.BTOpenConverter;
import bticino.btcommlib.exceptions.LibException;

/**
 * BTOpenRequestHandler
 */
public class BTOpenRequestHandler {
  private BTOpenConverter conv = new BTOpenConverter();
  private BTOpenProtocolAdapter opa = new BTOpenProtocolAdapter();

  /**
   * Initializes a new instance of the BTOpenRequestHandler class, with empty
   * constructor
   */
  public BTOpenRequestHandler() {
  }

  /**
   * Handle Request
   * 
   * @param theMsg
   *          High Level Msg
   * @return the open frame
   */
  public String HandleRequest(Object theMsg) throws IllegalArgumentException,
      IllegalAccessException, LibException {
    // Conversion Phase: from high-level to low-level
    BTOpenMessage openMsg = (BTOpenMessage) conv.ConvertToLowMsg(theMsg);

    // Build Open Frame
    String frame = opa.FromMsgToFrame(openMsg);

    return frame;
  }

}
