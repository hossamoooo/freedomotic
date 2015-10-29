/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clientjava.connections;

import com.freedomotic.plugins.devices.openwebnet.OpenWebNet;
import java.io.IOException;
import java.util.logging.*;

/**
 *
 * Dopo 30 sec chiude i thread di comunicazione con il server per handshacking
 * di sessioni comando o monitor
 *
 * Description: Gestisce i timeout durante la procedura di connessione al
 * WebServer e l'invio dei comandi open
 *
 */
public class TimerThread extends Thread {

    String name;
    int time = 0; //rappresenta lo sleep del thread (15 sec o 30 sec)
    int statoEntrata = 0;
    int tipoSocket; // 0 se la socket è di tipo comandi, 1 se è di tipo monitor
    //LOGGING
    private static Logger logger;
    private static FileHandler fh;
    private OpenWebNet pluginRef;

    /**
     * Costruttore
     *
     * @param threadName Nome del Thread
     * @param tipoSocket Tipo di socket che richiama il costruttore, 0 se è
     * socket comandi, 1 se è monitor
     */
    public TimerThread(String threadName, int tipoSocket, OpenWebNet pluginRef) {

        this.pluginRef = pluginRef;
        name = threadName;
        this.tipoSocket = tipoSocket;
        if (tipoSocket == 0) {
            statoEntrata = GestoreConnessioni.gestSocketComandi.stato;
        } else {
            statoEntrata = GestoreSocketMonitor.statoMonitor;
        }
        pluginRef.getLogger().log(Level.INFO, "Thread per il timeout attivato");
    }

    /**
     * Avvia il Thread per gestire il timeout
     */
    public void run() {
        do {
            time = 30000; //30 sec di timeout
            //time = 30000000; //30 sec di timeout


            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                pluginRef.getLogger().log(Level.INFO, "Thread timeout interrotto!");
                break;
                //e.printStackTrace();
            }

            pluginRef.getLogger().log(Level.INFO, "Thread timeout SCADUTO!");
            //chiudo il thread per la ricezione dei caratteri
            if (tipoSocket == 0) {
                if (GestoreSocketComandi.readTh != null) {
                    GestoreSocketComandi.readTh.interrupt();
                }
            } else {
                if (GestoreSocketMonitor.readThMon != null) {
                    GestoreSocketMonitor.readThMon.interrupt();
                }
            }
            break;
        } while (true);
    }
}
