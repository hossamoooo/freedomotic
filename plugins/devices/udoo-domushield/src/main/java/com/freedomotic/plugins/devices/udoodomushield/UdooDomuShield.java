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
package com.freedomotic.plugins.devices.udoodomushield;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.helpers.SerialHelper;
import com.freedomotic.helpers.SerialPortListener;
import com.freedomotic.reactions.Command;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPortException;

public class UdooDomuShield extends Protocol {

    private static final Logger LOG = Logger.getLogger(UdooDomuShield.class.getName());
    private String portName = configuration.getStringProperty("serial.port", "/dev/usb0");
    private Integer baudRate = configuration.getIntProperty("serial.baudrate", 9600);
    private Integer dataBits = configuration.getIntProperty("serial.databits", 8);
    private Integer parity = configuration.getIntProperty("serial.parity", 0);
    private Integer stopBits = configuration.getIntProperty("serial.stopbits", 1);
    private String chunkTerminator = configuration.getStringProperty("chunk.terminator", "\n");
    private String dataDelimiter = configuration.getStringProperty("data-delimiter", ";");
    private SerialHelper serial;
    private String storedHumidity = "";
    private String storedLuminosity = "";
    private String storedTemperature = "";
    private static EkironjiDevice mDevice = null;
    private boolean isAutoLightOn = false;
    private int autoLightTh = 10;
    String[] receivedMessage = null;
    String[] currentObject = null;
    String readValue = null;
    HashMap<Integer, EkironjObject> objects = null;
    ProtocolRead event = null;

    public UdooDomuShield() {
        super("Udoo DomuShield", "/udoo-domushield/udoo-domushield-manifest.xml");
        //This disables loop execution od onRun() method
        setPollingWait(-1); // onRun() executes once.
    }

    @Override
    public void onStart() throws PluginStartupException {
        try {
            mDevice = new EkironjiDevice(null);
            serial = new SerialHelper(portName, baudRate, dataBits, stopBits, parity, new SerialPortListener() {

                @Override
                public void onDataAvailable(String data) {
                    LOG.log(Level.CONFIG, "Udoo DomuShield received: {0}", data);
                    sendChanges(data);
                }
            });
            // in this example it reads until a string terminator (default: new line char)
            serial.setChunkTerminator(chunkTerminator);
        } catch (SerialPortException ex) {
            throw new PluginStartupException("Error while creating Arduino serial connection. " + ex.getMessage(), ex);
        }
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
        //this method receives freedomotic commands sent on channel app.actuators.protocol.arduinousb.in
        String message = c.getProperty("udoo-domushield.command");
        try {
            serial.write(message);
        } catch (SerialPortException ex) {
            throw new UnableToExecuteException("Error writing message '" + message + "' to arduino serial board: " + ex.getMessage(), ex);
        }
    }

    private void inizialize() {

        EkironjObject obj = new EkironjObject("humSensor", "sensor", "Hygrometer", "", "H");
        objects.put(0, obj);
        obj = new EkironjObject("lumSensor", "sensor", "Light Sensor", "", "L");
        objects.put(1, obj);
        obj = new EkironjObject("tempSensor", "sensor", "Thermometer", "", "L");
        objects.put(2, obj);
        obj = new EkironjObject("relay1", "actuator", "Light", "", "R1");
        objects.put(3, obj);
        obj = new EkironjObject("relay2", "actuator", "Light", "", "R2");
        objects.put(4, obj);
    }

    private void sendChanges(String data) {

        // remove '\r' and '\n' at the end of the string and split data read
        receivedMessage = data.substring(0, data.length() - 2).split(dataDelimiter);

        for (int i = 0; i < receivedMessage.length; i++) {
            currentObject = receivedMessage[i].split("#");
            readValue = currentObject[1];
            if (isChangedValue(i, readValue)) {
                event = new ProtocolRead(this, "udoo-domushield", objects.get(i).getAddress());
                //         if (receivedStatus.equalsIgnoreCase("on")) {
                //             event.addProperty("isOn", "true");
                //         } else {
                //             event.addProperty("isOn", "false");
                //       }
                event.addProperty("read.value", readValue);
                this.notifyEvent(event);
            }
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

    private boolean isChangedValue(int objectIndex, String readValue) {

        if (objects.get(objectIndex).getStoredValue().equalsIgnoreCase(readValue)) {
            return true;
        } else {
            return false;
        }
    }

    private byte[] intToByteArray(int integer) {
        byte[] array = new byte[4];

        array[0] = (byte) ((integer >> 24) & 0x000000ff);
        array[1] = (byte) ((integer >> 16) & 0x000000ff);
        array[2] = (byte) ((integer >> 8) & 0x000000ff);
        array[3] = (byte) ((integer >> 0) & 0x000000ff);

        return array;
    }

    private String intToBits(int integer) {
        String s = "";

        for (int i = 31; i >= 0; i--) {
            if (i % 8 == 7) {
                s += " ";
            }

            if (((integer >>> i) & 0x1) == 0) {
                s += "0";
            } else {
                s += 1;
            }
        }

        return s;
    }
}
