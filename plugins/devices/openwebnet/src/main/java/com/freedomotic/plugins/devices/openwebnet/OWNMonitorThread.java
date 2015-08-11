/**
 *
 * Copyright (c) 2009-2015 Freedomotic team http://freedomotic.com
 * 
* This file is part of Freedomotic
 * 
* This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 * 
* This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
* You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.plugins.devices.openwebnet;

import com.myhome.fcrisciani.connector.MyHomeJavaConnector;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.myhome.fcrisciani.exception.MalformedCommandOPEN;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OWNMonitorThread extends Thread {

    private static OpenWebNet pluginReference = null;
    private MyHomeJavaConnector myPlant = null;
    private String ipAddress = null;
    private Integer port = 0;

    public void run() {
        try {
            myPlant.startMonitoring();
            while (true) {
                try {
                    String readFrame = myPlant.readMonitoring();
                    pluginReference.LOG.log(Level.INFO, "Received frame ''{0}'' ", readFrame);
                    pluginReference.buildEventFromFrame(readFrame);
                } catch (InterruptedException ex) {
                    pluginReference.LOG.log(Level.SEVERE, "Monitoring interrupted for: " + ex.getLocalizedMessage(), ex);
                }
            }
            // use new nethod readMonitoring(pluginReference)  
            //  try {
            //      myPlant.startMonitoring();
            //      myPlant.readMonitoring(pluginReference);
            //  } catch (IOException ex) {
            //  } catch (InterruptedException ex) {
            //  }
            //  }
        } catch (IOException ex) {
            pluginReference.LOG.log(Level.SEVERE, "IOException during startMonitoring() " + ex.getLocalizedMessage(), ex);
        }

    }

    public OWNMonitorThread(OpenWebNet pluginReference, String ipAddress, Integer port) {

        this.pluginReference = pluginReference;
        this.ipAddress = ipAddress;
        this.port = port;
        //connect to own gateway
        myPlant = new MyHomeJavaConnector(ipAddress, port);
    }

    public void sendCommand(String command, int priority) {
        try {
            myPlant.sendCommandAsync(command, priority);
        } catch (MalformedCommandOPEN ex) {
            pluginReference.LOG.log(Level.SEVERE, "Malformed frame not sent." + ex.getLocalizedMessage(), ex);
        }
    }

    public void stopMonitoring() {
        try {
            myPlant.stopMonitoring();
            myPlant = null;
        } catch (IOException ex) {
            pluginReference.LOG.log(Level.SEVERE, "Error during plugin stopping for " + ex.getLocalizedMessage(), ex);
        }
    }
}
