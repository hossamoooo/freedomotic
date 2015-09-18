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
package com.freedomotic.plugins.devices.kodi;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonEncoding;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import java.io.IOException;
import java.io.*;

/**
 *
 * @author steve
 */
public class KodiThread implements Runnable {

    private KodiSystem myKodiSystem;
    OutputStream myOutputStream = null;
    InputStream myInputStream = null;

    public KodiThread(KodiSystem myKodiSystem) {
        this.myKodiSystem = myKodiSystem;
    }

    @Override
    public void run() {

        Boolean firstPass = true;

        do {
            try {
                while (myInputStream == null) {
                    if (firstPass == false) {
                        Thread.sleep(4000); // only needed for retry logic
                    }
                    firstPass = false;
                    Streams myStreams = new Streams(myKodiSystem.getKodiHost(), myKodiSystem.getKodiPort());
                    myInputStream = myStreams.getInputStream();
                    myOutputStream = myStreams.getOutputStream();
                    if (myOutputStream != null) { // Ping for power
                        sendJsonPing();
                    }
                }
            } catch (InterruptedException ieException) {
                //Freedomotic.logger.severe(myKodiSystem.getKodiHost()+" : ieException from set up streams main loop"); //Not sure what to do here
                myInputStream = null; //Stream has gone away get get new one
            } catch (IOException ioException) {
                //Freedomotic.logger.severe(myKodiSystem.getKodiHost()+" : ioException from set up streams main loop"); //lost the stream probably, Kodi closed? 
                myInputStream = null; //Stream has gone away get get new one
            }

            try {
                if (myInputStream != null) {
                    parseJson(myInputStream, myOutputStream); // check incomming json messages
                }
            } catch (IOException ioException) {
                //Freedomotic.logger.severe(myKodiSystem.getKodiHost()+" : ioException from parseJson main loop"); //lost the stream probably, Kodi closed?
                myInputStream = null; //Stream has gone away get get new one
            }
        } while (true);
    }

    private void parseJson(InputStream inJsonStream, OutputStream outJsonStream) throws IOException {

        JsonFactory factory = new JsonFactory();
        JsonParser jsonParser = factory.createJsonParser(inJsonStream);
        jsonParser.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE); //stop jasonParser.close from closing stream

