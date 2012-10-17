package ee.ut.cs.courses.appliedcrypto.common.logic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSSignedGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;

import ee.ut.cs.courses.appliedcrypto.model.asn1.Identifiers;

public class CommonLogicImpl implements CommonLogic {
    private static final Logger log = Logger.getLogger(CommonLogicImpl.class);

    private static final String CA_ROOT_ALIAS = "root";
    private static final String CRL_FILE = "ca/crl/ca.crl";

    private X509Certificate rootCert;
    private X509CRL crl;
    private CMSSignedDataGenerator generator;

    public CommonLogicImpl(String keyStoreFile, String alias, char[] password) {
        KeyStore keystore = getKeystore(keyStoreFile, password);
        generator = getSignedDataGenerator(keystore, password, alias);
        try {
            rootCert = (X509Certificate) keystore.getCertificate(CA_ROOT_ALIAS);
        } catch (KeyStoreException e) {
            log.error("Failed to load root certificate", e);
            throw new RuntimeException(e);
        }
        crl = getCrl();
    }

    private KeyStore getKeystore(String keystoreFile, char[] password) {
        InputStream input = null;
        KeyStore keystore = null;
        try {
            keystore = KeyStore.getInstance("JKS");
            input = new FileInputStream(keystoreFile);
            keystore.load(input, password);
            input.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load keystore", e);
        }
        return keystore;
    }

    private CMSSignedDataGenerator getSignedDataGenerator(KeyStore keystore, char[] password, String keystoreAlias) {
        try {
            CMSSignedDataGenerator generator = new CMSSignedDataGenerator();

            PrivateKey key = (PrivateKey) keystore.getKey(keystoreAlias, password);
            X509Certificate certificate = (X509Certificate) keystore.getCertificate(keystoreAlias);

            List<Object> list = new ArrayList<Object>();
            list.add(certificate);
            CollectionCertStoreParameters params = new CollectionCertStoreParameters(list);
            CertStore store = CertStore.getInstance("Collection", params, "BC");

            generator.addSigner(key, certificate, CMSSignedGenerator.DIGEST_SHA1);
            generator.addCertificatesAndCRLs(store);

            return generator;
        } catch (Exception e) {
            log.info("Failed to create a signed data generator", e);
            throw new RuntimeException(e);
        }
    }

    public void writeData(OutputStream outputStream, ASN1Encodable obj) throws IOException {
        outputStream.write(obj.getEncoded());
    }

    public DERObject readData(InputStream inputStream) throws IOException {
        ASN1InputStream input = new ASN1InputStream(inputStream);
        return input.readObject();
    }

    public CMSSignedData createSignedData(ASN1Encodable borrowStatement) {
        CMSSignedData data = null;
        try {
            CMSProcessable content = new CMSProcessableByteArray(borrowStatement.getEncoded());
            data = generator.generate(Identifiers.borrowStatement.toString(), content, true, "BC");
        } catch (Exception e) {
            log.info("Failed to generate CMS signed data", e);
        }
        return data;
    }
    
    private SignerInformation getSignedInfo(CMSSignedData cmsSignedData) {
        SignerInformationStore signerInfos = cmsSignedData.getSignerInfos();
        Collection<?> signers = signerInfos.getSigners();
        return (SignerInformation) signers.iterator().next();
    }
    
    public SignerInformation getSignedInfo(SignedData signedData) {
        CMSSignedData cmsSignedData = new CMSSignedData(new ContentInfo(CMSObjectIdentifiers.signedData, signedData));
        return getSignedInfo(cmsSignedData);
    }

