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

/**
 *
 * @author Mauro Cicolella
 */
public class Board {

    private String raspberryPiAddress;
    private String username;
    private String password;
    private String alias;
    private Integer raspberryPiPort;
    private Integer raspberryPiNumberOfPins;
    private GpioPin[] gpioPins;

    Board(String alias, String raspberryPiAddress, Integer raspberryPiPort, Integer raspberryPiNumberOfPins, String username, String password) {

        setRaspberryPiAlias(alias);
        setRaspberryPiAddress(raspberryPiAddress);
        setRaspberryPiPort(raspberryPiPort);
        setRaspberryPiUsername(username);
        setRaspberryPiPassword(password);
        setRaspberryPiNumberOfPins(raspberryPiNumberOfPins);
        initializePins(raspberryPiNumberOfPins);

    }

    void setRaspberryPiAlias(String alias) {
        this.raspberryPiAddress = alias;
    }

    void setRaspberryPiAddress(String raspberryPiAddress) {
        this.raspberryPiAddress = raspberryPiAddress;
    }

    void setRaspberryPiPort(Integer raspberryPiPort) {
        this.raspberryPiPort = raspberryPiPort;
    }

    void setRaspberryPiNumberOfPins(Integer raspberryPiNumberOfPins) {
        this.raspberryPiNumberOfPins = raspberryPiNumberOfPins;
    }

    void setRaspberryPiUsername(String username) {
        this.username = username;
    }

    void setRaspberryPiPassword(String password) {
        this.password = password;
    }

    String getRaspberryPiAddress() {
        return this.raspberryPiAddress;
    }

    Integer getRaspberryPiPort() {
        return this.raspberryPiPort;
    }

    Integer getRaspberryPiNumberOfPins() {
        return this.raspberryPiNumberOfPins;
    }

    String getRaspberryPiUsername() {
        return this.username;
    }

    String getRaspberryPiPassword() {
        return this.password;
    }

    void initializePins(Integer numberOfPins) {
        gpioPins = new GpioPin[numberOfPins];
        for (int i = 0; i < numberOfPins; i++) {
            gpioPins[i].setFunction("");
            gpioPins[i].setValue("");
            gpioPins[i].setPinNumber(i);
        }
    }

}
