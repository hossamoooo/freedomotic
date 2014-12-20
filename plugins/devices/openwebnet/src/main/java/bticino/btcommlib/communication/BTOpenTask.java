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

import bticino.btcommlib.trace.BTLibLogger;

public class BTOpenTask {
  protected BTTaskContext m_ctx = null;
  protected InputStream m_ntwStr = null;
  protected BTOpenLink m_openLink = null;
  protected final int FrmBufSize = 1;
  protected byte[] m_frmBuf = new byte[FrmBufSize];
  protected StringBuilder m_frmBld = new StringBuilder();

  /** Class logger */
  protected static BTLibLogger logger = BTLibLogger
      .createLogger("BTOpenFrmReaderTask");

  /**
  * Public BTOpenFrmReaderTask Constructor
  *
  * @param theStream TCP client
  * @param theLink BTOpenLink object, theLink contain channel
  * information
  */
  public BTOpenTask(InputStream theStream, BTOpenLink theLink) {
    m_ntwStr = theStream;
    m_openLink = theLink;
  }

  /**
  * Read frames from BTicino channel
  *
  * return Return frames received
  */
  protected String getAFrame() {
    String out_val = null;
    boolean frameIsReady = false;
    int byCnt = 0;

    logger.debug("getAFrame service invoked on Thread ..."
        + Thread.currentThread().getName());
    try {
      do {
        byCnt = m_ntwStr.read(m_frmBuf, 0, 1);
        if (byCnt > 0) {
          m_frmBld.append(new String(m_frmBuf));

          out_val = m_frmBld.toString();
          if (out_val.endsWith("##")) {
            m_frmBld = new StringBuilder();
            frameIsReady = true;
            logger.debug("getAFrame frame available: " + out_val);
          }
        }
      } while (!frameIsReady);
    } catch (IOException exc) {
      logger.debug("getAFrame ERROR_IOExc: " + exc);
      out_val = null;
    } catch (Exception exc) {
      logger.debug("getAFrame ERROR_Exc: " + exc);
      out_val = null;
    }

    return out_val;
  }

  /**
  * Read Frames from BTicino channel and save into internal data member, of
  * TaskContext type
  *
  * @param data TaskContext object, to save Bticino open frame
  * read
  */
  protected void readAFrame(Object data) throws Exception {
    m_ctx = (BTTaskContext) data;

    logger.debug("readAFrame service invoked ...");
    try {
      m_ctx.setOut_value(getAFrame());
      if (m_ctx.getOut_value() != null)
        m_ctx.setOut_result(true);
      else
        m_ctx.setOut_result(false);
    } catch (Exception exc) {
      logger.debug("readAFrame thread aborted ..." + exc);
      m_ctx.setOut_result(false);
      m_ctx.setOut_value(null);
      throw (exc);
    } finally {
      logger.debug("readAFrame service ended");
    }
  }

}
