/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.udoodomushield;

/**
 *
 * @author mauro
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
