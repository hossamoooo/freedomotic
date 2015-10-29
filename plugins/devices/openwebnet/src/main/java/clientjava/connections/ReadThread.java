/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clientjava.connections;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.*;

/**
 * Thread di supporto per la lettura di messaggi dal server.
 * Viene utilizzato:
 *  - durante handshaking di sessioni comando/monitor (metodo connect).
 *  - durante l'invio di un comando per ricevere ACK (metodo inviaComando)
 * 
 * Legge sulla buffer reader della socket specificata in cerca di
 * risposte del server per 30 sec (thread NewThread)
 *
 * Description:
 * Gestisce tramite un thread la ricezione di una stringa inviata dal WebServer
 *
 */
public class ReadThread extends Thread{

    Socket socket = null;
    BufferedReader input = null;
    int tipoSocket;
    int num = 0;
    int indice = 0;
    boolean esito = false;
    char risposta[];
	char c = ' ';
	int ci = 0;
	String responseString = null;

     //LOGGING
    private static Logger logger;
	private static FileHandler fh;

	/**
	 * Costruttore
	 *
	 * @param sock Socket da analizzare
	 * @param inp Character-input stream sul quale leggere i caratteri inviati dal WebServer
	 * @param tipoSocket Tipo di socket, 0 se è socket comandi, 1 se è monitor
	 */
	public ReadThread(Socket sock, BufferedReader inp, int tipoSocket){
        
        socket = sock;
        input = inp;
        this.tipoSocket = tipoSocket;
    }

	/**
	 * Avvia il Thread per la ricezione di una stringa inviata dal WebServer
	 */
    public void run(){
    	if(tipoSocket == 0) GestoreSocketComandi.responseLine = null;
    	else GestoreSocketMonitor.responseLineMon = null;
        num = 0;
        indice = 0;
        esito = false;
        risposta = new char[1024];  //array di carattery contenete il msg del server
    	c = ' '; //carattere letto
    	ci = 0;  //carattere come intero
    	try{
	    	do{//raccolgo un messaggio di risposta dal server
	    		if(socket != null && !socket.isInputShutdown()){
	    			ci = input.read();  //leggo carattere
		    		if (ci == -1){ //chiudo la socket se lo stream è finito
			        	num = 0;
			  			indice = 0;
			  			c = ' ';
			  			logger.log(Level.CONFIG, "----- Socket chiusa dal server -----");
			  			socket = null;
			  			 //annulla definitivam la socket
                        if(tipoSocket == 0){
			  			    GestoreConnessioni.gestSocketComandi.stato = 0;
			  			    GestoreConnessioni.gestSocketComandi.socket = null;
			  			}else{
			  				GestoreSocketMonitor.statoMonitor = 0;
			  				GestoreSocketMonitor.socketMon = null;
			  			}
			  			break;
			        }else{ //parsing della risposta del server
			        	c = (char)ci;
					    if (c == '#' && num == 0){
					    	risposta[indice] = c;
					    	num = indice;
					    	indice = indice +1;
					    }else if (c == '#' && indice == num + 1){
					    	risposta[indice] = c;
					    	esito = true;
					    	break;
					    }else if (c != '#'){
					    	risposta[indice] = c;
					    	num = 0;
					    	indice = indice + 1;
					    }else System.out.println("----------ERRORE-------------");
			        }
	    		}else{

	    		}
	        }while(true);
                    //in questo ciclo ci rimango fino a quando esito=true o il webserver chiude la socket;
	    				  //altrimenti esco quando scade il timeout e il thread viene interrotto.
		}catch(IOException e){
			System.out.println("eccezione <ReadThread>: ");
			e.printStackTrace();
	    }

		if (esito == true){
			responseString  = new String(risposta,0,indice+1);
			//informo i gestori della lettura effettuata
            if(tipoSocket == 0){
				GestoreSocketComandi.responseLine = responseString;
			}
			else{
				GestoreSocketMonitor.responseLineMon = responseString;
			}
		}else{
			if(tipoSocket == 0) GestoreSocketComandi.responseLine = null;
			else GestoreSocketMonitor.responseLineMon = null;
		}

		if(tipoSocket == 0){
			if (GestoreSocketComandi.timeoutThread != null) GestoreSocketComandi.timeoutThread.interrupt();
		}else{
			if (GestoreSocketMonitor.timeoutThreadMon != null) GestoreSocketMonitor.timeoutThreadMon.interrupt();
		}

		//logger.log(Level.INFO, "Thread ricezione stringa terminato");
		socket = null;
		input = null;
		risposta = null;
    }

}

