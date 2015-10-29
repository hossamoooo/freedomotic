package clientjava.connections;

/**
 * *************************************************************************
 * MonitorThread.java * -------------------------- * date : Sep 6, 2004 *
 * copyright : (C) 2005 by Bticino S.p.A. Erba (CO) - Italy * Embedded Software
 * Development Laboratory * license : GPL * email : * web site : www.bticino.it;
 * www.myhome-bticino.it *
 **************************************************************************
 */
/**
 * *************************************************************************
 *                                                                         *
 * This program is free software; you can redistribute it and/or modify * it
 * under the terms of the GNU General Public License as published by * the Free
 * Software Foundation; either version 2 of the License, or * (at your option)
 * any later version. * *
 **************************************************************************
 */
import com.freedomotic.plugins.devices.openwebnet.OpenWebNet;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.*;

/**
 * Una volta passatagli una socket monitor creata opportunamente, continua a
 * guardarla in cerca di messaggi
 *
 * Description: Gestisce tramite un thread la ricezione di tutti i messaggi che
 * passano sulla socket monitor.
 *
 */
public class MonitorThread extends Thread {

    Socket socketMon = null;
    BufferedReader inputMon = null;
    int num = 0;
    int indice = 0;
    boolean esito = false;
    char risposta[] = null;
    char c = ' ';
    int ci = 0;
    String responseString = null;
    //LOGGING
    private static Logger logger;
    private static FileHandler fh;
    private OpenWebNet pluginRef;

    //String responseLine = null; //stringa in ricezione dal Webserver
    /**
     * Costruttore
     */
    public MonitorThread(Socket sock, BufferedReader inp, OpenWebNet pluginRef) {
        this.pluginRef = pluginRef;  
        
        socketMon = sock;
        inputMon = inp;
    }

    /**
     * Avvia il Thread per la ricezione dei messaggi sulla monitor
     */
    public void run() {
        do {
            GestoreSocketMonitor.responseLineMon = null;
            num = 0;
            indice = 0;
            esito = false;
            risposta = new char[1024];
            c = ' ';
            ci = 0;
            try {
                do { //raccolgo un messaggio di risposta dal server
                    if (socketMon != null && !socketMon.isInputShutdown()) {
                        ci = inputMon.read();

                        if (ci == -1) {
                            num = 0;
                            indice = 0;
                            c = ' ';
                            pluginRef.getLogger().log(Level.CONFIG, "Mon: ----- Socket chiusa dal server -----");
                            socketMon = null;
                            GestoreSocketMonitor.statoMonitor = 0;
                            break;
                        } else {
                            c = (char) ci;
                            //System.out.println("Carattere ricevuto:  "+c);				        				        
                            if (c == '#' && num == 0) {
                                risposta[indice] = c;
                                num = indice;
                                indice = indice + 1;
                            } else if (c == '#' && indice == num + 1) {
                                risposta[indice] = c;
                                esito = true;
                                break;
                            } else if (c != '#') {
                                risposta[indice] = c;
                                num = 0;
                                indice = indice + 1;
                            }
                        }
                    } else {
                        //System.out.println("&&&&&   socket nulla");
                    }
                } while (esito != true); //finchè non l'ho raccolto...
            } catch (IOException e) {
                //System.out.println("Mon eccezione: ");
                //e.printStackTrace();
            }

            if (esito == true) {
                responseString = new String(risposta, 0, indice + 1);
                GestoreSocketMonitor.responseLineMon = responseString;    //imposto la
            } else {
                GestoreSocketMonitor.responseLineMon = null;
                GestoreSocketMonitor.statoMonitor = 0;
                break;
            }

            /**
             * ATTENZIONE I messaggi della connessione monitor vengono buttati
             * su standard output. Se voglio innescare delle logiche devo
             * intercettarli e filtrare quelli con prefisso "Mon:"
             */
            pluginRef.getLogger().log(Level.CONFIG, "Mon: " + GestoreSocketMonitor.responseLineMon);
            pluginRef.buildEventFromFrame(GestoreSocketMonitor.responseLineMon);
            risposta = null;
        } while (GestoreSocketMonitor.statoMonitor == 3);

        pluginRef.getLogger().log(Level.INFO, "Thread Monitorizza terminato");
    }
}
