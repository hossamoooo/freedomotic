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


/**
 *
 * @author steve
 */
public class KodiSystem {

    private String kodiName;
    private String kodiHost;
    private Integer kodiPort;
    private String kodiLocation;
    private Thread kodiThread;
    private String kodiPower;
    private String kodiMethod;
    private String kodiPlayer;
    
    public KodiSystem(String kodiName, String kodiHost, int kodiPort, String kodiLocation) {
        
        setKodiName(kodiName);
        setKodiHost(kodiHost);
        setKodiPort(kodiPort);
        setKodiLocation(kodiLocation);
        setKodiThread(null);
        setKodiPower("false");
        setKodiMethod("");
        setKodiPlayer("");
                
    }

     public void setKodiName(String kodiName) {
        this.kodiName = kodiName;
    }
     
     public String getKodiName() {
        return kodiName;
    }
     
    public void setKodiHost(String kodiHost) {
        this.kodiHost = kodiHost;
    }
     
     public String getKodiHost() {
        return kodiHost;
     }
     
     public void setKodiLocation(String kodiLocation) {
        this.kodiLocation = kodiLocation;
    }
     
     public String getKodiLocation() {
        return kodiLocation;
     }
     
    public void setKodiPort(int kodiPort) {
        this.kodiPort = kodiPort;
    }

    public int getKodiPort() {
        return kodiPort;
    }
    
    public void setKodiThread(Thread kodiThread) {
        this.kodiThread = kodiThread;
    }

    public Thread getKodiThread() {
        return kodiThread;
    }
    
    public void setKodiPower(String kodiPower) {
        this.kodiPower = kodiPower;
    }
   
     public String getKodiPower() {
        return kodiPower;
     }
     
     public void setKodiMethod(String kodiMethod) {
        this.kodiMethod = kodiMethod;
    }
   
     public String getKodiMethod() {
        return kodiMethod;
     }
     
      public String getKodiPlayer() {
        return kodiPlayer;
     }
     
     public void setKodiPlayer(String kodiPlayer) {
        this.kodiPlayer = kodiPlayer;
    }
}
