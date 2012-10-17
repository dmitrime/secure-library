package ee.ut.cs.courses.appliedcrypto.util;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;

public class TrustKeystore {

    private static String DEFAULT_KEYSTORE = "trust.jks";
    private static String DEFAULT_KEYPASS = "password";
    private static String DEFAULT_LENDER_ALIAS = "lender";
    private static String DEFAULT_LIBRARY_ALIAS = "library";
    private static String DEFAULT_ROOT_ALIAS = "root";

    public TrustKeystore() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String args[]) {
        if (args.length < 3) {
            System.out.println("Usage: java Keystore CAcert.crt lenderCertFile.crt libraryCertFile.crt [keystorePassword output.jks]");
            System.exit(0);
        } else {
            String password = args.length > 3 ? args[3] : DEFAULT_KEYPASS;
            String output = args.length > 4 ? args[4] : DEFAULT_KEYSTORE;
            new TrustKeystore().createKeystore(args[0], args[1], args[2], password, output);
        }
    }

    private void createKeystore(String rootFile, String lenderFile, String libraryFile,
                                String keystorePassword, String output) 
    {
        try {
            PEMReader pr3 = new PEMReader(new FileReader(rootFile));
            X509Certificate rootCert = (X509Certificate) pr3.readObject();
            
            PEMReader pr2 = new PEMReader(new FileReader(lenderFile));
            X509Certificate lenderCert = (X509Certificate) pr2.readObject();
            
            PEMReader pr1 = new PEMReader(new FileReader(libraryFile));
            X509Certificate libraryCert = (X509Certificate) pr1.readObject();
            
            // create empty key store
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(null, keystorePassword.toCharArray());
            
            ks.setCertificateEntry(DEFAULT_ROOT_ALIAS, rootCert);
            ks.setCertificateEntry(DEFAULT_LENDER_ALIAS, lenderCert);
            ks.setCertificateEntry(DEFAULT_LIBRARY_ALIAS, libraryCert);
            
            ks.store(new FileOutputStream(output), keystorePassword.toCharArray());

            System.out.println("Certificates stored to " + output + " with aliases \"" 
                    + DEFAULT_ROOT_ALIAS + "\", \"" + DEFAULT_LENDER_ALIAS + "\", " + DEFAULT_LIBRARY_ALIAS + "\"");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
