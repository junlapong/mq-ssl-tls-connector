package mq;

// Standard Java API imports 
import java.io.FileInputStream;
import java.util.Hashtable;

// Standard Java SSL API imports
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

// WMQ imports
import com.ibm.mq.MQC;
import com.ibm.mq.MQQueueManager;

import org.junit.Test;
import org.junit.Ignore;

/**
 * Simple command line class to create an SSL connection
 * to a queue manager, with the WMQ Base Java classes.
 * @author hursleyonwmq blogger
 * @see https://hursleyonwmq.wordpress.com
 */
public class WmqSslTest {

    /**
     * Main method
     * Example MQSC to define
     *     SVRCONN: DEF CHL(TEST.SSL.CHL)
     *              CHLTYPE(SVRCONN)
     *              SSLCIPH(RC4_MD5_US)
     */
    @Test
    @Ignore
    public void testMqConnection() throws Exception {

        // Queue manager details
        String qmgrName = "TEST.SSL";
        Hashtable<String, Object> props = new Hashtable<String, Object>();
        props.put(MQC.CHANNEL_PROPERTY,   "TEST.SSL.CHL");
        props.put(MQC.HOST_NAME_PROPERTY, "localhost");
        props.put(MQC.PORT_PROPERTY,      new Integer(1414));
        
        // SSL details
        props.put(MQC.SSL_CIPHER_SUITE_PROPERTY, "SSL_RSA_WITH_AES_128_CBC_SHA256");
        String keyStorePath   = "/path/to/keystore.jks";
        String trustStorePath = "/path/to/keystore.jks";
        String password       = "passw0rd";

        // Create a keystore object for the keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");

        // Open our file and read the keystore
        FileInputStream keyStoreInput = new FileInputStream(keyStorePath);
        try {
            keyStore.load(keyStoreInput, password.toCharArray());
        }
        finally {
            keyStoreInput.close();
        }               

        // Create a keystore object for the truststore
        KeyStore trustStore = KeyStore.getInstance("JKS");
        
        // Open our file and read the truststore (no password)
        FileInputStream trustStoreInput = new FileInputStream(trustStorePath);
        try {
            trustStore.load(trustStoreInput, null);
        }
        finally {
            trustStoreInput.close();
        }               

        // Create a default trust and key manager
        TrustManagerFactory trustManagerFactory = 
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        KeyManagerFactory keyManagerFactory = 
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

        // Initialise the managers
        trustManagerFactory.init(trustStore);
        keyManagerFactory.init(keyStore, password.toCharArray());

        // Get an SSL context. For more information on providers see:
        // http://www.ibm.com/developerworks/library/j-ibmsecurity.html
        // Note: Not all providers support all CipherSuites.
        SSLContext sslContext = SSLContext.getInstance("SSL"); // TLS
        System.out.println("SSLContextider: " + sslContext.getProvider().toString());

        // Initialise our SSL context from the key/trust managers  
        sslContext.init(keyManagerFactory.getKeyManagers(),
            trustManagerFactory.getTrustManagers(), null);

        // Get an SSLSocketFactory to pass to WMQ
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        
        // Set the socket factory in our WMQ parameters
        props.put(MQC.SSL_SOCKET_FACTORY_PROPERTY, sslSocketFactory);
        
        // Connect to WMQ
        MQQueueManager qmgr = new MQQueueManager(qmgrName, props);
        try {
        
            // Query the description
            String desc = qmgr.getDescription();
            
            // Output the description
            System.out.println("Queueger DESCR: \"" + desc + "\"");

        }
        finally {
            qmgr.disconnect();
        }
    }
}