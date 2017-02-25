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
package com.freedomotic.plugins.devices.authometionlyt8266;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.helpers.UdpHelper;
import com.freedomotic.helpers.UdpListener;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mauro Cicolella
 */
public class AuthometionLYT8266
        extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(AuthometionLYT8266.class.getName());

    private String BROADCAST_ADDRESS;
    private Integer SEND_COMMAND_UDP_PORT;
    private Integer RECEIVE_REPLY_UDP_PORT;
    private UdpHelper udpServer;

    /**
     *
     */
    public AuthometionLYT8266() {
        super("Authometion LYT8266", "/authometion-lyt8266/authometion-lyt8266-manifest.xml");
        BROADCAST_ADDRESS = configuration.getStringProperty("broadcast-address", "192.168.0.255");
        SEND_COMMAND_UDP_PORT = configuration.getIntProperty("send-command-udp-port", 8899);
        RECEIVE_REPLY_UDP_PORT = configuration.getIntProperty("receive-reply-udp-port", 48899);
        setPollingWait(-1); // polling disabled
    }

    @Override
    protected void onShowGui() {
    }

    @Override
    protected void onHideGui() {

    }

    @Override
    protected void onRun() {

    }

    @Override
    protected void onStart() {
        udpServer = new UdpHelper();
        udpServer.startServer("0.0.0.0", RECEIVE_REPLY_UDP_PORT, new UdpListener() {
            @Override
            public void onDataAvailable(String sourceAddress, Integer sourcePort, String data) {
                LOG.info("Authometion LYT8266 received: \"{}\"", data);
                try {
                    sendChanges(data);
                } catch (IOException ex) {
                    LOG.error("Impossible to send the command ", ex);
                }
            }
        });
        try {
            // broadcast list request
            udpServer.send(BROADCAST_ADDRESS, SEND_COMMAND_UDP_PORT, "+255\r\n");
        } catch (IOException ex) {
            LOG.error("Impossible to send the command ", ex);
        }
        LOG.info("Authometion LYT8266 plugin started");
    }

    @Override
    protected void onStop() {
        // stop udp server
        LOG.info("Authometion LYT8266 stopped");
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {

        String commandToSend = "";
        String whiteMode = "";
        String powered = "";

        if (c.getProperty("white-mode").equalsIgnoreCase("true")) {
            whiteMode = "ON";
        } else {
            whiteMode = "OFF";
        }

        if (c.getProperty("powered").equalsIgnoreCase("true")) {
            powered = "ON";
        } else {
            powered = "OFF";
        }

        switch (c.getProperty("authometion.command-name")) {

            case "LYT_BRIGHTNESS":
                if (c.getProperty("white-mode").equalsIgnoreCase("true")) {
                    commandToSend = "+" + c.getProperty("authometion.command-code") + "," + c.getProperty("red") + "," + c.getProperty("green") + "," + c.getProperty("blue") + "," + c.getProperty("brightness") + ",ON, ON," + c.getProperty("address") + "\r\n";
                } else {
                    commandToSend = "+" + c.getProperty("authometion.command-code") + "," + c.getProperty("red") + "," + c.getProperty("green") + "," + c.getProperty("blue") + "," + c.getProperty("brightness") + ",OFF, ON," + c.getProperty("address") + "\r\n";
                }
                break;

            case "SET_WHITE_MODE":
                commandToSend = "+" + c.getProperty("authometion.command-code") + "," + c.getProperty("red") + "," + c.getProperty("green") + "," + c.getProperty("blue") + "," + c.getProperty("brightness") + "," + whiteMode + "," + powered + "," + c.getProperty("address") + "\r\n";
                LOG.info("Set white mode " + commandToSend);
                break;

            case "LYT_RGB":
                if (c.getProperty("white-mode").equalsIgnoreCase("false") && isValidRGBValue(c.getProperty("red"), c.getProperty("green"), c.getProperty("blue"))) {
                    commandToSend = "+" + c.getProperty("authometion.command-code") + "," + c.getProperty("red") + "," + c.getProperty("green") + "," + c.getProperty("blue") + "," + c.getProperty("brightness") + "," + whiteMode + "," + powered + "," + c.getProperty("address") + "\r\n";
                }
                break;

            case "LYT_ON":
                commandToSend = "+" + c.getProperty("authometion.command-code") + "," + c.getProperty("red") + "," + c.getProperty("green") + "," + c.getProperty("blue") + "," + c.getProperty("brightness") + "," + whiteMode + "," + c.getProperty("powered") + "," + c.getProperty("address") + "\r\n";
                break;

            case "LYT_OFF":
                commandToSend = "+" + c.getProperty("authometion.command-code") + "," + c.getProperty("red") + "," + c.getProperty("green") + "," + c.getProperty("blue") + "," + c.getProperty("brightness") + "," + whiteMode + "," + c.getProperty("powered") + "," + c.getProperty("address") + "\r\n";
                break;
        }
        // sends the command
        udpServer.send(BROADCAST_ADDRESS, SEND_COMMAND_UDP_PORT, commandToSend);
        LOG.info("Sending command \"{}\"", commandToSend);

        // status request
        udpServer.send(BROADCAST_ADDRESS, SEND_COMMAND_UDP_PORT,
                "+15=" + c.getProperty("address") + "?\r\n");
        LOG.info("Sending status request \"{}\"", "+15=" + c.getProperty("address") + "?\r\n");
    }

    /**
     *
     * @param data
     * @throws IOException
     */
    private void sendChanges(String data) throws IOException {

        ProtocolRead event;
        String[] payload;
        String address;
        String brightness;
        String whiteMode;
        String power;
        String red;
        String green;
        String blue;

        if (data.startsWith("+15=")) {
            payload = data.substring(4, data.length() - 2).split(",");

            red = payload[0];
            green = payload[1];
            blue = payload[2];
            brightness = payload[3];
            whiteMode = payload[4];
            power = payload[5];
            address = payload[6];

            event = new ProtocolRead(this, "authometion-lyt8266", address);
            event.addProperty("brightness", brightness);
            if (whiteMode.equalsIgnoreCase("ON")) {
                event.addProperty("white-mode", "true");
            } else {
                event.addProperty("white-mode", "false");
            }
            event.addProperty("rgb.red", red);
            event.addProperty("rgb.green", green);
            event.addProperty("rgb.blue", blue);
            if (power.equalsIgnoreCase("ON")) {
                event.addProperty("isOn", "true");
            } else {
                event.addProperty("isOn", "false");
            }
            event.addProperty("object.name", "LYT8266 " + address);
            event.addProperty("object.class", "Authometion LYT8266 Light");
            notifyEvent(event);
        }

        // sends a status request - only for initialization
        if (data.startsWith("+255=")) {
            payload = data.substring(5, data.length() - 2).split(",");
            udpServer.send(BROADCAST_ADDRESS, SEND_COMMAND_UDP_PORT, "+15=" + payload[1] + "?\r\n");
        }

    }

    /**
     *
     * @param red
     * @param green
     * @param blue
     * @return
     */
    private boolean isValidRGBValue(String red, String green, String blue) {
        if (!(red.equalsIgnoreCase("0") && green.equalsIgnoreCase("0") && blue.equalsIgnoreCase("0"))) {
            return true;
        } else {
            return false;
        }
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
}
