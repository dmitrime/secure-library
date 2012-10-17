package ee.ut.cs.courses.appliedcrypto.lender;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.Security;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import ee.ut.cs.courses.appliedcrypto.lender.logic.LenderLogic;
import ee.ut.cs.courses.appliedcrypto.model.asn1.LibraryItem;
import ee.ut.cs.courses.appliedcrypto.util.Utils;

public class LenderClient {
    private static final Logger log = Logger.getLogger(LenderClient.class);

    private static final String CLIENT_KEYSTORE = "keystore/lender-ssl.jks";
    private static final String TRUST_KEYSTORE = "keystore/trust-ssl.jks";
    private static final char[] PASSWORD = "password".toCharArray();

    private String host = "localhost";
    private int port = 8888;

    private SSLSocket clientSock;
    private LenderLogic lenderLogic;

    public LenderClient(String host, String port, boolean auth) {
        this.host = host;
        try {
            this.port = Integer.parseInt(port);
        } catch (NumberFormatException e) {}
        
        Security.addProvider(new BouncyCastleProvider());

        try {
            SSLContext sslContext = createSSLContext(auth);
            SSLSocketFactory fact = sslContext.getSocketFactory();
            clientSock = (SSLSocket) fact.createSocket(this.host, this.port);
            clientSock.startHandshake();
        } catch (UnknownHostException e) {
            Utils.terminate("Failed to connect. Wrong host name.", e);
        } catch (IOException e) {
            Utils.terminate("Failed to connect.", e);
        } catch (Exception e) {
            Utils.terminate("Exception occured.", e);
        }

        lenderLogic = new LenderLogicImpl();
    }

    private SSLContext createSSLContext(boolean auth) throws Exception {
        KeyManagerFactory keyManagerFactory = null;
        // using 2-way authentication or not
        if (auth) {
            // set up a key manager for our local credentials
            keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            KeyStore clientStore = KeyStore.getInstance("JKS");
            InputStream keyInputStream = new FileInputStream(CLIENT_KEYSTORE);
            clientStore.load(keyInputStream, PASSWORD);
            keyInputStream.close();
            keyManagerFactory.init(clientStore, PASSWORD);
        }

        // set up a trust manager so we can recognise the server
        TrustManagerFactory trustFact = TrustManagerFactory.getInstance("SunX509");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        InputStream trustInputStream = new FileInputStream(TRUST_KEYSTORE);
        trustStore.load(trustInputStream, PASSWORD);
        trustInputStream.close();
        trustFact.init(trustStore);

        // create a context and set up a socket factory
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory == null ? null : keyManagerFactory.getKeyManagers(),
                        trustFact.getTrustManagers(), null);

