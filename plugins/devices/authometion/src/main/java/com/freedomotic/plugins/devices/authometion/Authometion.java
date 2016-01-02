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
package com.freedomotic.plugins.devices.authometion;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.helpers.SerialHelper;
import com.freedomotic.helpers.SerialPortListener;
import com.freedomotic.reactions.Command;
import com.freedomotic.things.EnvObjectLogic;
import java.awt.Color;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPortException;

public class Authometion extends Protocol {

    private static final Logger LOG = Logger.getLogger(Authometion.class.getName());
    private String portName = configuration.getStringProperty("serial.port", "/dev/usb0");
    private Integer baudRate = configuration.getIntProperty("serial.baudrate", 9600);
    private Integer dataBits = configuration.getIntProperty("serial.databits", 8);
    private Integer parity = configuration.getIntProperty("serial.parity", 0);
    private Integer stopBits = configuration.getIntProperty("serial.stopbits", 1);
    private String chunkTerminator = configuration.getStringProperty("chunk.terminator", "\n");
    private String delimiter = configuration.getStringProperty("delimiter", ";");
    private String save_lights_configuration = configuration.getStringProperty("save-lights-configuration", "false");
    private SerialHelper serial;
    private AuthometionGui gui;

    public Authometion() {
        super("Authometion", "/authometion/authometion-manifest.xml");
        setPollingWait(-1); // onRun() executes once.
    }

    @Override
    public void onStart() throws PluginStartupException {
        gui = new AuthometionGui(this);
        try {
            serial = new SerialHelper(portName, baudRate, dataBits, stopBits, parity, new SerialPortListener() {
                @Override
                public void onDataAvailable(String data) {
                    LOG.log(Level.CONFIG, "Authometion received: {0}", data);
                    sendChanges(data);
                }
            });
            // in this example it reads until a string terminator (default: new line char)
            serial.setChunkTerminator(chunkTerminator);
        } catch (SerialPortException ex) {
            throw new PluginStartupException("Error while creating Authometion serial connection. " + ex.getMessage(), ex);
        }
    }

    @Override
    protected void onShowGui() {
        bindGuiToPlugin(gui);
    }

    @Override
    public void onStop() {
        if (serial != null) {
            serial.disconnect();
        }
    }

    @Override
    protected void onRun() {
        try {
            initializeThings();
        } catch (UnableToExecuteException ex) {
            LOG.log(Level.CONFIG, ex.getLocalizedMessage());
        }
    }

    @Override
    protected void onCommand(Command c) throws UnableToExecuteException {
        //this method receives freedomotic commands sent on channel app.actuators.protocol.authometion.in
        String message = "";
        Random random = new Random();
        final int maxRGB = 255;
        int red, green, blue;

        switch (c.getProperty("authometion.command")) {
            case "SBR":
                if (!c.getProperty("white").equalsIgnoreCase("0")) {
                    message = c.getProperty("authometion.command");
                    message += delimiter + c.getProperty("address");
                    message += delimiter + c.getProperty("white");
                }
                break;

            case "RGB":
                if (isValidRGBValue(c.getProperty("red"), c.getProperty("green"), c.getProperty("blue"))) {
                    message = c.getProperty("authometion.command");
                    message += delimiter + c.getProperty("address");
                    message += delimiter + c.getProperty("red") + delimiter + c.getProperty("green") + delimiter + c.getProperty("blue");
                }
                break;

            case "RND":
                red = random.nextInt(maxRGB);
                green = random.nextInt(maxRGB);
                blue = random.nextInt(maxRGB);
                if (isValidRGBValue(String.valueOf(red), String.valueOf(green), String.valueOf(blue))) {
                    message = "RGB";
                    message += delimiter + c.getProperty("address");
                    message += delimiter + String.valueOf(red) + delimiter + String.valueOf(green) + delimiter + String.valueOf(blue);
                }
                break;

            default:
                message = c.getProperty("authometion.command");
                message += delimiter + c.getProperty("address");
                break;
        }

        if (!message.equalsIgnoreCase("")) {
            System.out.println("Message to write " + message);
            writeToSerial(message);
        }

        // if save configuration enabled execute SAV,<object-address>
        if (save_lights_configuration.equalsIgnoreCase("true")) {
            writeToSerial("SAV," + c.getProperty("address"));
        }
    }

    private void sendChanges(String data) {

        if (data.startsWith("STATUS:")) {
            String[] payload = data.substring(7, data.length() - 1).split(" ");
            ProtocolRead event = new ProtocolRead(this, "authometion", payload[0]);
            if (payload[1].equalsIgnoreCase("1")) {
                event.addProperty("isOn", "true");
            } else {
                event.addProperty("isOn", "false");
            }
            event.addProperty("rgb.red", payload[2]);
            event.addProperty("rgb.green", payload[3]);
            event.addProperty("rgb.blue", payload[4]);
            event.addProperty("white", payload[5]);
            event.addProperty("rssi", payload[6]);
            System.out.println("Event: " + event.getPayload().getStatements());
            notifyEvent(event);
        }
    }

    public void writeToSerial(String message) throws UnableToExecuteException {
        try {
            serial.write(message + "\r");
        } catch (SerialPortException ex) {
            throw new UnableToExecuteException("Error writing message '" + message + "' to arduino serial board: " + ex.getMessage(), ex);
        }
    }

    private void initializeThings() throws UnableToExecuteException {
        for (EnvObjectLogic object : getApi().things().findByProtocol("authometion")) {
            System.out.println("Found: " + object.getPojo().getName()+"\n");
            writeToSerial("RIS" + delimiter + object.getPojo().getPhisicalAddress());
        }
    }

    private boolean isValidRGBValue(String red, String green, String blue) {
        if (!(red.equalsIgnoreCase("0") && green.equalsIgnoreCase("0") && blue.equalsIgnoreCase("0"))) {
            return true;
        } else {
            return false;
        }
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
