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
import java.awt.Color;
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
    private SerialHelper serial;
    private AuthometionGui gui;

    public Authometion() {
        super("Authometion", "/authometion/authometion-manifest.xml");
        //This disables loop execution od onRun() method
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
                    //sendChanges(data);
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
        //nothing to do, Arduino messages are read by SerialHelper
    }

    @Override
    protected void onCommand(Command c) throws UnableToExecuteException {
        //this method receives freedomotic commands sent on channel app.actuators.protocol.authometion.in
        String message = "";

        switch (c.getProperty("authometion.command")) {
            case "SBR":
                message = c.getProperty("authometion.command");
                message += delimiter + c.getProperty("address");
                int brightness = Integer.valueOf(c.getProperty("brightness"));
                message += delimiter + (int) Math.ceil((brightness * 255) / 100);
                break;

            case "HSB":
                message = c.getProperty("authometion.command");
                message += delimiter + c.getProperty("address");
                String[] rgbValues = hsbToRgb(Float.valueOf(c.getProperty("hue")), Float.valueOf(c.getProperty("saturation")) / 100, Float.valueOf(c.getProperty("brightness")) / 100);
                message += delimiter + rgbValues[0] + delimiter + rgbValues[1] + delimiter + rgbValues[2];
                break;
        }

        //System.out.println("AUTHOMETION "+message);  
        writeToSerial(message);

    }

    private void sendChanges(String data) {
        // in this example we are using Arduino Serial.println() so
        // remove '\r' and '\n' at the end of the string and split data read
        String[] receivedMessage = data.substring(0, data.length() - 2).split(delimiter);
        String receivedAddress = receivedMessage[0];
        String receivedStatus = receivedMessage[1];

        ProtocolRead event = new ProtocolRead(this, "authometion", receivedAddress);
        if (receivedStatus.equalsIgnoreCase("on")) {
            event.addProperty("isOn", "true");
        } else {
            event.addProperty("isOn", "false");
        }
        this.notifyEvent(event);
    }

    private String[] hsbToRgb(float hue, float saturation, float brightness) {

        String[] rgbValues;

        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        rgbValues = new String[3];
        rgbValues[0] = String.valueOf(red);
        rgbValues[1] = String.valueOf(green);
        rgbValues[2] = String.valueOf(blue);

        return rgbValues;
    }
    

    private String[] rgbToHsb(int red, int green, int blue) {

        String[] hsb = new String[3];

        float[] hsbValues = Color.RGBtoHSB(red, green, blue, null);
        hsb[0] = String.valueOf(hsbValues[0]);
        hsb[1] = String.valueOf(hsbValues[1]);
        hsb[2] = String.valueOf(hsbValues[2]);
        return hsb;

    }

    public void writeToSerial(String message) throws UnableToExecuteException {
        try {
            serial.write(message + "\r");
        } catch (SerialPortException ex) {
            throw new UnableToExecuteException("Error writing message '" + message + "' to arduino serial board: " + ex.getMessage(), ex);
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
