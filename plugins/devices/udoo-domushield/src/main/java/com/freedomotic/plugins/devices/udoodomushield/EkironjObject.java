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
public class EkironjObject {

    String name = null;
    String type = null;
    String freedomoticClass = null;
    String storedValue = null;
    String address = null;

    EkironjObject(String name, String type, String freedomoticClass, String storedValue, String address) {

        setName(name);
        setType(type);
        setFreedomoticClass(freedomoticClass);
        setStoredValue(storedValue);
        setAddress(address);
    }

    String getName() {
        return name;
    }

    String getType() {
        return type;
    }

    String getFreedomoticClass() {
        return freedomoticClass;
    }

    String getStoredValue() {
        return storedValue;
    }

    String getAddress() {
        return address;
    }

    void setName(String name) {
        this.name = name;
    }

    void setType(String type) {
        this.type = type;
    }

    void setFreedomoticClass(String freedomoticClass) {
        this.freedomoticClass = freedomoticClass;
    }

    void setStoredValue(String storedValue) {
        this.storedValue = storedValue;
    }

    void setAddress(String address) {
        this.address = address;
    }
}
