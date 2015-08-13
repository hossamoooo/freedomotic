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
 */

package com.freedomotic.things.impl;

import com.freedomotic.behaviors.ListBehaviorLogic;
import com.freedomotic.model.ds.Config;
import com.freedomotic.model.object.ListBehavior;
import com.freedomotic.things.impl.ElectricDevice;
import com.freedomotic.reactions.Command;
import java.util.ArrayList;
import java.util.List;

public class Kodi extends ElectricDevice {

    //NOTE: transitions from one object state to another are done executing a generic action
    //the aim of this class is to map state changes to generic actions, performed by the plugin 
    //linked to this object protocol.
    //set of behaviors (the possible states of this object)
    protected final static String BEHAVIOR_KODIPLAYER0 = "kodiplayer0";
    protected final static String BEHAVIOR_KODIPLAYER1 = "kodiplayer1";
    protected final static String BEHAVIOR_KODIPLAYER2 = "kodiplayer2";
    private final List<ListBehaviorLogic> players = new ArrayList<ListBehaviorLogic>();

    @Override
    public void init() {
        //linking this xml behavior to a state change listener
        final ListBehaviorLogic kodiPlayer0 = new ListBehaviorLogic((ListBehavior) getPojo().getBehavior(BEHAVIOR_KODIPLAYER0));
        kodiPlayer0.addListener(new ListBehaviorLogic.Listener() {

            @Override
            public void selectedChanged(Config params, boolean fireCommand) {
                //when an object state change is requested
                changeKodiPlayerState(0, fireCommand, params);
            }
        });
        players.add(0, kodiPlayer0);
        registerBehavior(kodiPlayer0);

        //linking this xml behavior to a state change listener
        ListBehaviorLogic kodiPlayer1 = new ListBehaviorLogic((ListBehavior) getPojo().getBehavior(BEHAVIOR_KODIPLAYER1));
        kodiPlayer1.addListener(new ListBehaviorLogic.Listener() {

            @Override
            public void selectedChanged(Config params, boolean fireCommand) {
                changeKodiPlayerState(1, fireCommand, params);
            }
        });
        players.add(1, kodiPlayer1);
        registerBehavior(kodiPlayer1);

        //linking this xml behavior to a state change listener
        ListBehaviorLogic kodiPlayer2 = new ListBehaviorLogic((ListBehavior) getPojo().getBehavior(BEHAVIOR_KODIPLAYER2));
        kodiPlayer2.addListener(new ListBehaviorLogic.Listener() {

            @Override
            public void selectedChanged(Config params, boolean fireCommand) {
                changeKodiPlayerState(2, fireCommand, params);
            }
        });
        players.add(2, kodiPlayer2);
        registerBehavior(kodiPlayer2);

        //initialize the object 
        super.init();
    }

    //forces the related plugin to execute a command and then updates the object state 
    //according to the command execution result
    public void changeKodiPlayerState(int playerId, boolean fireCommand, Config params) {
        //a real plugin action is requested
        if (fireCommand) {
            //add two useful properties to be readed by the kodi plugin
            params.put("player.name", players.get(playerId).getName());
            params.put("player.action", params.getProperty("value"));
            //returns true if the command is executed succesfully, false otherwise
            if (executeCommand("set kodiplayer" + playerId, params)) {
                setKodiPlayer(playerId, params.getProperty("value"));
            }
            //a trigger needs just to update the object state (no real commands to execute)
        } else {
            setKodiPlayer(playerId, params.getProperty("value"));
        }
    }

    //changes only the object representation
    public void setKodiPlayer(int id, String value) {
        if (!players.get(id).getSelected().equals(value)) {
            players.get(id).setSelected(value);
            setIcon();
            setChanged(true);
        }
    }

    private void setIcon() {
        getPojo().setCurrentRepresentation(1);
        if (players.get(1).getSelected().equals("Play")) {
            getPojo().setCurrentRepresentation(2);
        } else if (players.get(1).getSelected().equals("Pause")) {
            getPojo().setCurrentRepresentation(3);
        } else if (players.get(0).getSelected().equals("Play")) {
            getPojo().setCurrentRepresentation(4);
        } else if (players.get(0).getSelected().equals("Pause")) {
            getPojo().setCurrentRepresentation(5);
        } else if (players.get(2).getSelected().equals("Play")) {
            getPojo().setCurrentRepresentation(6);
        } else if (players.get(2).getSelected().equals("Pause")) {
            getPojo().setCurrentRepresentation(7);
        }
    }

    @Override
    protected void createCommands() {
        //create commands using upper level objects definitions
        super.createCommands();

        Command playSong = new Command();
        playSong.setName("play current song");
        playSong.setDescription("starts playing current song");
        playSong.setReceiver("app.events.sensors.behavior.request.objects");
        playSong.setProperty("object", getPojo().getName());
        playSong.setProperty("behavior", BEHAVIOR_KODIPLAYER1);
        playSong.setProperty("value", "Play");

        //TODO: add more commands to be used in automations like "if party mode turns on then play current song"

        commandRepository.create(playSong);
    }

    @Override
    protected void createTriggers() {
        super.createTriggers();
    }
}
