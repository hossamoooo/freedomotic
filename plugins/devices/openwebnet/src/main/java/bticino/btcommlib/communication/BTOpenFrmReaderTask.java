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

public class BTOpenFrmReaderTask extends BTOpenTask implements Runnable {

  public BTOpenFrmReaderTask(InputStream theStream, BTOpenLink theLink) {
    super(theStream, theLink);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void run() {
    String theFrame = null;

    logger.debug("Start frameRead Loop ...");

    for (;;) {

      // retrieve frmae from BTicino devices and notify its to the controller
      theFrame = getAFrame();
      m_openLink.notifyFrame(theFrame);

      // Exit read loop since got an error reading a frame
      if (theFrame == null)
        break;

      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

}
