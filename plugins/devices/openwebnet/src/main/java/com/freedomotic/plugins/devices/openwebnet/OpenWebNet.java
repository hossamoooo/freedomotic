/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.myhome.fcrisciani.connector.MyHomeJavaConnector;
import com.myhome.fcrisciani.exception.MalformedCommandOPEN;

public class OpenWebNet extends Protocol {
    /*
     * Initializations
     */

    public static final Logger LOG = Logger.getLogger(OpenWebNet.class.getName());
    private final String host = configuration.getProperty("host");
    private final Integer port = Integer.parseInt(configuration.getProperty("port"));
    static MyHomeJavaConnector myPlant = null;
    private OWNMonitorThread monitorSessionThread = null;
    private String address = null;
    private static String frame = null;
    OWNFrame JFrame = new OWNFrame(this);

    /*
     *
     * OWN Diagnostic Frames
     *
     */
    final static String LIGHTNING_DIAGNOSTIC_FRAME = "*#1*0##";
    final static String AUTOMATIONS_DIAGNOSTIC_FRAME = "*#2*0##";
    final static String ALARM_DIAGNOSTIC_FRAME = "*#5##";
    final static String POWER_MANAGEMENT_DIAGNOSTIC_FRAME = "*#3##";

    /*
     *
     * OWN Control Messages
     *
     */
    final static String MSG_OPEN_ACK = "*#*1##";
    final static String MSG_OPEN_NACK = "*#*0##";

    public OpenWebNet() {
        super("OpenWebNet", "/openwebnet/openwebnet-manifest.xml");
    }

    protected void onShowGui() {
        bindGuiToPlugin(JFrame);
    }


    @Override
    public void onStart() {
        setPollingWait(-1);
        // create thread 
        monitorSessionThread = new OWNMonitorThread(this, host, port);
        // start thread 
        monitorSessionThread.start();
    }

    @Override
    protected void onRun() {
        // syncronizes the software with the system status
        initSystem();
    }

    @Override
    public void onCommand(Command c) throws IOException, UnableToExecuteException {
        try {
            myPlant.sendCommandAsync(OWNUtilities.createFrame(c), 1);
        } catch (MalformedCommandOPEN ex) {
            LOG.log(Level.SEVERE, "Malformed OWN frame. " + ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onStop() {
        try {
            myPlant.stopMonitoring();
            this.setDescription("Plugin stopped");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error during plugin stopping for " + ex.getLocalizedMessage(), ex);
        }
    }

    // sends diagnostic frames to syncronize the software with the real system
    private void initSystem() {
        try {
            LOG.log(Level.INFO, "Sending ''{0}'' frame to initialize LIGHTNING", LIGHTNING_DIAGNOSTIC_FRAME);
            OWNFrame.writeAreaLog(OWNUtilities.getDateTime() + " Act:" + "Sending " + LIGHTNING_DIAGNOSTIC_FRAME + " (inizialize LIGHTNING)");
            myPlant.sendCommandAsync(LIGHTNING_DIAGNOSTIC_FRAME, 1);
            LOG.log(Level.INFO, "Sending ''{0}'' frame to initialize AUTOMATIONS", AUTOMATIONS_DIAGNOSTIC_FRAME);
            OWNFrame.writeAreaLog(OWNUtilities.getDateTime() + " Act:" + "Sending " + AUTOMATIONS_DIAGNOSTIC_FRAME + " (inizialize AUTOMATIONS)");
            myPlant.sendCommandAsync(AUTOMATIONS_DIAGNOSTIC_FRAME, 1);
            LOG.log(Level.INFO, "Sending ''{0}'' frame to initialize ALARM", ALARM_DIAGNOSTIC_FRAME);
            OWNFrame.writeAreaLog(OWNUtilities.getDateTime() + " Act:" + "Sending " + ALARM_DIAGNOSTIC_FRAME + " (inizialize ALARM)");
            myPlant.sendCommandAsync(ALARM_DIAGNOSTIC_FRAME, 1);
            LOG.log(Level.INFO, "Sending ''{0}'' frame to initialize POWER MANAGEMENT", POWER_MANAGEMENT_DIAGNOSTIC_FRAME);
            OWNFrame.writeAreaLog(OWNUtilities.getDateTime() + " Act:" + "Sending " + POWER_MANAGEMENT_DIAGNOSTIC_FRAME + " (inizialize POWER MANAGEMENT)");
            myPlant.sendCommandAsync(POWER_MANAGEMENT_DIAGNOSTIC_FRAME, 1);
        } catch (MalformedCommandOPEN ex) {
            LOG.log(Level.SEVERE, "Malformed OWN frame. " + ex.getLocalizedMessage(), ex);
        }
    }
}