    public String verifySignedData(SignedData signedData, Identifiers id) {
        ContentInfo encapContentInfo = signedData.getEncapContentInfo();
        if (encapContentInfo == null || encapContentInfo.getContentType() == null) {
            return "SignedData encapContentInfo is not complete";
        }
        if (id.stringEquals(encapContentInfo.getContentType().toString()) == false) {
            return "Content identifier does not match the expected \"" + id.toString() + "\"";
        }

        String error = null;
        CMSSignedData cmsSignedData = new CMSSignedData(new ContentInfo(CMSObjectIdentifiers.signedData, signedData));
        try {
            CertStore certStore = cmsSignedData.getCertificatesAndCRLs("Collection", "BC");
            SignerInformation signerInfo = getSignedInfo(cmsSignedData);
            
            Collection<? extends Certificate> certificates = certStore.getCertificates(signerInfo.getSID());
            X509Certificate cert = (X509Certificate) certificates.iterator().next();
            if (signerInfo.verify(cert, "BC") == false) {
                error = "Signature verification failed";
            }

            error = verifyByCrl(cert);
        } catch (NoSuchAlgorithmException e) {
            log.info("Algorithm not supported", e);
            error = "Signature verification failed. Algorithm not supported.";
        } catch (CMSException e) {
            log.error("CMS exception occured", e);
            error = "Internal error";
        } catch (NoSuchProviderException e) {
            log.error("Security provider not installed", e);
            error = "Internal error";
        } catch (CertStoreException e) {
            log.error("Cert Store problem", e);
            error = "Signature verification failed";
        } catch (CertificateExpiredException e) {
            log.info("Certificate has expired", e);
            error = "Signature verification failed. Certificate has expired";
        } catch (CertificateNotYetValidException e) {
            log.info("Certificate not yet valid", e);
            error = "Signature verification failed. Certificate not yet valid";
        }

        return error;
    }

    /**
     * Don't check the root cert because it comes from the local keystore. Assume it is correct.
     * Client cert validity should be checked before, not checking here.
     */
    private String verifyByCrl(X509Certificate cert) {
        String error = null;
        try {
            // Can the cert be used for signing
            boolean[] usages = cert.getKeyUsage();
            if (usages != null) {
                // digitalSignature(0) and non-repudiation(1)
                if (usages[0] == false && usages[1] == false) {
                    error = "Certificate cannot be used for signing data";
                }
            } else {
                error = "Certificate does not have the KeyUsage extension";
            }

            if (error == null) {
                // check if cert was signed by the CA
                cert.verify(rootCert.getPublicKey());
            }
        } catch (InvalidKeyException e) {
            error = "Verification failed. Certificate was not signed by the same CA";
            log.warn(error, e);
        } catch (CertificateException e) {
            error = "Verification failed. Internal error";
            log.error(error, e);
        } catch (NoSuchAlgorithmException e) {
            error = "Verification failed. Internal error";
            log.error(error, e);
        } catch (NoSuchProviderException e) {
            error = "Verification failed. Internal error";
            log.error(error, e);
        } catch (SignatureException e) {
            error = "Verification failed. Internal error";
            log.error(error, e);
        }
            
        // verify by crl
        if (crl == null) {
            log.warn("Cannot check by CRL certificate from " + cert.getSubjectX500Principal() + ". It may be revoked");
        }
        else if (crl != null && error == null) {
            try {
                // verify crl with CA key
                crl.verify(rootCert.getPublicKey());

                // check update date
                Date update = crl.getNextUpdate();
                if (update != null && update.before(new Date())) {
                    error = "Cannot verify the certificate with CRL. CRL has expired on " + update;
                }
                // check if cert is revoked
                if (error == null && crl.isRevoked(cert)) {
                    X509CRLEntry entry = crl.getRevokedCertificate(cert.getSerialNumber());
                    error = "Verification failed. Certificate was revoked on " + entry.getRevocationDate();
                }
            } catch (CRLException e) {
                error = "Cannot verify the certificate with CRL. Internal error";
                log.error(error, e);
            } catch (InvalidKeyException e) {
                error = "Cannot verify the certificate with CRL. CRL was not signed by the same CA";
                log.warn(error, e);
            } catch (NoSuchAlgorithmException e) {
                error = "Cannot verify the certificate with CRL. Internal error";
                log.error(error, e);
            } catch (NoSuchProviderException e) {
                error = "Cannot verify the certificate with CRL. Internal error";
                log.error(error, e);
            } catch (SignatureException e) {
                error = "Cannot verify the certificate with CRL. Internal error";
                log.error(error, e);
            }
        }
        return error;
    }

    private X509CRL getCrl() {
        X509CRL crl = null;
        InputStream crlInput = null;
        try {
            crlInput = new FileInputStream(CRL_FILE);
            CertificateFactory cert_factory = CertificateFactory.getInstance("X.509", "BC");
            crl = (X509CRL) cert_factory.generateCRL(crlInput);
        } catch (Exception e) {
            log.warn("Failed to load CRL file. It will not be used during verification", e);
        }
        finally {
            if (crlInput != null) {
                try {
                    crlInput.close();
                } catch (IOException e) {
                    log.warn("Failed to close crl input stream", e);
                }
            }
        }
        return crl;
    }
}
