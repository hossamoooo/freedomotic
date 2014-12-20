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

import java.util.ArrayList;
import java.util.HashMap;

import bticino.btcommlib.domain.lowlevel.BTOpenMessage;
import bticino.btcommlib.exceptions.LibException;
import bticino.btcommlib.handlers.BTOpenEventHandler;
import bticino.btcommlib.handlers.BTOpenRequestHandler;
import bticino.btcommlib.trace.BTLibLogger;

/*
 * IBTCommChan is the interface that provides all the generic communication service
 * BTOpenCommChan is a concrete class that provides all the services defined by its IBTCommChan abstract
 * class according to OpenWebNet communication schema
 */
public class BTOpenCommChan extends BTCommChan implements IBTOpenLinkNotify {
  private static BTLibLogger logger = BTLibLogger
      .createLogger("BTOpenCommChan");

  @SuppressWarnings("rawtypes")
  private HashMap<String, Class> eventsToHandle = new HashMap<String, Class>();

  private BTOpenRequestHandler openReqHnd = new BTOpenRequestHandler();

  private BTOpenEventHandler openEvHnd = new BTOpenEventHandler();

  private String m_connMode = null;
  private String m_IP = null;
  private int m_PORT = 0;
  private BTOpenLink m_comLink = new BTOpenLink();
  private BTOpenLink m_monLink = new BTOpenLink();

  public BTCommErr connect(BTCommChanPar theChanPar) {

    // extract parameter
    BTCommErr theRetVal = null;

    try {

      m_connMode = theChanPar.getConnPar("MODE");
      m_IP = theChanPar.getConnPar("IP");
      m_PORT = Integer.parseInt(theChanPar.getConnPar("PORT"));

      boolean conRes = true;

      if (isConnected()) {
        return new BTCommErr(BTCommErr.m_errorType.e_AlreadyConnected);
      }

      m_comLink.addListner(this);
      // Start command connection
      if (m_connMode.equals("RW")) {
        conRes = conRes
            && (m_comLink.connect(BTOpenLink.ConnMode.CMD, m_IP, m_PORT));

      } else if (m_connMode.equals("RW_ASY")) {
        conRes = conRes
            && (m_comLink.connect(BTOpenLink.ConnMode.CMD_ASY, m_IP, m_PORT));
      }

    
      // Start monitor connection
      // @@TODO
      m_monLink.addListner(this);

      conRes = conRes
          && m_monLink.connect(BTOpenLink.ConnMode.MON, m_IP, m_PORT);

      if (conRes == false) {
        m_comLink.disconnect();
        m_monLink.disconnect();
        theRetVal = new BTCommErr(BTCommErr.m_errorType.e_NotConnected);
        logger.error("Error connecting Gateway: " + m_IP + " Port: " + m_PORT);
      } else {
        theRetVal = new BTCommErr(BTCommErr.m_errorType.e_NoError);
        logger.error("Successful Connection to Gateway: " + m_IP + " Port: "
            + m_PORT);
      }
    } catch (LibException ex) {
      // Notify Error check if it is an IP not in range connection error
      if (ex.getMessage().equals(BTTaskContext.BT_IP_NOT_IN_RANGE_ERROR)) {
        theRetVal = new BTCommErr(BTCommErr.m_errorType.e_NotInRange);
        logger.error("Error connecting Gateway: " + m_IP + " Port: " + m_PORT
            + ", IP not in range");
      } else {
        theRetVal = new BTCommErr(BTCommErr.m_errorType.e_NotConnected);
        logger.error("Error connecting Gateway: " + m_IP + " Port: " + m_PORT);
      }

      m_comLink.disconnect();
      m_monLink.disconnect();
    }

    return theRetVal;
  }

  @Override
  public void disconnect() {
    m_comLink.disconnect();
    m_monLink.disconnect();

  }

  @Override
  public boolean isConnected() {
    boolean retVal = false;

    if ((m_connMode != null) && (m_connMode.contains("RW"))
        && (m_comLink != null) && (m_monLink != null))
      retVal = (m_comLink.isConnected()) && (m_monLink.isConnected());
    else if (m_monLink != null)
      retVal = m_monLink.isConnected();

    return retVal;
  }

