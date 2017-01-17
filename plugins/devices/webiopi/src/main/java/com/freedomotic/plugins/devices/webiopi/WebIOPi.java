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
    private HttpHelper httpHelper;
    String url = "https://private-anon-857d7cc727-zwayhomeautomation.apiary-mock.com/ZAutomation/api/v1/devices";

    String url2 = "{\"UART0\": 1, \"I2C0\": 0, \"I2C1\": 1, \"SPI0\": 0, \"GPIO\":{\n"
            + "\"0\": {\"function\": \"IN\", \"value\": 1}, \n"
            + "\"1\": {\"function\": \"IN\", \"value\": 1}, \n"
            + "\"2\": {\"function\": \"ALT0\", \"value\": 1}, \n"
            + "\"3\": {\"function\": \"ALT0\", \"value\": 1}, \n"
            + "\"4\": {\"function\": \"IN\", \"value\": 0}, \n"
            + "\"5\": {\"function\": \"ALT0\", \"value\": 0}, \n"
            + "\"6\": {\"function\": \"OUT\", \"value\": 1}, \n"
            + "\"53\": {\"function\": \"ALT3\", \"value\": 1}\n"
            + "}}";

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
        //try {
        //String content = httpHelper.retrieveContent(url);
        //sendChanges(content);
        sendChanges(url2);
        //} catch (IOException ex) {
        //    java.util.logging.Logger.getLogger(WebIOPi.class.getName()).log(Level.SEVERE, null, ex);
        //}
    }

    private void sendChanges(String content) {

        JSONObject obj = new JSONObject(content);
        if (obj != null) {
            JSONObject gpio = obj.getJSONObject("GPIO");
            JSONObject current = gpio.getJSONObject("0");
            System.out.println(current.getString("function"));
            System.out.println(current.getInt("value"));   
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
