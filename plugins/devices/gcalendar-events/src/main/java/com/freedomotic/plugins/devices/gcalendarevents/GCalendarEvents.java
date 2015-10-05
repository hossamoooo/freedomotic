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
package com.freedomotic.plugins.devices.gcalendarevents;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.Trigger;
import com.freedomotic.reactions.TriggerRepository;
import com.freedomotic.settings.Info;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;
import com.google.inject.Inject;
import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public class GCalendarEvents extends Protocol {

    private static final Logger LOG = Logger.getLogger(GCalendarEvents.class.getName());
    private final int POLLING_TIME = configuration.getIntProperty("time-between-reads", 5000);
    private final String CALENDAR_ID = configuration.getStringProperty("calendar-id", "primary");
    @Inject
    protected TriggerRepository triggerRepository;
    com.google.api.services.calendar.Calendar service;
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME =
            "freedomotic";
    /**
     * Directory to store user credentials for this application.
     */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/freedomotic");
    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();
    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;
    /**
     * Global instance of the scopes required by this quickstart.
     */
    private static final List<String> SCOPES =
            Arrays.asList(CalendarScopes.CALENDAR_READONLY);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                new FileInputStream(new File(Info.PATHS.PATH_DEVICES_FOLDER + System.getProperty("file.separator") + "gcalendar-events/data/client_secret.json"));
        //GCalendarEvents.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        LOG.log(Level.INFO,
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     *
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public static com.google.api.services.calendar.Calendar getCalendarService() throws IOException {
        Credential credential = authorize();
        return new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
    }

    public GCalendarEvents() {
        super("Google Calendar UI", "/gcalendar-events/gcalendar-events-manifest.xml");
    }

    @Override
    public void onStart() throws PluginStartupException {
        setPollingWait(POLLING_TIME);
        try {
            service = getCalendarService();
            retrieveEvents();
        } catch (IOException ex) {
            throw new PluginStartupException("Exception during Google Calendar Service creation " + ex.getLocalizedMessage());
        }
    }

    @Override
    public void onStop() {
        setDescription(configuration.getStringProperty("description", ""));
    }

    @Override
    protected void onRun() {
        retrieveEvents();
    }

    private void retrieveEvents() {
        try {
            // List the next events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            Events events = service.events().list(CALENDAR_ID).setTimeMin(now).setOrderBy("startTime").setSingleEvents(true).execute();
            List<Event> items = events.getItems();
            if (items.size() == 0) {
                LOG.log(Level.INFO, "No upcoming events found.");
            } else {
                LOG.log(Level.INFO, "Upcoming events");
                for (Event event : items) {
                    DateTime start = event.getStart().getDateTime();
                    if (start == null) {
                        start = event.getStart().getDate();
                    }
                    LOG.info(
                            "Readed Google Calendar event '" + event.getSummary() + "' at " + event.getStart());
                    //if the tigger isn't already present 
                    if (triggerRepository.findByName(event.getSummary()).isEmpty()) {

                        Trigger t = new Trigger();
                        t.setName(event.getSummary());
                        t.setDescription("A time based trigger readed from Google Calendar");
                        t.setChannel("app.event.sensor.calendar.event.schedule");

                        GregorianCalendar cal = new GregorianCalendar();
                        cal.setTime(new Date(event.getStart().getDateTime().getValue()));
                        t.getPayload().addStatement("time.hour", cal.get(GregorianCalendar.HOUR_OF_DAY));
                        t.getPayload().addStatement("time.minute", cal.get(GregorianCalendar.MINUTE));
                        t.getPayload().addStatement("time.second", "0"); //only at the first second of a minute
                        t.getPayload().addStatement("date.day", cal.get(GregorianCalendar.DAY_OF_MONTH));
                        t.getPayload().addStatement("date.month", cal.get(GregorianCalendar.MONTH) + 1);
                        t.getPayload().addStatement("date.year", cal.get(GregorianCalendar.YEAR));
                        t.setPersistence(true);
                        triggerRepository.create(t);
                    }
                    setDescription(items.size() + " event(s) synchronized with your online account");
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, ex.getLocalizedMessage());
        }
    }
            
            
    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
