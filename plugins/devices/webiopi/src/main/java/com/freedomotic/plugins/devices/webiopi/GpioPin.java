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
public class GpioPin {

    private String function;
    private String value;
    private Integer pinNumber;

    GpioPin(String function, String value, Integer pinNumber) {

        setFunction(function);
        setValue(value);
        setPinNumber(pinNumber);

    }

    void setFunction(String function) {
        this.function = function;
    }

    void setValue(String value) {
        this.value = value;
    }

    void setPinNumber(Integer pinNumber) {
        this.pinNumber = pinNumber;
    }

    String getFunction() {
        return this.function;
    }

    Integer getPinNumber() {
        return this.pinNumber;
    }

    String getValue() {
        return this.value;
    }

}
