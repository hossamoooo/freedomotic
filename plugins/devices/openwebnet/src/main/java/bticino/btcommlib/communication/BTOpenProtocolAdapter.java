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

import bticino.btcommlib.domain.lowlevel.BTOpenMessage;
import bticino.btcommlib.domain.util.BTOpenMsgType;
import bticino.btcommlib.exceptions.LibException;
import bticino.btcommlib.trace.BTLibLogger;

public class BTOpenProtocolAdapter {
  private static BTLibLogger logger = BTLibLogger.createLogger("BTOpenProtocolAdapter");

  /**
  * Constants that describe the proper structure of the BTicino frames
  */ 
  private final String m_openRequest = "*%s*%s*%s##"; //*CHI*COSA*DOVE##
  private final String m_openRequestStatus = "*#%s*%s##"; //*#CHI*DOVE##
  private final String m_openRequestDimension = "*#%s*%s*%s##"; //*#CHI*DOVE*GRANDEZZA##
  private final String m_openRequestWriteDimension = "*#%s*%s*#%s%s##"; //*#CHI*DOVE*#GRANDEZZA*VAL1*...*VALn##

  private final String m_openEventStatusHead = "*";
  private final String m_openEventStatusDimensionHead = "*#";

  private final String m_openFrameValueSeparator = "*";
  private final String m_openFrameRegValueSeparator = "\\*";
  private final int    m_openFrameValueIndexPosition = 3;
  private final String m_openFrameTail = "##";

  public BTOpenProtocolAdapter() { }

  /**
  * Translator from BTOpenMessage to BTicino string message,
  * see also #region Constants
  *
  * @param theMsg BTOpenMessage to be translate
  * @return BTicino OPEN frame
  */
  public String FromMsgToFrame(BTOpenMessage theMsg)
  {
      //Check for Raw Frame
      if (theMsg.getOpenRawMsg() != null) 
      {
          return theMsg.getOpenRawMsg();
      }

      
      String resultFrame = null;

      //Building Frame
      switch (theMsg.getMsgType())
      {
          case Request:
              resultFrame = String.format(m_openRequest, theMsg.getWho(), theMsg.getWhat(), theMsg.getWhere());
              break;

          case RequestStatus:
              resultFrame = String.format(m_openRequestStatus, theMsg.getWho(), theMsg.getWhere());
              break;

          case RequestDimension:
              resultFrame = String.format(m_openRequestDimension, theMsg.getWho(), theMsg.getWhere(), theMsg.getDimension());
              break;

          case RequestWriteDimension:
              StringBuilder dimValuesStr = new StringBuilder();
              for (int i = 0; i < theMsg.getDimValues().length; i++)
              {
                  dimValuesStr.append(m_openFrameValueSeparator).append(theMsg.getDimValues()[i]);
              }
              resultFrame = String.format(m_openRequestWriteDimension, theMsg.getWho(), theMsg.getWhere(), theMsg.getDimension(), dimValuesStr.toString());
              break;
      }

      return resultFrame;
  }

  /**
  * Translator from BTicino OPEN frame to BTOpenMassage
  *
  * @param theFrame BTicino OPEN frame
  * @return BTOpenMessage, if OPEN frame is syntactically correct, null otherwise
   * @throws LibException 
  */
  public BTOpenMessage FromFrameToMsg(String theFrame) throws LibException 
  {
      BTOpenMessage om = null;
      String bodyFrame = null;
      String[] tokens = null;


      //EventStatusDimension
      if (theFrame.startsWith(m_openEventStatusDimensionHead))
      {
          bodyFrame = theFrame.substring(m_openEventStatusDimensionHead.length(), theFrame.length() - m_openFrameTail.length());
          logger.debug("source: |" + theFrame + "| bodyFrame EventStatusDimension: |" + bodyFrame + "|");

          tokens = bodyFrame.split(m_openFrameRegValueSeparator);

          //Get Dimension Values
          String[] tmpValues = new String[tokens.length - m_openFrameValueIndexPosition];
          System.arraycopy(tokens, m_openFrameValueIndexPosition, tmpValues, 0, tmpValues.length);
          
          if (tokens.length < 3)
            throw new LibException();
          //Fill Low-Level Message
          om = new BTOpenMessage();
          om.setMsgType(BTOpenMsgType.EventStatusDimension);
          om.setWho(tokens[0]);
          om.setWhere(tokens[1]);
          om.setDimension(tokens[2]);
          om.setDimValues(tmpValues);
          om.setOpenRawMsg(theFrame);

          //DEBUG
          for (String item : tokens)
          {
              logger.debug("frame token: " + item);
          }

          for (String item : tmpValues)
          {
              logger.debug("value token: " + item);
          }
      }
      //EventStatus
      else if (theFrame.startsWith(m_openEventStatusHead))
      {
          bodyFrame = theFrame.substring(m_openEventStatusHead.length(), theFrame.length() - m_openFrameTail.length());
          logger.debug("source: |" + theFrame + "| bodyFrame EventStatus: |" + bodyFrame + "|");

          tokens = bodyFrame.split( m_openFrameRegValueSeparator );

          if (tokens.length < 3)
            throw new LibException();
          
          //Fill Low-Level Message
          om = new BTOpenMessage();
          om.setMsgType(BTOpenMsgType.EventStatus);
          om.setWho(tokens[0]);
          om.setWhat(tokens[1]);
          om.setWhere(tokens[2]);
          om.setOpenRawMsg(theFrame);

          //DEBUG
          for (String item:tokens)
          {
              logger.debug("frame token: " + item);
          }
      }
      else
      {
          logger.warn("Frame match not found: " + theFrame);
      }
      
      return om;
  }
}