        return sslContext;
    }

    public void run() {
        System.out.println("Client is connected to the library on " + host + ":" + port);
        System.out.println("Press (h)elp to view the available commands");

        Scanner scan = new Scanner(System.in);
        String input = "";
        while (scan.hasNextLine()) {
            input = scan.nextLine();
            try {
                if (input == null || startsWith(input, "q", "quit", false)) {
                    break;
                } else if (startsWith(input, "h", "help", false)) {
                    showHelp();
                } else if (startsWith(input, "s", "search", true)) {
                    String pattern = "*";
                    int parsedYear = -1;
                    String[] split = input.split("\\s+", 2);
                    if (split.length == 2) {
                        pattern = split[1];
                        if (pattern.length() == 4) { // can be a year
                            try {
                                parsedYear = Integer.parseInt(pattern);
                            } catch (NumberFormatException e) {}
                        }
                    }
                    
                    if (parsedYear > 0) {
                        lenderLogic.searchItems(clientSock.getInputStream(), clientSock.getOutputStream(), parsedYear);
                    } else {
                        lenderLogic.searchItems(clientSock.getInputStream(), clientSock.getOutputStream(), pattern);
                    }
                    printFoundItems();
                } else if (startsWith(input, "l", "list", false)) {
                    listBorrowedItems();
                } else if (startsWith(input, "b", "borrow", true)) {
                    String[] split = input.split("\\s+", 2);
                    int itemN = -1;
                    if (split.length == 2) {
                        try {
                            itemN = Integer.parseInt(split[1]);
                        } catch (NumberFormatException e) {}
                    }
                    int size = lenderLogic.getFoundItems() != null ? lenderLogic.getFoundItems().size() : -1;
                    if (itemN >= 0 && itemN < size) { // false when foundItems is null
                        LibraryItem libraryItem = lenderLogic.getFoundItems().get(itemN);
                        String status = lenderLogic.borrowItem(clientSock.getInputStream(), clientSock.getOutputStream(), libraryItem);
                        if (status == null) {
                            System.out.println("Item " + libraryItem + " borrowed");
                        } else {
                            System.out.println("Borrowing failed: " + status);
                        }
                    } else {
                        if (size > 0) {
                            System.out.println("You can borrow item" + 
                                    (size > 1 ? "s [0.." + (size-1) + "]" : " 0")
                                    + " from the list returned by search");
                        } else {
                            System.out.println("Found items list empty. Try searching first");
                        }
                    }
                } else if (startsWith(input, "r", "return", true)) {
                    String[] split = input.split("\\s+", 2);
                    int itemN = -1;
                    if (split.length == 2) {
                        try {
                            itemN = Integer.parseInt(split[1]);
                        } catch (NumberFormatException e) {}
                    }
                    int size = lenderLogic.getBorrowedItems() != null ? lenderLogic.getBorrowedItems().size() : -1;
                    if (itemN >= 0 && itemN < size) {
                        LibraryItem libraryItem = lenderLogic.getBorrowedItems().get(itemN);
                        String status = lenderLogic.returnItem(clientSock.getInputStream(), clientSock.getOutputStream(), libraryItem);
                        if (status == null) {
                            System.out.println("Item " + libraryItem + " returned");
                        } else {
                            System.out.println("Returning failed: " + status);
                        }
                    } else {
                        if (size > 0) {
                            System.out.println("You can return item" +
                                    (size > 1 ? "s [0.." + (size-1) + "]" : " 0")
                                    + " from the list of borrowed items");
                        } else {
                            System.out.println("Borrowed items list empty. Try borrowing something first");
                        }
                    }
                } else {
                    System.out.println("Command undefined");
                    System.out.println("Type (h)elp to view the available commands");
                }
            } catch (IOException e) {
                System.err.println("Failed to send or recieve query: " + e.getMessage());
            }
        }

        try {
            clientSock.close();
            System.out.println("Connection closed");
        } catch (Exception e) {
            log.warn("Failed to close the connection", e);
        }
    }

    private boolean startsWith(String input, String shortForm, String longForm, boolean hasArguments) {
        if (!hasArguments) {
            return shortForm.equalsIgnoreCase(input) || longForm.equalsIgnoreCase(input);
        } else {
            String lower = input.toLowerCase();
            return lower.startsWith(shortForm) || lower.startsWith(longForm);
        }
    }

    private void listBorrowedItems() {
        List<LibraryItem> borrowedItems = lenderLogic.getBorrowedItems();
        if (borrowedItems != null && borrowedItems.size() > 0) {
            int i = 0;
            for (LibraryItem item : borrowedItems) {
                System.out.println("[" + i + "]\t" + item.toString());
                i++;
            }
        } else {
            System.out.println("Nothing was borrowed");
        }
    }

    private void printFoundItems() {
        List<LibraryItem> foundItems = lenderLogic.getFoundItems();
        if (foundItems != null && foundItems.size() > 0) {
            int i = 0;
            for (LibraryItem item : foundItems) {
                System.out.println("[" + i + "]\t" + item.toString());
                i++;
            }
        } else {
            System.out.println("Nothing was found");
        }
    }

    private void showHelp() {
        System.out.println("(s)earch [text|year] \t - search the library for items");
        System.out.println("(b)orrow n           \t - borrow item n from the found items list");
        System.out.println("(r)eturn n           \t - return item n from the borrowed items list");
        System.out.println("(l)ist               \t - list borrowed items");
        System.out.println("(h)elp               \t - print this help");
        System.out.println("(q)uit               \t - disconnect and quit");
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            Utils.terminate("Usage: java LenderClient host port");
        }
        new LenderClient(args[0], args[1], true).run();
    }

}
