IBM MQ SSL/TLS connections
==========================

[A wizard to help configure your MQ SSL/TLS connections](https://github.com/ibm-messaging/mq-tls-ssl-wizard)


This package was originally released as `SupportPac MO04`
If you wish to contribute to this package, please read `CLA.md` 
for the IBM Contributor License Agreement.

## Scenario
The following scenario will be used for detailing the steps on enabling SSL support.
The diagram shows App1, a sample Java application running in a non-IBM JVM instance which uses MQ Client libraries to connect to MQ queue manager.

The queue manager is enabled for SSL connections and a channel APP1.SVRCONN is defined with SSL authentication. This server-connection channel is used by APP1 to connect to the MQ Queue Manager.

![][figure1]
credit: https://qadeer786.files.wordpress.com/2013/10/ssldemo1_overview.png

The following diagram shows how the certificates contain public, private keys and the contents of the key databases of either ends of the SSL channel.

![][figure2]
credit: https://qadeer786.files.wordpress.com/2013/10/ssldemo1_keysharing.png

### Simple example class

```java
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
     *      SVRCONN: DEF CHL(TEST.SSL.CHL)
     *      CHLTYPE(SVRCONN)
     *      SSLCIPH(RC4_MD5_US)
     * @param args Unused
     * @throws Exception No exception handling
     */
    public static void main(String[] args) throws Exception {

        // Queue manager details
        String qmgrName = "TEST.SSL";
        Hashtable props = new Hashtable();
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
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

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
```

## References
 - [IBM developer kits](https://developer.ibm.com/javasdk/)
 - [IBM® SDK, Java™ Technology Edition](https://www.ibm.com/support/knowledgecenter/en/SSYKE2)
 - [IBM MQ](https://www.ibm.com/support/knowledgecenter/SSFKSJ/com.ibm.mq.helphome.doc/product_welcome_wmq.htm)
 - [Using SSL support for Java clients & WebSphere MQ](https://qadeer786.wordpress.com/2013/10/08/using-ssl-support-for-java-clients-websphere-mq/)
 - [Custom SSLSocketFactory with WMQ Base Java](https://hursleyonwmq.wordpress.com/2007/03/08/custom-sslsocketfactory-with-wmq-base-java/)
 - [Configuring WebSphere Application Server to support TLS 1.2](https://www.ibm.com/support/knowledgecenter/en/SS2L6K_5.0.0/com.ibm.rational.relm.install.doc/topics/t_enable_tls1.2_was.html)
 - [Manage FIPS](https://www.ibm.com/support/knowledgecenter/en/SSAW57_8.5.5/com.ibm.websphere.nd.doc/ae/usec_manage_fips.html)
 - [Configure FIPS mode for DB2 and WebSphere](https://www.ibm.com/developerworks/data/library/techarticle/dm-ind-configure-fips-db2-ws/index.html)
 - [SSL configuration of the Websphere MQ Java/JMS client](https://www.ibm.com/developerworks/websphere/library/techarticles/0510_fehners/0510_fehners.html)
 - [Using SSL with IBM MQ classes for JMS](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_8.0.0/com.ibm.mq.dev.doc/q032390_.htm)
   - [SSL/TLS CipherSpecs and CipherSuites in IBM MQ classes for JMS](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_8.0.0/com.ibm.mq.dev.doc/q113220_.htm)
- [Sample Java client for IBM MQ Consumer and Producer](https://riyafa.wordpress.com/2016/02/21/sample-java-client-for-websphere-mq-consumer-and-producer/)
- [Creating and running a Java JMS client for IBM Websphere MQ, from the ground up](http://www.kevinboone.net/simplewmqclient.html)

[figure1]: https://github.com/junlapong/mq-tls-ssl-wizard/raw/master/media/mq_ssl_tls_overview.png "MQ SSL/TLS Overview"
[figure2]: https://github.com/junlapong/mq-tls-ssl-wizard/raw/master/media/ssl_tls_keysharing.png "SSL/TLS Keysharing"
