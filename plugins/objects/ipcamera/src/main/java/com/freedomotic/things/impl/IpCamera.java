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
package com.freedomotic.things.impl;

import com.freedomotic.reactions.Command;

/**
 *
 * @author Mauro Cicolella
 */
public class IpCamera extends ElectricDevice {

    @Override
    public void init() {
        super.init();
    }

    @Override
    protected void createCommands() {
        super.createCommands();

        Command a = new Command();
        a.setName("capture video");
        a.setDescription("the IPCamera " + getPojo().getName() + " captures a video");
        a.setReceiver("app.events.sensors.behavior.request.objects");
        a.setProperty("object", getPojo().getName());

        Command b = new Command();
        b.setName("Capture image");
        b.setDescription("the IPCamera " + getPojo().getName() + " captures an image");
        b.setReceiver("app.events.sensors.behavior.request.objects");
        b.setProperty("object", getPojo().getName());

        commandRepository.create(a);
        commandRepository.create(b);
    }

    @Override
    protected void createTriggers() {
        super.createTriggers();
    }
}
