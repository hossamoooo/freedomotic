/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins.devices.webiopi;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.helpers.HttpHelper;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mauro Cicolella
 */
public class WebIOPi extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(WebIOPi.class.getName());
    private HttpHelper httpHelper;
    String url = "http://httpbin.org/ip";

    /**
     *
     */
    public WebIOPi() {
        super("WebIOPi", "/webiopi/webiopi-manifest.xml");
        //This disables loop execution od onRun() method
        setPollingWait(5000); // onRun() executes once.
    }

    @Override
    public void onStart() {

        httpHelper = new HttpHelper();

    }

    @Override
    public void onStop() {

    }

    @Override
    protected void onRun() {
        try {
            String content = httpHelper.retrieveContent(url);
            System.err.println(content);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(WebIOPi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void onCommand(Command c) throws UnableToExecuteException {

    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        //not nothing. This plugins doesn't listen to freedomotic events
    }
}
