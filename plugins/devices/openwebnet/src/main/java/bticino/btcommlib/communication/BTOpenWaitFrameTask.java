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

import java.io.InputStream;

public class BTOpenWaitFrameTask extends BTOpenTask implements Runnable {

  public BTOpenWaitFrameTask(InputStream theStream, BTOpenLink theLink,BTTaskContext data) {
    super(theStream, theLink);
    
    m_ctx = (BTTaskContext) data;
  }

  /**
  * waiting for a specific frame (eg. ACK or NACK)
  *
  * @param data Object that contain frame to be compare and pushing
  * the result
  */
  @Override
  public void run() {
   
    String theFrame = null;
    boolean exitLoop = false;

    logger.debug("waitAFrame service invoked ..." + m_ctx.getCtx_name());

    try {
      m_ctx.setOut_value("");
      m_ctx.setOut_result(false);

      do {
        theFrame = getAFrame();

        if (theFrame != null) {
          logger.debug("Waiting the frame: " + m_ctx.getIn_value()
              + "  Got the frame: " + theFrame);

          // Collect the frames that comes before the expected one
          m_ctx.setOut_value(m_ctx.getOut_value() + "|" + theFrame);

          if (m_ctx.getIn_value().contains(theFrame)) {
            exitLoop = true;
            m_ctx.setOut_result(true);
          } else if (m_ctx.isIn_exactMD() == true) {
            exitLoop = true;
            m_ctx.setOut_result(false);
          }
        } else {
          logger.debug("waitAFrame reading error ");
          exitLoop = true;
        }
      } while (!exitLoop);

      logger.debug("waitAFrame ended Frame received: " + m_ctx.getOut_value());

    } catch (Exception exc) {
      logger.debug("Thread Aborted: " + exc);
      m_ctx.setOut_result(false);
    } finally {
      logger.debug("waitAFrame service ended");
    }
  }
}
