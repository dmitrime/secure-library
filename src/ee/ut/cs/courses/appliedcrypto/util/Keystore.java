package ee.ut.cs.courses.appliedcrypto.util;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

public class Keystore {

    private static String DEFAULT_KEYPASS = "password";
    private static String DEFAULT_ALIAS = "imported";
    private static String DEFAULT_KEYSTORE = "keystore.jks";
    private static String DEFAULT_ROOT_ALIAS = "root";

    public Keystore() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String args[]) {
        if (args.length < 3) {
            System.out.println("Usage: java Keystore privateKey.pem certFile.crt keyPassword [alias output.jks keystorePassword CAcert.crt]");
            System.exit(0);
        } else {
            String alias = args.length > 3 ? args[3] : DEFAULT_ALIAS;
            String output = args.length > 4 ? args[4] : DEFAULT_KEYSTORE;
            String password = args.length > 5 ? args[5] : DEFAULT_KEYPASS;
            String rootFile = args.length > 6 ? args[6] : null;
            new Keystore().createKeystore(args[0], args[1], args[2], alias, output, password, rootFile);
        }
    }

    private void createKeystore(String privKeyFile, String certFile, final String keyPassword, 
                                String alias, String output, final String keystorePassword, String rootFile) 
    {
        try {
            // loading Key
            PEMReader pr1 = new PEMReader(new FileReader(privKeyFile), new PasswordFinder() {
                public char[] getPassword() {
                    return keyPassword.toCharArray();
                }
            });
            KeyPair pair = (KeyPair) pr1.readObject();
            PEMReader pr2 = new PEMReader(new FileReader(certFile));
            X509Certificate cert = (X509Certificate) pr2.readObject();
            
            // create empty key store
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(null, keystorePassword.toCharArray());
            
            // when there's a root, add it to the chain and also make a trusted entry
            if (rootFile != null) {
                PEMReader pr3 = new PEMReader(new FileReader(rootFile));
                X509Certificate rootCert = (X509Certificate) pr3.readObject();
                ks.setKeyEntry(alias, pair.getPrivate(), keystorePassword.toCharArray(), new Certificate[] { cert, rootCert });
                ks.setCertificateEntry(DEFAULT_ROOT_ALIAS, rootCert);
            } else {
                ks.setKeyEntry(alias, pair.getPrivate(), keystorePassword.toCharArray(), new Certificate[] { cert });
            }
            
            ks.store(new FileOutputStream(output), keystorePassword.toCharArray());

            System.out.println("Key and certificate stored to " + output + " with alias \"" + alias + "\"");
            if (rootFile != null) {
                System.out.println("The root certificate has been added to the chain " +
                		"and also saved as a trusted entry with alias \"" + DEFAULT_ROOT_ALIAS + "\"");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
