package clientjava.connections;

import com.freedomotic.plugins.devices.openwebnet.OpenWebNet;
import java.util.logging.Level;

/**
 * Adapter per i gestori connessione di basso livello (Object Adapter).
 * L'istanza del gestore è Singleton.
 *
 * @author Maurizio Lorenzoni (loremaur@libero.it)
 */
public class GestoreConnessioni implements GestoreConnessioniInterface {

    /*
     * ***********************
     * CONFIGURAZIONE SERVER
     */
    String Client_IPaddress = "192.168.1.101"; //tabella indirizzi aperti (da 101-103)
    OpenWebNet pluginRef = null;
    String gatewayAddress = "192.168.0.3";
    int gatewayPort = 20000;
    /*
     * *********************
     */
    //istanza sigleton del gestore
    private static GestoreConnessioni instance;
    //Istanze degli adaptee
    static GestoreSocketComandi gestSocketComandi = null;
    static GestoreSocketMonitor gestSocketMonitor = null;

    /**
     * costruttore privato (singleton)
     */
    private GestoreConnessioni() {
    }

    /**
     * Si assicura che ci sia una sola istanza del gestore nel sistema
     *
     * @return l'istanza Singleton del gestore
     */
    public static synchronized GestoreConnessioni getInstance() {
        if (instance == null) {
            instance = new GestoreConnessioni();
        }
        return instance;
    }

    public void init(String gatewayAddress, int gatewayPort, OpenWebNet pluginRef) {
        this.gatewayAddress = gatewayAddress;
        this.gatewayPort = gatewayPort;
        this.pluginRef = pluginRef;
        gestSocketComandi = new GestoreSocketComandi(pluginRef);
        gestSocketMonitor = new GestoreSocketMonitor(pluginRef);
    }

    /**
     * Invia al sistema un comando OpenWebNet utilizzando le API di basso
     * livello.
     *
     * @param comando una stringa contenente il comando OpenWebNet
     * @return true se il comando è inviato, false se non è possibile inviare il
     * comando
     */
    public boolean inviaComandoOpen(String comando) {

        if (comando.length() != 0) {
            if (gestSocketComandi.connect(gatewayAddress, gatewayPort, 000)) {
                gestSocketComandi.inviaComando(comando);
                gestSocketComandi.disconnect();
                return true;
            } else {
                //connessione ko
                pluginRef.getLogger().log(Level.SEVERE, "Connessione con il server KO");
                return false;
            }
        } else {
            pluginRef.getLogger().log(Level.SEVERE, "comando open non valido");
            return false;
        }

    }

    /**
     * Inizia una connessione di monitoring con il sistema. Utilizzare lo stdOut
     * per intercettare i messaggi (prefisso "Mon:").
     *
     * @return true se la connessione è stabilita, false altrimenti
     */
    public boolean startMonitoring() {

        if (gestSocketMonitor.connect(gatewayAddress, gatewayPort, 000)) {
            //connessione OK thread monitorizza giÃ  attivato
            return true;
        } else {
            return false;
        }
    }

    /**
     * Ferma la connessione di monitoring con il sistema.
     *
     * @return true se l'azione e riuscita, false altrimenti
     */
    public boolean stopMonitoring() {
        if (gestSocketMonitor != null) {
            gestSocketMonitor.disconnect();
            return true;
        }
        return false;
    }
}
