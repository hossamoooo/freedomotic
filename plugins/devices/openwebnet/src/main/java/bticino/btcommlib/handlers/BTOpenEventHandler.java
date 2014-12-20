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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import bticino.btcommlib.communication.BTOpenConstFrame;
import bticino.btcommlib.communication.BTOpenProtocolAdapter;
import bticino.btcommlib.domain.lowlevel.BTOpenMessage;
import bticino.btcommlib.domain.util.BTOpenConverter;
import bticino.btcommlib.exceptions.LibException;
import bticino.btcommlib.trace.BTLibLogger;

/**
 * BTOpenEventHandler
 */
public class BTOpenEventHandler {
  private BTOpenConverter conv = new BTOpenConverter();
  private BTOpenProtocolAdapter opa = new BTOpenProtocolAdapter();
  private static BTLibLogger logger = BTLibLogger
      .createLogger("BTOpenEventHandler");

  /**
   * Initializes a new instance of the BTOpenErrHandler class, wit empty
   * constructor
   */
  public BTOpenEventHandler() {
  }

  /**
   * Get OpenMessage
   * 
   * @param theFrame
   *          Frame to Convert
   * @return a BTOpenMessage
   * @throws LibException 
   */
  public BTOpenMessage getOpenMessage(String theFrame) throws LibException {
    // Build Open Message
    BTOpenMessage openMsg = opa.FromFrameToMsg(theFrame);

    return openMsg;
  }

  /**
   * Handle Event
   * 
   * @param theMsg
   *          Low Level Message
   * @param targetMsg
   *          High Level Message
   */
  public void handleEvent(BTOpenMessage theMsg, Object targetMsg)
      throws IllegalArgumentException, IllegalAccessException, LibException {
    // Conversion Phase: from low-level to high-level
    conv.ConvertToHighMsg(theMsg, targetMsg);
  }

  /**
   * Dissect BTicino OPEN frame and it creates a high level language message,
   * defined by user
   * 
   * @param typDic
   *          Type of BTicino frame
   * @param theFrame
   *          BTicino OPEN frame
   * @return High level language message
   */
  @SuppressWarnings("rawtypes")
  public Object dissectFrame(HashMap<String, Class> typDic, String theFrame)
      throws InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException, LibException {
    Object targetInstance = null;

    BTOpenMessage openMsg = opa.FromFrameToMsg(theFrame);
    Class targetMsg = typDic.get(openMsg.getMsgType());

    if (targetMsg != null) {
      targetInstance = targetMsg.newInstance();

    } else {
      // Unable to convert a frame to an object
      logger.error("Ubale to convert frame to Object fo frame : " + theFrame);
    }

    conv.ConvertToHighMsg(openMsg, targetInstance);

    return (targetInstance);
  }

  /**
   * Dissect BTicino OPEN frames and it creates some high level language
   * messages, defined by user
   * 
   * @param typDic
   *          Type of BTicino frames
   * @param theFrame
   *          BTicino OPEN frames 
   * @return High level language messages
   */
  @SuppressWarnings("rawtypes")
  public ArrayList<Object> dissectFrames(HashMap<String, Class> typDic,
      String theFrames, String theSep) throws InstantiationException,
      IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, LibException {

    ArrayList<Object> theRetValues = new ArrayList<Object>();

    String[] framesList = theFrames.split(theSep);

    Object curValue = null;

    for (String curFrame : framesList) {

      if (curFrame.equals(BTOpenConstFrame.ACK_FRM)
          || curFrame.equals(BTOpenConstFrame.NACK_FRM))
        continue;

      curValue = dissectFrame(typDic, curFrame);

      if (curValue != null)
        theRetValues.add(curValue);

    }

    return (theRetValues);
  }
}
