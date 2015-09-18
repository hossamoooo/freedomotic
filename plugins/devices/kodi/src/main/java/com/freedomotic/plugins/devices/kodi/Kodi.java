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
package com.freedomotic.plugins.devices.kodi;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Kodi extends Protocol {

    final int POLLING_WAIT;
    private static final Logger LOG = Logger.getLogger(Kodi.class.getName());
    List<KodiSystem> systemList = new ArrayList<KodiSystem>();

    public Kodi() {
        super("Kodi", "/kodi/kodi-manifest.xml");
        POLLING_WAIT = configuration.getIntProperty("time-between-reads", 2000); // Not sure if needed?
        setPollingWait(POLLING_WAIT); 

    }

    @Override
    protected void onStart() {

        String thisHost;
        Integer thisPort;
        Thread thisThread;

        loadKodiSystems();

        for (KodiSystem thisKodiSystem : systemList) {
            thisThread = new Thread(new KodiThread(thisKodiSystem), "KodiPluginThread" + systemList.indexOf(thisKodiSystem));
            thisKodiSystem.setKodiThread(thisThread);
            thisKodiSystem.getKodiThread().start();
        }

        LOG.info("Kodi plugin started");
    }

    @Override
    protected void onShowGui() {
        //bindGuiToPlugin(new Kodi(this));
    }

    @Override
    protected void onHideGui() {
        setDescription("My GUI is now hidden");
    }

    @Override
    protected void onRun() {

        String thisHost;
        String thisState;
        Thread thisThread;

        for (KodiSystem thisKodiSystem : systemList) {
            thisHost = thisKodiSystem.getKodiHost();
            thisState = thisKodiSystem.getKodiThread().getState().toString();
            LOG.info("Kodi Host : " + thisHost + " State : " + thisState);
        }

    }

    @Override
    protected void onStop() {
        LOG.info("Kodi plugin is stopped ");
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        LOG.info("Kodi plugin receives a command called " + c.getName()
                + " with parameters " + c.getProperties().toString());
    }

    @Override
    protected boolean canExecute(Command c) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void loadKodiSystems() {
        Integer counter;
        String KodiName;
        String KodiHost;
        Integer KodiPort;
        String KodiLocation;
        String result;
        Integer numberOfSystems;

        numberOfSystems = configuration.getTuples().size();

        for (counter = 0; counter < numberOfSystems; counter++) {
            result = configuration.getTuples().getProperty(counter, "System");
            if (result != null) {
                KodiHost = configuration.getTuples().getStringProperty(counter, "KodiHost", "localhost");
                KodiPort = configuration.getTuples().getIntProperty(counter, "KodiPort", 9090);
                KodiName = configuration.getTuples().getStringProperty(counter, "System", "none");
                KodiLocation = configuration.getTuples().getStringProperty(counter, "Location", "none");
                KodiSystem KodiSystem = new KodiSystem(KodiName, KodiHost, KodiPort, KodiLocation);
                systemList.add(KodiSystem);
            }
        }
    }
}
