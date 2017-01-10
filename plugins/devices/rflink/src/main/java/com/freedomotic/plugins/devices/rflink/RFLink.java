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
package com.freedomotic.plugins.devices.rflink;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.helpers.SerialHelper;
import com.freedomotic.helpers.SerialPortListener;
import com.freedomotic.reactions.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jssc.SerialPortException;

/**
 *
 * @author Mauro Cicolella
 */
public class RFLink extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(RFLink.class.getName());
    private String portName = configuration.getStringProperty("serial.port", "/dev/usb0");
    private Integer baudRate = configuration.getIntProperty("serial.baudrate", 57600);
    private Integer dataBits = configuration.getIntProperty("serial.databits", 8);
    private Integer parity = configuration.getIntProperty("serial.parity", 0);
    private Integer stopBits = configuration.getIntProperty("serial.stopbits", 1);
    private String chunkTerminator = configuration.getStringProperty("chunk.terminator", "\n");
    private String delimiter = configuration.getStringProperty("delimiter", ";");
    private SerialHelper serial;

    /**
     *
     */
    public RFLink() {
        super("RFLink Gateway", "/rflink/rflink-manifest.xml");
        //This disables loop execution od onRun() method
        setPollingWait(-1); // onRun() executes once.
    }

    @Override
    public void onStart() throws PluginStartupException {
        try {
            serial = new SerialHelper(portName, baudRate, dataBits, stopBits, parity, new SerialPortListener() {
                @Override
                public void onDataAvailable(String data) {
                    LOG.info("RFLink gateway received: ''{}''", data);
                    sendChanges(data);
                }
            });
            // in this example it reads until a string terminator (default: new line char)
            serial.setChunkTerminator(chunkTerminator);
        } catch (SerialPortException ex) {
            throw new PluginStartupException("Error while creating RFLink gateway serial connection for " + ex.getMessage(), ex);
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
        //nothing to do, RFLink messages are read by SerialHelper
    }

    @Override
    protected void onCommand(Command c) throws UnableToExecuteException {
        //this method receives freedomotic commands sent on channel app.actuators.protocol.rflink.in
        String message = c.getProperty("arduinousb.message");
        try {
            serial.write(message);
        } catch (SerialPortException ex) {
            throw new UnableToExecuteException("Error writing message '" + message + "'' to RFLink gateway serial board: " + ex.getMessage(), ex);
        }
    }

    /**
     *
     *
     * @param data
     */
    private void sendChanges(String data) {
        String[] receivedMessage = data.substring(0, data.length() - 2).split(delimiter);
        String nodeNumber = receivedMessage[0];
        String deviceName = receivedMessage[2];
        String deviceID = receivedMessage[3].substring(3, receivedMessage[3].length());
        ProtocolRead event;
        String convertedValue;

        if (receivedMessage.length >= 5) {

            for (int i = 4; i < receivedMessage.length; i++) {

                event = new ProtocolRead(this, "rflink", deviceID);
                event.getPayload().addStatement("rflink.node-number", nodeNumber);
                event.getPayload().addStatement("rflink.node-name", deviceName);
                String[] payload = receivedMessage[i].split("=");
                convertedValue = valuesConvertion(payload[0], payload[1]);
                event.getPayload().addStatement("rflink.sensor-type", payload[0]);
                event.getPayload().addStatement("rflink.sensor-value", convertedValue);
                System.out.println(event.getPayload().getStatements());
                notifyEvent(event);
            }
        }
    }

    /**
     * 
     * 
     * @param sensorType
     * @param value
     * @return 
     */
    private String valuesConvertion(String sensorType, String value) {

        String convertedValue;

        switch (sensorType) {

            case "TEMP":
                convertedValue = String.valueOf(Integer.parseInt(value,16));

                return convertedValue;


            default:
                return value;
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
