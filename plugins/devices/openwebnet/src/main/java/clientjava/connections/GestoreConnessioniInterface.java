/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clientjava.connections;

/**
 * Interfaccia High Level di accesso alle API di basso livello
 *
 * @author Maurizio Lorenzoni (loremaur@libero.it)
 */
public interface GestoreConnessioniInterface {

    /**
     * Invia al sistema un comando OpenWebNet.
     *
     * @param comando una stringa contenente il comando OpenWebNet
     * @return true se il comando è inviato, false se non è possibile inviare il comando
     */
    public boolean inviaComandoOpen(String comando) ;

    /**
     * Inizia una connessione di monitoring con il sistema.
     * Utilizzare lo stdOut per intercettare i messaggi (prefisso "Mon:").
     *
     * @return true se la connessione è stabilita, false altrimenti
     */
    public boolean startMonitoring() ;

    /**
     * Ferma la connessione di monitoring con il sistema.
     *
     * @return true se l'azione e riuscita, false altrimenti
     */
    public boolean stopMonitoring() ;

}
