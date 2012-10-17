package ee.ut.cs.courses.appliedcrypto.library;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import ee.ut.cs.courses.appliedcrypto.library.logic.LibraryLogic;
import ee.ut.cs.courses.appliedcrypto.library.logic.LibraryLogicImpl;
import ee.ut.cs.courses.appliedcrypto.model.asn1.LenderQuery;
import ee.ut.cs.courses.appliedcrypto.util.Utils;

public class LibraryServer {
    private static final Logger log = Logger.getLogger(LibraryServer.class);
    
    private static final String SERVER_KEYSTORE = "keystore/library-ssl.jks";
    private static final String TRUST_KEYSTORE = "keystore/trust-ssl.jks";
    private static final char[] PASSWORD = "password".toCharArray();

    private int PORT = 8888;
    private SSLServerSocket serverSock;
    private LibraryLogic libraryLogic;

    public LibraryServer(String port) {
        try {
            PORT = Integer.parseInt(port);
        } catch (NumberFormatException e) {}
        
        Security.addProvider(new BouncyCastleProvider());
        
        try {
            SSLContext sslContext = createSSLContext();
            SSLServerSocketFactory fact = sslContext.getServerSocketFactory();
            serverSock = (SSLServerSocket) fact.createServerSocket(PORT);
        } catch (Exception e) {
            Utils.terminate("Exception occured. Terminating", e);
        }
        
        libraryLogic = new LibraryLogicImpl();
    }

    private SSLContext createSSLContext() throws Exception {
        // set up a key manager for our local credentials
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        KeyStore clientStore = KeyStore.getInstance("JKS");
        InputStream keyInputStream = new FileInputStream(SERVER_KEYSTORE);
        clientStore.load(keyInputStream, PASSWORD);
        keyInputStream.close();
        keyManagerFactory.init(clientStore, PASSWORD);

        // set up a trust manager so we can recognise the client
        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance("SunX509");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        InputStream trustInputStream = new FileInputStream(TRUST_KEYSTORE);
        trustStore.load(trustInputStream, PASSWORD);
        trustInputStream.close();
        trustFactory.init(trustStore);

        // create a context and set up a socket factory
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustFactory.getTrustManagers(), null);

        return sslContext;
    }

    public void run() {
        log.info("Library ready. Server listening on port " + PORT);
        // client authenticate where possible
        serverSock.setWantClientAuth(true);

        // loop infinitely
        while (true) {
            SSLSocket sslSock = null;
            try {
                sslSock = (SSLSocket) serverSock.accept();
                sslSock.startHandshake();
            } catch (IOException e1) {
                log.warn("Failed to establish SSL connection." +
                        "Make sure the trusted certificates are loaded in the keystore.", e1);
                continue;
            }
            
            // can check for 2-way authentication
            /*String peer = null;
            try {
                peer = sslSock.getSession().getPeerPrincipal().getName();
                log.debug("Peer = " + peer);
            } catch (IOException e) {
            }*/
            
            // it seems that we can only check if the remote socket has been 
            // closed by receiving null while reading from the input stream 
            while (true) {
                try {
                    LenderQuery readQuery = libraryLogic.readQuery(sslSock.getInputStream());
                    if (readQuery == null) {
                        break;
                    }
                    libraryLogic.processQuery(readQuery, sslSock.getOutputStream());
                } catch (IOException e) {
                    log.warn("IO failed", e);
                } catch (IllegalArgumentException e) {
                    log.warn("Incorrect query recieved. Ignoring.", e);
                }
            }
            
            try {
                sslSock.close();
                log.debug("Connection closed");
            } catch (Exception e) {
                log.warn("Failed to close connection", e);
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            Utils.terminate("Usage: java LibraryServer port");
        }
        new LibraryServer(args[0]).run();
    }

}
