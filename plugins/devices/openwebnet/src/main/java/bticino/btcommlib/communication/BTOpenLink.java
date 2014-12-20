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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EventListener;

import bticino.btcommlib.exceptions.LibException;
import bticino.btcommlib.trace.BTLibLogger;

public class BTOpenLink implements EventListener {
  private ArrayList<IBTOpenLinkNotify> m_listenerList = new ArrayList<IBTOpenLinkNotify>();

  public enum ConnMode {
    CMD, MON, CMD_ASY
  };

  private enum WaitResult {
    KO, OK, T_OUT
  };

  private Socket m_openCli = null;
  private String m_chanFrame;
  private String m_theIP = null;
  private int m_thePORT = 0;
  private BTOpenLink.ConnMode m_chanMode;
  private Thread m_wrkThread = null;
  private Thread m_readerThread = null;
  private boolean m_conSts = false;
  private boolean m_firstCon = false;

  /** Logger object */
  private static BTLibLogger logger = BTLibLogger.createLogger("BTOpenLink");

  /**
   * Empty constructor
   */
  public BTOpenLink() {
    m_wrkThread = null;
    m_firstCon = false;
  }

  public void addListner(IBTOpenLinkNotify listener) {
    m_listenerList.add(listener);
  }

  public void removeListner(IBTOpenLinkNotify listener) {
    m_listenerList.remove(listener);
  }

  /**
   * Called by frameReadLoop method, if theFrame is different from null, forward
   * the frame to every monFrameEve event controller else send a channel down
   * event, to every monChanDownEve event controller
   * 
   * @param theFrame
   *          Monitor frame receive from BTicino devices
   */
  public void notifyFrame(String theFrame) {
    if (theFrame == null) {
      logger.debug(String.format("Channel: %s is DOWN !", m_chanMode.name()));
      m_conSts = false;
      for (IBTOpenLinkNotify icc : m_listenerList)
        icc.monChanDownNotify();
    } else {
      logger.debug(String.format("Received Frame: %s on Channel: %s", theFrame,
          m_chanMode.name()));

      if (m_chanMode == ConnMode.MON)
        for (IBTOpenLinkNotify icc : m_listenerList)
          icc.monFrameNotify(theFrame);
    }
  }

  /**
   * Try to connect to BTicino device
   * 
   * @param theMode
   *          Type of connection, take from BTOpenLink.ConnMode enumeration
   * 
   * @param theIp
   *          BTicino device ip address
   * @param thePort
   *          BTicino device port to be connected
   * @return return true on connection, false otherwise
   */
  public boolean connect(ConnMode theMode, String theIp, int thePort)
      throws LibException {

    logger.info(String.format(
        "Start connection procedure for chan: %s, on Gateway: %s, Port: %d ",
        theMode.name(), theIp, thePort));

    boolean retCode = false;
    InetAddress theIpAdd;
    m_chanMode = theMode;
    m_openCli = new Socket();

    try {
      theIpAdd = InetAddress.getByName(theIp);
      m_openCli.connect(new InetSocketAddress(theIpAdd, thePort));
      if (doConnect()) {
        retCode = true;
        m_conSts = true;
        m_theIP = theIp;
        m_thePORT = thePort;
        m_firstCon = true;
        if (m_chanMode == ConnMode.MON)
          startReadLoop();
      } else {
        // Trace the error: connection flow error
        retCode = false;
      }

    } catch (LibException ex) {
      logger
          .error(String
              .format(
                  "Connection procedure for chan: %s, on Gateway: %s, Port: %d Ended with: %s",
                  theMode.name(), theIp, thePort, "ERROR, IP not in range !!"));
      retCode = false;
      throw ex;
    } catch (IOException exIO) {
	  logger
          .error(String
              .format(
                  "Connection procedure for chan: %s, on Gateway: %s, Port: %d Ended with: %s",
                  theMode.name(), theIp, thePort, "ERROR !!"));
      logger.debug("Connect error exception details: " + exIO);
      retCode = false;
    }
	
    return (retCode);
  }

