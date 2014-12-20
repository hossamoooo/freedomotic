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
/**
 * 
 */
package bticino.btcommlib.communication;

import java.util.ArrayList;
import java.util.HashMap;

import bticino.btcommlib.domain.lowlevel.BTOpenMessage;
import bticino.btcommlib.domain.util.BTOpenMsgType;
import bticino.btcommlib.exceptions.LibException;
import bticino.btcommlib.handlers.BTOpenEventHandler;

/**
 * 
 */
public abstract class BTCommChan implements IBTCommChan {
  @SuppressWarnings("rawtypes")
  private HashMap<String, Class> m_eventsToHandle = new HashMap<String, Class>();
  private ArrayList<IBTCommNotify> m_listenerList = new ArrayList<IBTCommNotify>();
  private BTOpenEventHandler m_openEvHnd = new BTOpenEventHandler();

  
  public void addListner(IBTCommNotify listener) {
    m_listenerList.add(listener);
  }

  public void removeListner(IBTCommNotify listener) {
    m_listenerList.remove(listener);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * bticino.btcommlib.communication.IBTCommChan#handleEvent(java.lang.String,
   * java.lang.Class)
   */
  @Override
  public void handleEvent(String type, @SuppressWarnings("rawtypes") Class targetEventMsg) {
    m_eventsToHandle.put(type, targetEventMsg);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * bticino.btcommlib.communication.IBTCommChan#removeHandleEvent(java.lang
   * .String)
   */
  @Override
  public void removeHandleEvent(String type) {
    m_eventsToHandle.remove(type);
  }

  /*
   * (non-Javadoc)
   * 
   * @see bticino.btcommlib.communication.IBTCommChan#removeAllHandleEvents()
   */
  @Override
  public void removeAllHandleEvents() {
    m_eventsToHandle.clear();
  }

  protected void riseError(BTCommErr highLevelErrorMsg) {
    for (IBTCommNotify icc : m_listenerList)
      icc.notifyError(highLevelErrorMsg);
  }

  protected void riseEvent(Object highLevelEventMsg) {
    for (IBTCommNotify icc : m_listenerList)
      icc.notifyEvent(highLevelEventMsg);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * bticino.btcommlib.communication.IBTCommChan#invokeEventHandler(java.lang
   * .String)
   */
  @SuppressWarnings({"rawtypes" })
  @Override
  public void invokeEventHandler(String lowLevelFrame) {
    Object targetInstance = null;

    try {
      // Obtain Open Message From a Frame
      BTOpenMessage openMsg = m_openEvHnd.getOpenMessage(lowLevelFrame);

      if (openMsg == null) {
        throw new LibException(
            "Unable to obtain Open Message From this Frame: " + lowLevelFrame);
      }

      BTOpenMsgType msgType = openMsg.getMsgType();

      // Prepare Target Msg
      
      Class targetMsg = m_eventsToHandle.get(msgType.name());
      if (targetMsg != null) {
        targetInstance = targetMsg.newInstance();

      } else {
        throw new LibException("Unable to instantiate :" + msgType.name());
      }

      // Building High Level Message Instance
      m_openEvHnd.handleEvent(openMsg, targetInstance);

    } catch (Exception exc) {
      // Notify Error
      riseError(new BTCommErr(BTCommErr.m_errorType.e_NotConnected,
          exc.toString()));

      return;
    }

    // Notify
    if (targetInstance != null)
      riseEvent(targetInstance);

  }

}
