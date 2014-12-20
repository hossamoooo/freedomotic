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

public interface IBTCommChan {
    /**
    * Stars connection procedure towards specific Bticino device
    *
    * @param theChanPar Communication channel parameter dictionary
    * @return Return BTCommErr enumearation
    */
    public abstract BTCommErr connect(BTCommChanPar theChanPar);


    /**
    * Disconnect from BTicino gateway taking care of all the underling links (e.g. command and monitor link)
    */
    public abstract void disconnect();


    /**
    * Check the connection status
    *
    * @return True if all the underling links (e.g. command and monitor link) are connected, False if they are not connected
    */
    public abstract boolean isConnected();


    /**
    * Send Message, written in high level format, to BTicino devices and gives back replay frames.
    *
    * @param theMsg high message to send
    * @param theResult Frame received form BTicino devices
    * @return BTCommErr object
    */
    public abstract BTCommErr send(Object theMsg);


    /**
    * Send Message, written in low level string format, to BTicino devices and gives back replay frames.
    *
    * <param theMsg low string message to send
    * <param theResult Frame received form BTicino devices
    * @return BTCommErr object
    */
    public abstract BTCommErr send(String theMsg, ArrayList<String> theResult);


    /**
    * Send Message, write in high level language, to BTicino devices and gives back replay frames.
    *
    * @param theMsg high message to send 
    * @param theResult Frame received form BTicino devices
    * @return BTCommErr object
    */
    public abstract BTCommErr send(Object theMsg, ArrayList<Object> theResult);


    /**
    * Call Event Handle
    *
    * @param type Event status name
    * @param targetEventMsg Event status class type
    */
    @SuppressWarnings("rawtypes")
    public abstract void handleEvent(String type, Class targetEventMsg);


    /**
    * Remove specific Handle instance
    *
    * @param keyType Handle name
    */
    public abstract void removeHandleEvent(String type);


    /**
    * Remove all Handle instances
    */
    public abstract void removeAllHandleEvents();


    /**
    * Invoke made by a Low Level Call
    *
    * @param lowLevelFrame
    */
    public abstract void invokeEventHandler(String lowLevelFrame);

}
