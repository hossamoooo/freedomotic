/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clientjava.connections;

import clientjava.openwebnet.OWN;
import com.freedomotic.plugins.devices.openwebnet.OpenWebNet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import java.util.logging.*;

/**
 *
 * @author Maurizio Lorenzoni (loremaur@libero.it)
 */
public class GestoreSocketComandi {

    //THREAD DI SUPPORTO
    static ReadThread readTh = null; //thread per la ricezione dei caratteri inviati dal webserver
    static TimerThread timeoutThread = null; //thread per la gestione dei timeout
    //STATO
    static String responseLine = null; //stringa in ricezione dal Webserver
    int stato = 0;  //stato socket comandi
    //SUPPORTO
    static final String socketComandi = "*99*0##"; //messaggio comando per server
    Socket socket = null;
    BufferedReader input = null;
    PrintWriter output = null;
    OWN openWebNet = null;   //supporto per l' OpenWebNet
    //LOGGING
    private static Logger logger;
    private static FileHandler fh;
    private OpenWebNet pluginRef;

    public GestoreSocketComandi(OpenWebNet pluginRef) {
        this.pluginRef = pluginRef;
    }

    /**
     * Si occupa dell' handshaking di sessioni comando.
     *
     * Apre una socket con il client (se possibile) e rende possibile il metodo
     * inviaComando (sulla stessa socket creata).
     *
     * Tentativo di apertura socket comandi verso il webserver Diversi possibili
     * stati: stato 0 = non connesso stato 1 = inviata richiesta socket comandi,
     * in attesa di risposta stato 2 = inviato risultato sulle operazioni della
     * password, attesa per ack o nack. Se la risposta e' ack si passa allo
     * stato 3 stato 3 = connesso correttamente
     *
     * @param ip Ip del webserver al quale connettersi
     * @param port Porta sulla quale aprire la connessione
     * @param passwordOpen Password open del webserver
     * @return true Se la connessione va a buon fine, false altrimenti
     */
    public boolean connect(String ip, int port, long password) {
        try {
            //opens a socket
            pluginRef.getLogger().log(Level.CONFIG, "Tentativo connessione a " + ip + "  Port: " + port);
            socket = new Socket(ip, port);

            //preparo stream di lettura/scrittura
            setTimeout(0);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pluginRef.getLogger().log(Level.INFO, "Buffer reader creato");
            output = new PrintWriter(socket.getOutputStream(), true);
            pluginRef.getLogger().log(Level.INFO, "Print Writer creato");

        } catch (IOException e) {
            pluginRef.getLogger().log(Level.SEVERE, "Server connection error");
            this.disconnect();
        }

        //uso la socket aperta

        if (socket != null) {
            while (true) {

                //ASPETTO DI AVERE QUALCOSA IN RESPONSELINE
                readTh = null;
                readTh = new ReadThread(socket, input, 0);
                readTh.start(); //leggo la prima risposta del server
                try {
                    readTh.join(); //aspetto di aver letto --> verrà copiata in responseLine
                } catch (InterruptedException e1) {
                    pluginRef.getLogger().log(Level.SEVERE, "----- ERRORE readThread.join() durante la connect:");
                    e1.printStackTrace();
                }
                //riempito responseLine, a seconda
                //dello stato in cui sono nel'handshake mi comporto diversamente
                if (responseLine != null) {

                    if (stato == 0) { //ho mandato la richiesta di connessione
                        pluginRef.getLogger().log(Level.INFO, "\n----- STATO 0 ----- ");
                        pluginRef.getLogger().log(Level.CONFIG, "Rx: " + responseLine);
                        //1.2- attendo messaggio ACK (per 30 sec)
                        if (responseLine.equals(OWN.MSG_OPEN_OK)) {//controllo ACK
                            System.out.println("--- Stabilita comunicazione TCP/IP con il server.");
                            //2.1 invia il codice *99*0## e rimane in attesa
                            pluginRef.getLogger().log(Level.CONFIG, "Tx: " + socketComandi);
                            output.write(socketComandi); //comandi (invio codice al server)

                            output.flush();
                            stato = 1;
                            setTimeout(0);
                        } else { //caso NACK
                            //se non mi connetto chiudo la socket
                            pluginRef.getLogger().log(Level.CONFIG, "--- Comunicazione TCP/IP con il server non riuscita.");
                            pluginRef.getLogger().log(Level.CONFIG, "Chiudo la socket verso il server ");
                            this.disconnect();
                            break;
                        }

                    } else if (stato == 1) { //ho mandato il tipo di servizio richiesto
                        pluginRef.getLogger().log(Level.INFO, "\n----- STATO 1 -----");
                        pluginRef.getLogger().log(Level.INFO, "Rx: " + responseLine);

                        //controllo password disattivato
						/*
                         * if(GestoreConnessioniAdapter.abilitaPass.isSelected()){
                         * //applico algoritmo di conversione
                         * logger.log(Level.CONFIG, "Controllo sulla password");
                         * long risultato =
                         * gestPassword.applicaAlgoritmo(passwordOpen,
                         * responseLine); logger.log(Level.CONFIG, "Tx:
                         * "+"*#"+risultato+"##",1,0,0);
                         * output.write("*#"+risultato+"##"); output.flush();
                         * stato = 2; //setto stato dopo l'autenticazione
                         * setTimeout(0); }else{
                         */
                        //non devo fare il controllo della password
                        pluginRef.getLogger().log(Level.CONFIG, "NON effettuo il controllo sulla password - mi aspetto ACK");

                        //2.6 se entro 30 sec non ricevo ACK -> chiudo la connessione
                        if (responseLine.equals(OWN.MSG_OPEN_OK)) {
                            pluginRef.getLogger().log(Level.CONFIG, "--- Stabilita sessione comandi con il server.");
                            pluginRef.getLogger().log(Level.CONFIG, "Ricevuto ack, stato = 3");
                            stato = 3;
                            break;
                        } else {
                            pluginRef.getLogger().log(Level.CONFIG, "Impossibile connettersi!!");
                            pluginRef.getLogger().log(Level.CONFIG, "--- Sessione comandi con il server non stabilita.");
                            //se non mi connetto chiudo la socket
                            pluginRef.getLogger().log(Level.INFO, "Chiudo la socket verso il server ");
                            this.disconnect();
                            break;
                        }
                        //}
                    } else if (stato == 2) { //attesa password (disattivato)
                        pluginRef.getLogger().log(Level.INFO, "\n----- STATO 2 -----");
                        pluginRef.getLogger().log(Level.CONFIG, "Rx: " + responseLine);
                        if (responseLine.equals(OWN.MSG_OPEN_OK)) {
                            pluginRef.getLogger().log(Level.CONFIG, "Connessione OK");
                            stato = 3;
                            break;
                        } else {
                            pluginRef.getLogger().log(Level.SEVERE, "Impossibile connettersi!!");
                            //se non mi connetto chiudo la socket
                            pluginRef.getLogger().log(Level.INFO, "Chiudo la socket verso il server ");
                            this.disconnect();
                            break;
                        }
                    } else {
                        break; //non dovrebbe servire (quando passo per lo stato tre esco dal ciclo con break)
                    }
                } else {
                    pluginRef.getLogger().log(Level.INFO, "--- Risposta dal webserver NULL");
                    this.disconnect();
                    break;//ramo else di if(responseLine != null)
                }
            }//chiude while(true)
        } else {
        }

        if (stato == 3) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Chiude la socket comandi ed imposta stato = 0
     *
     */
    public void disconnect() {
        if (socket != null) {
            try {
                socket.close();
                socket = null;
                stato = 0;
                pluginRef.getLogger().log(Level.CONFIG, "-----Socket chiusa correttamente-----");
                pluginRef.getLogger().log(Level.CONFIG, "--- Chiusa sessione comandi con il server.");
            } catch (IOException e) {
                pluginRef.getLogger().log(Level.CONFIG, "Errore Socket: <GestioneSocketComandi>");
                e.printStackTrace();
            }
        }
    }

    /**
     * Metodo per l'invio di un comando open una volta aperta la socket con il
     * metodo connect
     *
     * @param comandoOpen comando da inviare
     * @return 0 se il comando vine inviato, 1 se non Ã¨ possibile inviare il
     * comando
     */
    public int inviaComando(String comandoOpen) {
        //creo l'oggetto openWebNet con il comandoOpen
        try {
            openWebNet = new OWN(comandoOpen);
            if (openWebNet.isErrorFrame()) {
                pluginRef.getLogger().log(Level.CONFIG, "ERRATA frame open " + comandoOpen + ", la invio comunque!!!");
            } else {
                pluginRef.getLogger().log(Level.INFO, "CREATO oggetto OpenWebNet " + openWebNet.getFrameOpen());
            }
        } catch (Exception e) {
            pluginRef.getLogger().log(Level.CONFIG, "ERRORE nella creazione dell'oggetto OpenWebNet " + comandoOpen);
            pluginRef.getLogger().log(Level.CONFIG, "Eccezione in GestioneSocketComandi durante la creazione del'oggetto OpenWebNet");
            e.printStackTrace();
        }

        //3.1 invia il messaggio open e rimane in attesa della risposta (ACK/NACK) del server
        pluginRef.getLogger().log(Level.INFO, "Tx: " + comandoOpen);
        output.write(comandoOpen);
        output.flush();

        do {
            setTimeout(0);
            readTh = null;
            readTh = new ReadThread(socket, input, 0);
            readTh.start(); //attendo risposta dal server
            try {
                readTh.join();
            } catch (InterruptedException e1) {
                pluginRef.getLogger().log(Level.SEVERE, "----- ERRORE readThread.join() durante l'invio comando:");
                e1.printStackTrace();
            }

            //3.2 la risposta può essere ACK(*#*1##) o NACK(*#*0##)
            if (responseLine != null) {
                if (responseLine.equals(OWN.MSG_OPEN_OK)) {//ACK
                    pluginRef.getLogger().log(Level.CONFIG, "Rx: " + responseLine);
                    pluginRef.getLogger().log(Level.CONFIG, "Comando inviato correttamente");
                    this.disconnect();//chiudo connessione
                    return 0;
                    //break;
                } else if (responseLine.equals(OWN.MSG_OPEN_KO)) {//NACK
                    pluginRef.getLogger().log(Level.CONFIG, "Rx: " + responseLine);
                    pluginRef.getLogger().log(Level.SEVERE, "Comando NON inviato correttamente");
                    //if(!ClientFrame.mantieniSocket.isSelected()) this.disconnect();
                    this.disconnect();//chiudo connessione
                    return 0;
                    //break;
                } else {
                    //RICHIESTA STATO
                    System.out.println("Rx: " + responseLine);
                    if (responseLine == OWN.MSG_OPEN_OK) {
                        pluginRef.getLogger().log(Level.CONFIG, "Comando inviato correttamente");
                        this.disconnect();//chiudo connessione
                        return 0;
                        //break;
                    } else if (responseLine == OWN.MSG_OPEN_KO) {
                        pluginRef.getLogger().log(Level.CONFIG, "Comando NON inviato correttamente");
                        this.disconnect();//chiudo connessione
                        return 0;
                        //break;
                    }
                }
            } else {
                pluginRef.getLogger().log(Level.SEVERE, "Impossibile inviare il comando");
                this.disconnect();//chiudo connessione
                return 1;
                //break;
            }
        } while (true);
    }

    /**
     * Attiva il thread per il timeout sulla risposta inviata dal WebServer.
     *
     * @param tipoSocket: 0 se Ã¨ socket comandi, 1 se Ã¨ socket monitor
     */
    public void setTimeout(int tipoSocket) {
        timeoutThread = null;
        timeoutThread = new TimerThread("timeout", tipoSocket, pluginRef);
        timeoutThread.start();
    }
}