        /*
         * use the streaming json parse approach, faster?
         */
        while (true) {

            try {
                parseJsonElement(jsonParser);
                processJson();
            } catch (IOException ioException) { //lost the stream probably
                //Freedomotic.logger.severe(myKodiSystem.getKodiHost()+" : IO Exception in parseJson"); //lost the stream probably, Kodi closed?
                throw new IOException("Lost connection or bad data");
            }
        }
    }

    private void parseJsonElement(JsonParser jsonParser) throws IOException {

        JsonToken currentToken;
        String debugString = "";
        Integer countObject = 0;
        Integer countField = 0;

        do {
            if (jsonParser.isClosed()) {
                throw new IOException("Lost connection or bad data");
            }
            try {
                currentToken = jsonParser.nextToken(); // move to next 'token
                countField = countField + 1;
                lookupJsonField(jsonParser.getCurrentName(), jsonParser.getText(), countField);// lookup to see if we want the content
                debugString = debugString + "||" + jsonParser.getCurrentName() + " | " + jsonParser.getText() + " | " + countField;
                if (currentToken == JsonToken.END_OBJECT) {
                    countObject = countObject - 1;
                } else if (currentToken == JsonToken.START_OBJECT) {
                    countObject = countObject + 1;
                }
            } catch (IOException ioException) {
                throw new IOException("Lost connection or bad data");//lost the stream probably
            }

        } while (countObject != 0);
        System.out.println("Json = :: " + debugString);

    }

    private void lookupJsonField(String currentFieldName, String jsonField, Integer countField) throws IOException {

        if (currentFieldName == null) {
        } else if (currentFieldName.equalsIgnoreCase("method")) {
            if (jsonField.equals("System.OnQuit")) { // Kodi system closing down in an orderly way
                myKodiSystem.setKodiPower("false");
            } else if (jsonField.equals("Player.OnPlay")) {
                myKodiSystem.setKodiMethod("Play");
            } else if (jsonField.equals("Player.OnPause")) {
                myKodiSystem.setKodiMethod("Pause");
            } else if (jsonField.equals("Player.OnStop")) { // Something stopped playing, Kodi includes the type, not the player
                myKodiSystem.setKodiMethod("Stop");
            }

        } else if (currentFieldName.equalsIgnoreCase("type")) {
            if (jsonField.equalsIgnoreCase("song")) {
                myKodiSystem.setKodiPlayer("0");
            } else if (jsonField.equalsIgnoreCase("movie")) {
                myKodiSystem.setKodiPlayer("1");
            } else if (jsonField.equalsIgnoreCase("episode")) {
                myKodiSystem.setKodiPlayer("1");
            } else if (jsonField.equalsIgnoreCase("unknown")) {
                myKodiSystem.setKodiPlayer("1");
            } else if (jsonField.equalsIgnoreCase("picture")) {
                myKodiSystem.setKodiPlayer("2");
            }

        } else if (currentFieldName.equalsIgnoreCase("playerid")) {
            myKodiSystem.setKodiPlayer(jsonField);

        } else if ((countField == 3) && (currentFieldName.equalsIgnoreCase("id"))) {  // id in this position means a response from Kodi
            if (jsonField.equalsIgnoreCase("pong")) {
                myKodiSystem.setKodiPower("true");
            }
        }

    }

    private void processJson() {
        ProtocolRead event;

        //       Freedomotic.logger.severe("KodiPower = " + myKodiSystem.getKodiPower() + " | KodiMethod = " + myKodiSystem.getKodiMethod() + " | KodiPlayer = " + myKodiSystem.getKodiPlayer()); 

        if ((!myKodiSystem.getKodiMethod().equalsIgnoreCase("")) && (!myKodiSystem.getKodiPlayer().equalsIgnoreCase(""))) {
            event = new ProtocolRead(this, "kodi", myKodiSystem.getKodiHost());
            event.addProperty("object.name", "kodi-" + myKodiSystem.getKodiName());
            event.addProperty("object.class", "kodi");
            event.addProperty("function", "player" + myKodiSystem.getKodiPlayer());
            event.addProperty("kodiplayer" + myKodiSystem.getKodiPlayer(), myKodiSystem.getKodiMethod());
            Freedomotic.sendEvent(event);
        }
        if (true) {  // always send a power status?
            event = new ProtocolRead(this, "kodi", myKodiSystem.getKodiHost());
            event.addProperty("powered", myKodiSystem.getKodiPower());
            event.addProperty("object.name", "kodi-" + myKodiSystem.getKodiName());
            event.addProperty("object.class", "kodi");
            event.addProperty("function", "power");
            Freedomotic.sendEvent(event);
        }
        myKodiSystem.setKodiMethod("");
        myKodiSystem.setKodiPlayer("");
    }

    private void sendJsonPing() throws IOException {  // used for power detection on startup
        JsonFactory factory = new JsonFactory();

        JsonGenerator jsonGenerator = factory.createJsonGenerator(myOutputStream, JsonEncoding.UTF8);
        jsonGenerator.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET); //stop jasonGenerator.close from closing stream

        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("jsonrpc", "2.0");
        jsonGenerator.writeStringField("method", "JSONRPC.Ping");
        jsonGenerator.writeStringField("id", "pong");
        jsonGenerator.writeEndObject();
        jsonGenerator.close();

    }

    private void getActivePlayers() throws IOException {  // works but not needed?
        JsonFactory factory = new JsonFactory();

        JsonGenerator jsonGenerator = factory.createJsonGenerator(myOutputStream, JsonEncoding.UTF8);
        jsonGenerator.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET); //stop jasonGenerator.close from closing stream
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("jsonrpc", "2.0");
        jsonGenerator.writeStringField("method", "Player.GetActivePlayers");
        jsonGenerator.writeStringField("id", "GetActivePlayers");
        jsonGenerator.writeEndObject();
        jsonGenerator.close();

    }

    private void getPlayerStatus() throws IOException {   // WIP - may need it for startup condition?
        JsonFactory factory = new JsonFactory();

        JsonGenerator jsonGenerator = factory.createJsonGenerator(myOutputStream, JsonEncoding.UTF8);
        jsonGenerator.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET); //stop jasonGenerator.close from closing stream

        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("jsonrpc", "2.0");
        jsonGenerator.writeStringField("method", "Player.GetProperties");
        jsonGenerator.writeStringField("id", "PlayerGetProperties");
        jsonGenerator.writeArrayFieldStart("params");
        jsonGenerator.writeStringField("playerid", "1");
        jsonGenerator.writeArrayFieldStart("properties");
        jsonGenerator.writeString("speed");
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
        jsonGenerator.close();

    }
}