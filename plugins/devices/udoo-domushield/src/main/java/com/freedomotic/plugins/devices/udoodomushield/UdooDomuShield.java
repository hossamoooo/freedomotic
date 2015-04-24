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

/**
 *
 * @author Mauro Cicolella <mcicolella@libero.it>
 */
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
    private String dataDelimiter = configuration.getStringProperty("data-delimiter", ",");
    private String valueDelimiter = configuration.getStringProperty("value-delimiter", "#");
    private SerialHelper serial;
    private String storedHumidity = "";
    private String storedLuminosity = "";
    private String storedTemperature = "";
    String[] receivedMessage = null;
    String[] currentObject = null;
    String readValue = null;
    HashMap<Integer, EkironjObject> objects = null;
    ProtocolRead event = null;
    /**
     * These values are use to encode and decode an easy custom protocol
     *
     * |__-__+__ __|__ __ __ __|__ __ __ __|__ __ __ __| (4 bytes) 31 23 15 8 0
     */
    // masks
    public final static int OP_CODE_MASK = 0xf0000000;
    public final static int ID_CODE_MASK = 0x0f000000;
    public final static int MAIN_OP_CODE_MASK = 0xc0000000;
    public final static int SUB_OP_CODE_MASK = 0x30000000;
    public final static int CMD_MASK = 0xff000000;
    public final static int RED_MASK = 0x00ff0000;
    public final static int GREEN_MASK = 0x0000ff00;
    public final static int BLUE_MASK = 0x000000ff;
    public final static int PAYLOAD_MASK = 0x00ffffff;
    // offsets
    public final static int MAIN_OP_OFFSET = 30;
    public final static int SUB_OP_OFFSET = 28;
    public final static int OP_OFFSET = 28;
    public final static int ID_OFFSET = 24;
    public final static int CMD_OFFSET = 24;
    public final static int RED_OFFSET = 16;
    public final static int GREEN_OFFSET = 8;
    public final static int BLUE_OFFSET = 0;
    // MAIN op_codes
    public final static int REQUEST_MSG = 0x0;
    public final static int RELAY_MSG = 0x1;
    public final static int STRIP_MSG = 0x2;
    public final static int VIDEO_MSG = 0x3;
    // SUB op_codes
    public final static int REQUEST_IP_DISCOVERY_MSG = 0x0;
    public final static int REQUEST_SERVICE_LIST_MSG = 0x1;
    public final static int REQUEST_GENERIC_MSG = 0x2;
    public final static int STRIP_DIRECT_MSG = 0x0;
    public final static int STRIP_FADE_MSG = 0x1;
    public final static int STRIP_BLINK_MSG = 0x2;
    public final static int STRIP_RAINBOW_MSG = 0x3;
    public final static int RELAY_OFF_MSG = 0x0;
    public final static int RELAY_ON_MSG = 0x1;
    public final static int RELAY_CHANGE_MSG = 0x2;
    public final static int VIDEO_GET_LIST_MSG = 0x0;
    public final static int VIDEO_PLAY_MSG = 0x1;
    public final static int VIDEO_PLAY_LOOP_MSG = 0x2;
    public final static int VIDEO_PLAY_EXTRA_MSG = 0x3; // si usano i bit dell id per ulteriori comandi

    public UdooDomuShield() {
        super("UDOO DomuShield", "/udoo-domushield/udoo-domushield-manifest.xml");
        //This disables loop execution od onRun() method
        setPollingWait(-1); // onRun() executes once.
    }

    @Override
    public void onStart() throws PluginStartupException {
        try {
            serial = new SerialHelper(portName, baudRate, dataBits, stopBits, parity, new SerialPortListener() {

                @Override
                public void onDataAvailable(String data) {
                    LOG.log(Level.CONFIG, "UDOO DomuShield received: {0}", data);
                    sendChanges(data);
                }
            });
            // in this example it reads until a string terminator (default: new line char)
            serial.setChunkTerminator(chunkTerminator);

            initialize();
        } catch (SerialPortException ex) {
            throw new PluginStartupException("Error while creating UDOO DomuShield serial connection. " + ex.getMessage(), ex);
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
        //nothing to do, UDOO DomuShield messages are read by SerialHelper
    }

    @Override
    protected void onCommand(Command c) throws UnableToExecuteException {

        int strip = 1;
        int msg = 0;
        //this method receives freedomotic commands sent on channel app.actuators.protocol.udoo-domushield.in
        String command = c.getProperty("udoo-domushield.command");
        String idCode = c.getProperty("udoo-domushield.idCode");
        idCode = idCode.substring(idCode.length() - 1, idCode.length());
        switch (command) {
            case "TURN_OFF_RELAY":
                msg = pack(RELAY_MSG, RELAY_OFF_MSG, Integer.valueOf(c.getProperty(idCode)), null, null, 0);
                break;
        }
        try {
            serial.write(intToByteArray(msg));
        } catch (SerialPortException ex) {
            //    throw new UnableToExecuteException("Error writing message '" + message + "' to UDOO DomuShield shield: " + ex.getMessage(), ex);
        }
    }

    private void initialize() {

        EkironjObject obj = new EkironjObject("humSensor", "sensor", "Hygrometer", "", "H");
        objects.put(0, obj);
        obj = new EkironjObject("lumSensor", "sensor", "Light Sensor", "", "L");
        objects.put(1, obj);
        obj = new EkironjObject("tempSensor", "sensor", "Thermometer", "", "T");
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
            currentObject = receivedMessage[i].split(valueDelimiter);
            readValue = currentObject[i];
            if (isChangedValue(i, readValue)) {
                event = new ProtocolRead(this, "udoo-domushield", objects.get(i).getAddress());
                //         if (receivedStatus.equalsIgnoreCase("on")) {
                //             event.addProperty("isOn", "true");
                //         } else {
                //             event.addProperty("isOn", "false");
                //       }
                event.addProperty("read.value", readValue);
                event.addProperty("object.class", objects.get(i).getFreedomoticClass());
                event.addProperty("object.name", "Udoo " + objects.get(i).getName());
                event.addProperty("object.addres", objects.get(i).getAddress());
                objects.get(i).setStoredValue(readValue);
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

    /**
     * Checks if a value has changed or not avoiding to send unuseful events
     *
     * @param objectIndex
     * @param readValue
     * @return boolean
     *
     */
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

    // This method pack the bits into an integer before send it over udp
    private int pack(Integer mainOpCode, Integer subOpCode, Integer idCode,
            Integer r, Integer g, Integer b) {
        int msg = 0;

        msg |= (mainOpCode << MAIN_OP_OFFSET);
        msg |= (subOpCode << SUB_OP_OFFSET);

        if (idCode != null) {
            msg |= (idCode << ID_OFFSET);
        }

        if (r != null) {
            msg |= (r << RED_OFFSET);
        }

        if (g != null) {
            msg |= (g << GREEN_OFFSET);
        }

        if (b != null) {
            msg |= (b << BLUE_OFFSET);
        }

        return msg;
    }
}