  /**
   * Disconnect every devices, channel and destroy active thread
   */
  public void disconnect() {

    logger.info("Disconnecting from Gateway: " + m_theIP + "  Port: "
        + m_thePORT);

    if (m_wrkThread != null)
      m_wrkThread.interrupt();

    if (m_readerThread != null)
      m_readerThread.interrupt();

    if (m_openCli != null)
      try {
        m_openCli.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    m_openCli = null;
    m_wrkThread = null;
    m_readerThread = null;

    m_conSts = false;

  }

  /**
   * Check status channel, if connected send frame to low level and if necessary
   * wait the answer. If channel is down, try to reconnect the channel and retry
   * the sending
   * 
   * @param theFrame
   *          Frame to be sent
   * @param replayFrames
   *          the output frames
   * @param retryMode
   *          true if to proceed to retry, false otherwise
   * @return true if frame is sent, false otherwise
   */
  public boolean sendAFrame(String theFrame, StringBuilder replayFrames,
      boolean retryMode) {
    boolean retCode = false;
    //StringBuilder collecteFrames = new StringBuilder();

    logger.debug("sendAFrame service invoked with frame: " + theFrame
        + " on chan: " + m_chanMode.name());

    if ((m_chanMode == ConnMode.MON) && (m_conSts == true))
      return (false);

    retCode = writeFrame(theFrame);

    if (retCode == false) {
      logger.debug("Channel connection is down try to connect: " + m_chanMode.name());
      if ((m_conSts == false) && (m_firstCon == true))
        if (reConnect())
          retCode = writeFrame(theFrame);
        else
          retCode = false;
    }

    if (m_chanMode == ConnMode.CMD_ASY)
      retCode = true;
    else if (m_chanMode == ConnMode.CMD) {
      if (waitAFrame(BTOpenConstFrame.ACK_NACK_FRM, 5, false, replayFrames) == WaitResult.OK) {
        //replayFrames = collecteFrames;
        if ((replayFrames != null)
            && (replayFrames.toString().contains(BTOpenConstFrame.ACK_FRM)))
          retCode = true;
        else
          retCode = false;
      } else {
        if (retryMode)
          retCode = false;
        else {
          // if not in retry-mode issue a disconnect and a resend
          disconnect();
          retCode = sendAFrame(theFrame, replayFrames, true);
          //replayFrames = collecteFrames;
        }
      }
    }
    return (retCode);
  }

  /**
   * Check if device is connected
   * 
   * @return true if device is connected, false otherwise
   */
  public boolean isConnected() {
    return m_conSts;
  }

  /**
   * Take new stream form open channel
   * 
   * @return New stream object
   */
  private InputStream getNetwStream() {
    InputStream theNtwStr = null;
    try {
      theNtwStr = m_openCli.getInputStream();
    } catch (Exception e) {
      logger.debug("Network Stream for Channel: " + m_chanMode.name()
          + "  no longer available");
    }

    return (theNtwStr);
  }

  /**
   * Makes a waiting thread to collect device answer; the thread is stopped when
   * find an answer or for timeout
   * 
   * @param theFrame
   *          Frame to be compared (ACK, NACK, etc...)
   * @param timeOut
   *          Thread timeout, write in seconds
   * @param exactMatch
   *          Extra parameter
   * @param collectedFrames
   *          collectedFrames contain response frame, received from the device
   * @return return OK if find the answer and it's equal to theFrame (input
   *         parameter)
   */
  private WaitResult waitAFrame(String theFrame, int timeOut,
      boolean exactMatch, StringBuilder collecteFrames) {
    WaitResult retCode = WaitResult.KO;

    int curTOut = timeOut * 1000;
    // collectedFrames = null;

    logger.debug("waitAFrame service invoked for frame: " + theFrame
        + " with TimeOut: " + timeOut + " Sec. and ExactMatchMode: "
        + exactMatch);

    InputStream theNtwStr = getNetwStream();

    if ((m_wrkThread == null) && (theNtwStr != null)) {
      // Create a background thread with the Related task
      // Sets its context and Starts the task
      BTTaskContext t_ctx = new BTTaskContext();
      t_ctx.setCtx_name("waitFrameTask:" + m_chanMode.name());
      t_ctx.setIn_value(theFrame);
      t_ctx.setIn_exactMD(exactMatch);

      try {
        m_wrkThread = new Thread(new BTOpenWaitFrameTask(
            m_openCli.getInputStream(), this, t_ctx));
      } catch (IOException e) {
        m_wrkThread = null;
        retCode = WaitResult.T_OUT;
        return retCode;
      }
      m_wrkThread.setName("waitFrameTask");
      m_wrkThread.setDaemon(true);

      m_wrkThread.start();

      // Wait at least timeout seconds
      try {
        m_wrkThread.join(curTOut);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block

      }

      logger
          .debug("waitAFrame service End, Return to original Thread, result: "
              + t_ctx.getOut_value());
      if (!m_wrkThread.isAlive()) {
        m_wrkThread = null;
        if (t_ctx.isOut_result()) {
          logger.debug("waitAFrame service success");
          collecteFrames.append(t_ctx.getOut_value());
          retCode = WaitResult.OK;
        } else {
          logger.debug("waitAFrame service failed !!");
          retCode = WaitResult.KO;
          if (t_ctx.getOut_value() != null)
            collecteFrames.append(t_ctx.getOut_value());
        }
      } else {
        logger.debug("waitAFrame service Timeout !!");
        m_wrkThread.interrupt();
        m_wrkThread = null;
        retCode = WaitResult.T_OUT;
      }
    } else {
      // Operation already on-goig
      retCode = WaitResult.KO;
    }

    return (retCode);
  }

  /**
   * Send frame without waiting for the answer
   * 
   * @param theFrame
   *          Frame to be sent
   * @return Return true if the frame is sent, false otherwise
   */
  private boolean writeFrame(String theFrame) {
    boolean retCode = false;

    logger.debug("writeFrame service Invoked with Frame: " + theFrame);
    try {
      byte[] myWriteBuffer = theFrame.getBytes();
      m_openCli.getOutputStream().write(myWriteBuffer, 0, myWriteBuffer.length);

      retCode = true;

    } catch (Exception e) {
      // TRACE THE Error ...
      logger.error("writeFrame service error" + e);
      retCode = false;
      m_conSts = false;
    }

    return (retCode);
  }

  /**
   * Try to reconnect monitor channel
   * 
   * @return true if channel is reconnected, false otherwise
   */
  private boolean reConnect() {

    logger.info("Reconnect procedure invoked ...");

    disconnect();

    try {
      return (connect(m_chanMode, m_theIP, m_thePORT));
    } catch (LibException e) {

    }
    return false;
  }

  /**
  * Connection step implementation, to initialize channel protocol
  *
  * @return true if if protocol is completed correctly
  */
  private boolean doConnect() throws LibException {
    StringBuilder collecteFrames = new StringBuilder();
    boolean retCode = false;

    logger.debug("doConnect procedure invoked ...");

    // Set the channel type frame depending on the operational mode
    if ((m_chanMode == BTOpenLink.ConnMode.CMD)
        || m_chanMode == BTOpenLink.ConnMode.CMD_ASY)
      m_chanFrame = BTOpenConstFrame.CMDCHAN_FRM;
    else
      m_chanFrame = BTOpenConstFrame.MONCHAN_FRM;

    // Execute the foreseen negotiation
    if (waitAFrame(BTOpenConstFrame.ACK_FRM, 30, true, collecteFrames) == WaitResult.OK)
      if (writeFrame(m_chanFrame))
        if (waitAFrame(BTOpenConstFrame.ACK_FRM, 30, true, collecteFrames) == WaitResult.OK)
          retCode = true;
        else {
          if (collecteFrames.toString().matches(
              BTOpenConstFrame.NOT_IN_RANGE_FRM)) {
            logger.debug("Waiting for channel mode:" + m_chanMode.name()
                + " confirmation failed, IP not in range !!");
            throw new LibException(BTTaskContext.BT_IP_NOT_IN_RANGE_ERROR);
          } else {
            logger.debug("Waiting for channel mode:" + m_chanMode.name()
                + " confirmation failed !!");
          }
        }
      else
        logger.debug("Write channel mode failed !!!");
    else
      logger.debug("Waiting for initial ACK Failed !!!");

    if (retCode == true)
      logger.debug("doConnect procedure invoked ended with success !");

    return (retCode);
  }

  /**
  * Create a thread to stay in loop to receive same, event, frame from
  * devices
  */
  private void startReadLoop() {
    if (m_readerThread == null) {
      logger.debug("Start read Thread for chan: " + m_chanMode.name());

      // Create a background thread to put in readLoop
      try {
        m_readerThread = new Thread(new BTOpenFrmReaderTask(
            m_openCli.getInputStream(), this));
      } catch (IOException e) {
        return;
      }
      m_readerThread.setName("frameReadLoop");
      m_readerThread.setDaemon(true);

      m_readerThread.start();
    }
  }
}
