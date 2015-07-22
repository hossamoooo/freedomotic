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
package com.freedomotic.plugins.devices.weatherforecasts;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.ThingRepository;
import com.freedomotic.reactions.Command;
import com.google.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.aksingh.owmjapis.CurrentWeather;
import net.aksingh.owmjapis.OpenWeatherMap;
import org.json.JSONException;

public class WeatherForecasts
        extends Protocol {

    private static final Logger LOG = Logger.getLogger(WeatherForecasts.class.getName());
    final int POLLING_WAIT;
    private OpenWeatherMap owm;

    public WeatherForecasts() {
        super("Weather Forecasts", "/weather-forecasts/weather-forecasts-manifest.xml");
        POLLING_WAIT = configuration.getIntProperty("time-between-reads", 2000);
        setPollingWait(POLLING_WAIT); //millisecs interval between hardware device status reads
    }

    @Override
    protected void onShowGui() {
    }

    @Override
    protected void onHideGui() {
    }

    @Override
    protected void onRun() {
        try {
            retrieveData();
        } catch (IOException ex) {
            Logger.getLogger(WeatherForecasts.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(WeatherForecasts.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void onStart() {
        // declaring object of "OpenWeatherMap" class
        owm = new OpenWeatherMap("");
        owm.setUnits(OpenWeatherMap.Units.METRIC);
        LOG.info("Weather Forecasts plugin is started");
    }

    @Override
    protected void onStop() {
        LOG.info("Weather Forecasts plugin is stopped ");
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        LOG.info("Weather Forecasts plugin receives a command called " + c.getName() + " with parameters "
                + c.getProperties().toString());
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

    private void retrieveData()
            throws IOException, MalformedURLException, JSONException {

        // getting current weather data for the "London" city
        CurrentWeather cwd = owm.currentWeatherByCityName("Rome");

        // checking data retrieval was successful or not
        if (cwd.isValid()) {

            // checking if city name is available
            if (cwd.hasCityName()) {
                //printing city name from the retrieved data
                System.out.println("City: " + cwd.getCityName());
            }

            // checking if max. temp. and min. temp. is available
            if (cwd.getMainInstance().hasMaxTemperature() && cwd.getMainInstance().hasMinTemperature()) {
                // printing the max./min. temperature
                
                System.out.println("Temperature: " + cwd.getMainInstance().getMaxTemperature()
                        + "/" + cwd.getMainInstance().getMinTemperature() + "\'F");
            }
        }
    }
}
