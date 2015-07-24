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
import com.freedomotic.events.ProtocolRead;
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
    private final int POLLING_WAIT;
    final String UNITS;
    final String CITY_NAME;
    private OpenWeatherMap owm;

    public WeatherForecasts() {
        super("Weather Forecasts", "/weather-forecasts/weather-forecasts-manifest.xml");
        POLLING_WAIT = configuration.getIntProperty("time-between-reads", 50000);
        UNITS = configuration.getStringProperty("units", "metric");
        CITY_NAME = configuration.getStringProperty("city-name", "Rome");
        setPollingWait(POLLING_WAIT);
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
            LOG.log(Level.SEVERE, "IOException retrieving data ", ex);
        } catch (JSONException ex) {
            LOG.log(Level.SEVERE, "JSONException retrieving data ", ex);
        }
    }

    @Override
    protected void onStart() {
        // declaring object of "OpenWeatherMap" class
        owm = new OpenWeatherMap("");
        owm.setUnits(OpenWeatherMap.Units.valueOf(UNITS));
        LOG.log(Level.INFO, "Weather Forecasts plugin is started");
    }

    @Override
    protected void onStop() {
        LOG.log(Level.INFO, "Weather Forecasts plugin is stopped ");
    }

    @Override
    protected void onCommand(Command c) {
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

        CurrentWeather cwd = owm.currentWeatherByCityName(CITY_NAME);

        // checking if data retrieval was successful or not
        if (cwd.isValid()) {

            //building the event
            ProtocolRead event = new ProtocolRead(this, "weather-forecasts", cwd.getCityName());
            //adding some information to the event
            if (cwd.hasCityName()) {
                event.getPayload().addStatement("city-name", cwd.getCityName());
            }
            if (cwd.hasWeatherInstance()) {
                event.getPayload().addStatement("conditions", cwd.getWeatherInstance(0).getWeatherName());
            }
            if (cwd.hasWindInstance() && cwd.getWindInstance().hasWindSpeed()) {
                event.getPayload().addStatement("wind-speed", String.valueOf(cwd.getWindInstance().getWindSpeed()));
            }
            if (cwd.hasWindInstance() && cwd.getWindInstance().hasWindDegree()) {
                event.getPayload().addStatement("wind-degree", String.valueOf(cwd.getWindInstance().getWindDegree()));
            }
            if (cwd.hasRainInstance() && cwd.getRainInstance().hasRain()) {
                event.getPayload().addStatement("rain", String.valueOf(cwd.getRainInstance().getRain()));
            }
            if (cwd.getMainInstance().hasTemperature()) {
                event.getPayload().addStatement("temperature", String.valueOf(cwd.getMainInstance().getTemperature()));
            }
            if (cwd.getMainInstance().hasMaxTemperature()) {
                event.getPayload().addStatement("max-temperature", String.valueOf(cwd.getMainInstance().getMaxTemperature()));
            }
            if (cwd.getMainInstance().hasMinTemperature()) {
                event.getPayload().addStatement("min-temperature", String.valueOf(cwd.getMainInstance().getMinTemperature()));
            }
            if (cwd.getMainInstance().hasPressure()) {
                event.getPayload().addStatement("pressure", String.valueOf(cwd.getMainInstance().getPressure()));
            }
            if (cwd.getMainInstance().hasHumidity()) {
                event.getPayload().addStatement("humidity", String.valueOf(cwd.getMainInstance().getHumidity()));
            }
            if (cwd.getSysInstance().hasSunriseTime()) {
                event.getPayload().addStatement("sunrise-time", String.valueOf(cwd.getSysInstance().getSunriseTime()));
            }
            if (cwd.getSysInstance().hasSunsetTime()) {
                event.getPayload().addStatement("sunset-time", String.valueOf(cwd.getSysInstance().getSunsetTime()));
            }
            //publish the event on the messaging bus
            notifyEvent(event);
        }
    }
}