  @Override
  public BTCommErr send(Object theMsg) {
    BTCommErr theRetVal = null;
    StringBuilder replayFrames = new StringBuilder();

    String frameToSend = "";
    try {
      frameToSend = openReqHnd.HandleRequest(theMsg);
    } catch (Exception e) {
      frameToSend = null;
      e.printStackTrace();
    }

    if (!isConnected()) {
      return new BTCommErr(BTCommErr.m_errorType.e_ConnNotAvailable);
    } else if (frameToSend == null) {
      return new BTCommErr(BTCommErr.m_errorType.e_UnableBuilOpenFrame);
    }

    boolean retCode = m_comLink.sendAFrame(frameToSend, replayFrames, false);

    if (retCode) {
      theRetVal = new BTCommErr(BTCommErr.m_errorType.e_NoError);
    } else {
      theRetVal = new BTCommErr(BTCommErr.m_errorType.e_UnableSendFrame);
      logger.error("Error Sending message:  " + theMsg);
    }

    return theRetVal;
  }

  @Override
  public BTCommErr send(String theMsg, ArrayList<String> theResult) {
    BTCommErr theRetVal = null;
    StringBuilder replayFrames = new StringBuilder();
    
    String frameToSend = theMsg;

    if (theResult == null)
      return new BTCommErr(BTCommErr.m_errorType.e_UnableBuilOpenFrame);
    if (m_connMode.contains("RO"))
      return new BTCommErr(BTCommErr.m_errorType.e_ReadOnlySession);
    if (!isConnected()) {
      return new BTCommErr(BTCommErr.m_errorType.e_ConnNotAvailable);
    } else if (frameToSend == null) {
      theResult.add(BTOpenConstFrame.NACK_FRM);
      return new BTCommErr(BTCommErr.m_errorType.e_MissedOpenFrame);
    }

    boolean retCode = m_comLink.sendAFrame(frameToSend, replayFrames, false);

    if (retCode) {
      if (replayFrames != null) {
        // Dissect replyFrames
        String[] framesList = replayFrames.toString().split("\\|");

        for (String curFrame : framesList) {
          if (curFrame.length() > 0)
            theResult.add(curFrame);
        }
      }
      theRetVal = new BTCommErr(BTCommErr.m_errorType.e_NoError);
    } else {
      theRetVal = new BTCommErr(BTCommErr.m_errorType.e_UnableSendFrame);
      logger.error("Error Sending message:  " + theMsg);
      theResult.add(BTOpenConstFrame.NACK_FRM);
    }

    return theRetVal;
  }

  @Override
  public BTCommErr send(Object theMsg, ArrayList<Object> theResult) {
    BTCommErr theRetVal = null;
    StringBuilder replayFrames = new StringBuilder();
    
    if (theResult == null)
      return new BTCommErr(BTCommErr.m_errorType.e_UnableBuilOpenFrame);

    String frameToSend = "";
    try {
      frameToSend = openReqHnd.HandleRequest(theMsg);
    } catch (Exception e1) {
      frameToSend  = null;
      e1.printStackTrace();
    }

    if (!isConnected()) {
      return new BTCommErr(BTCommErr.m_errorType.e_ConnNotAvailable);
    } else if (frameToSend == null) {
      return new BTCommErr(BTCommErr.m_errorType.e_UnableBuilOpenFrame);
    }

    boolean retCode = m_comLink.sendAFrame(frameToSend, replayFrames, false);

    if (retCode) {
      // Dissect answer frames when available
      if (replayFrames != null)
        try {
          theResult = openEvHnd
              .dissectFrames(eventsToHandle, replayFrames.toString(), "\\|");
          theRetVal = new BTCommErr(BTCommErr.m_errorType.e_NoError);
        } catch (Exception e) {
          theRetVal = new BTCommErr(BTCommErr.m_errorType.e_UnableSendFrame);
          e.printStackTrace();
        }
    } else {
      theRetVal = new BTCommErr(BTCommErr.m_errorType.e_UnableSendFrame);
      logger.error("Error Sending message:  " + theMsg);
    }

    return theRetVal;
  }

  @Override
  public void monChanDownNotify() {
    logger.info("Received Chan Monitor Down ");
  }

  @Override
  public void monFrameNotify(Object openRawFrame) {
    BTOpenMessage theOpenMsg = new BTOpenMessage();

    theOpenMsg.setOpenRawMsg((String) openRawFrame);

    logger.info("Received Frame on Monitor Channel:  " + (String) openRawFrame);

    invokeEventHandler(theOpenMsg.getOpenRawMsg());
  }
}
