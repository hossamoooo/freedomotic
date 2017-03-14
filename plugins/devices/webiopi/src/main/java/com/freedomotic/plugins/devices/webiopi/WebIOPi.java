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
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Mauro Cicolella
 */
public class WebIOPi extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(WebIOPi.class.getName());
    private final String USERNAME = configuration.getStringProperty("username", "webiopi");
    private final String PASSWORD = configuration.getStringProperty("password", "raspberry");
    private final String RASPBERRY_IP = configuration.getStringProperty("raspberry-ip-address", "127.0.0.1");
    private final Integer RASPBERRY_PORT = configuration.getIntProperty("raspberry-ip-port", 8000);

    private HttpHelper httpHelper;

    String url = "{\"ONEWIRE\": 1, \"GPIO\": {\"0\": {\"value\": 1, \"function\": \"IN\"}, \"1\": {\"value\": 1, \"function\": \"IN\"}, \"2\": {\"value\": 1, \"function\": \"ALT0\"}, \"3\": {\"value\": 1, \"function\": \"ALT0\"}, \"4\": {\"value\": 0, \"function\": \"IN\"}, \"5\": {\"value\": 1, \"function\": \"IN\"}, \"6\": {\"value\": 1, \"function\": \"IN\"}, \"7\": {\"value\": 1, \"function\": \"OUT\"}, \"8\": {\"value\": 1, \"function\": \"OUT\"}, \"9\": {\"value\": 0, \"function\": \"ALT0\"}, \"10\": {\"value\": 0, \"function\": \"ALT0\"}, \"11\": {\"value\": 0, \"function\": \"ALT0\"}, \"12\": {\"value\": 0, \"function\": \"IN\"}, \"13\": {\"value\": 0, \"function\": \"IN\"}, \"14\": {\"value\": 1, \"function\": \"ALT5\"}, \"15\": {\"value\": 1, \"function\": \"ALT5\"}, \"16\": {\"value\": 0, \"function\": \"IN\"}, \"17\": {\"value\": 0, \"function\": \"OUT\"}, \"18\": {\"value\": 0, \"function\": \"OUT\"}, \"19\": {\"value\": 0, \"function\": \"IN\"}, \"20\": {\"value\": 0, \"function\": \"IN\"}, \"21\": {\"value\": 0, \"function\": \"IN\"}, \"22\": {\"value\": 0, \"function\": \"IN\"}, \"23\": {\"value\": 0, \"function\": \"IN\"}, \"24\": {\"value\": 0, \"function\": \"IN\"}, \"25\": {\"value\": 0, \"function\": \"IN\"}, \"26\": {\"value\": 0, \"function\": \"IN\"}, \"27\": {\"value\": 0, \"function\": \"IN\"}, \"28\": {\"value\": 0, \"function\": \"IN\"}, \"29\": {\"value\": 1, \"function\": \"IN\"}, \"30\": {\"value\": 0, \"function\": \"IN\"}, \"31\": {\"value\": 0, \"function\": \"IN\"}, \"32\": {\"value\": 1, \"function\": \"ALT3\"}, \"33\": {\"value\": 1, \"function\": \"ALT3\"}, \"34\": {\"value\": 1, \"function\": \"ALT3\"}, \"35\": {\"value\": 1, \"function\": \"ALT3\"}, \"36\": {\"value\": 1, \"function\": \"ALT3\"}, \"37\": {\"value\": 1, \"function\": \"ALT3\"}, \"38\": {\"value\": 1, \"function\": \"ALT3\"}, \"39\": {\"value\": 1, \"function\": \"ALT3\"}, \"40\": {\"value\": 0, \"function\": \"ALT0\"}, \"41\": {\"value\": 1, \"function\": \"ALT0\"}, \"42\": {\"value\": 0, \"function\": \"ALT0\"}, \"43\": {\"value\": 0, \"function\": \"ALT0\"}, \"44\": {\"value\": 1, \"function\": \"ALT1\"}, \"45\": {\"value\": 1, \"function\": \"ALT1\"}, \"46\": {\"value\": 1, \"function\": \"IN\"}, \"47\": {\"value\": 1, \"function\": \"IN\"}, \"48\": {\"value\": 0, \"function\": \"ALT0\"}, \"49\": {\"value\": 1, \"function\": \"ALT0\"}, \"50\": {\"value\": 1, \"function\": \"ALT0\"}, \"51\": {\"value\": 1, \"function\": \"ALT0\"}, \"52\": {\"value\": 1, \"function\": \"ALT0\"}, \"53\": {\"value\": 1, \"function\": \"ALT0\"}}, \"UART\": 1, \"SPI\": 1, \"I2C\": 1} ";

    /**
     *
     */
    public WebIOPi() {
        super("WebIOPi", "/webiopi/webiopi-manifest.xml");
        //This disables loop execution od onRun() method
        setPollingWait(1000); // onRun() executes once.
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

            String content = httpHelper.retrieveContent("http://" + RASPBERRY_IP + ":" + RASPBERRY_PORT + "/*", USERNAME, PASSWORD);
            sendChanges(content);
            //sendChanges(url);
            LOG.info("Data received from Raspberry Pi \"" + content + "\"");
        } catch (IOException ex) {
            LOG.error(ex.getLocalizedMessage());
        }
    }

    private void sendChanges(String content) {

        JSONObject obj = new JSONObject(content);
        if (obj != null) {
            JSONObject gpio = obj.getJSONObject("GPIO");
            for (int i = 0; i < 53; i++) {
                JSONObject current = gpio.getJSONObject(String.valueOf(i));
                System.out.println("GPIO " + i + " function: " + current.getString("function") + " value: " + current.getInt("value"));
            }
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
