package ee.ut.cs.courses.appliedcrypto.judge;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;

import ee.ut.cs.courses.appliedcrypto.model.asn1.BorrowStatement;
import ee.ut.cs.courses.appliedcrypto.model.asn1.LibraryItem;
import ee.ut.cs.courses.appliedcrypto.model.asn1.ReturnStatement;
import ee.ut.cs.courses.appliedcrypto.util.Utils;

public class JudgeResolution {
    private static final Logger log = Logger.getLogger(JudgeResolution.class);
    
    private SignedData libraryEvidence;
    private SignedData clientEvidence;
    
    public JudgeResolution(String library, String client) {
        libraryEvidence = loadEvidence(library);
        if (libraryEvidence == null) {
            Utils.terminate("Failed to load library evidence");
        }
        
        clientEvidence = loadEvidence(client);
        if (clientEvidence == null) {
            Utils.terminate("Failed to load client evidence");
        }
    }
    
    public void resolve() {
        try {
            BorrowStatement borrowStatement = getBorrowStatement(libraryEvidence);
            ReturnStatement returnStatement = getReturnStatement(clientEvidence);
            
            LibraryItem borrowedBook = borrowStatement.getLibraryItem();
            LibraryItem returnedBook = returnStatement.getLibraryItem();
            if (borrowedBook.equals(returnedBook)) {
                Date outTime = borrowStatement.getOutTime();
                Date inTime = returnStatement.getInTime();
                if (outTime.before(inTime)) {
                    System.out.println("The lender returned the item.");
                } else {
                    System.out.println("The lender borrowed the item again after returning it. He must return it again.");
                }
            } else {
                System.out.println("The lender returned the wrong item. The lender should have returned '" + borrowedBook +"'.");
            }
        } catch (IOException e) {
            Utils.terminate("Failed to parse data", e);
        }
    }

    private SignedData loadEvidence(String library) {
        InputStream is = null;
        try {
            is = new FileInputStream(library);
            ASN1InputStream input = new ASN1InputStream(is);
            return SignedData.getInstance(input.readObject());
        } catch (IOException e) {
            log.warn("IO problem", e);
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.warn("IO problem", e);
                }
            }
        }
    }
    
    private ReturnStatement getReturnStatement(SignedData signedData) throws IOException {
        ContentInfo encapContentInfo = signedData.getEncapContentInfo();
        DEROctetString content = (DEROctetString) encapContentInfo.getContent();
        ASN1Object seq = ASN1Sequence.fromByteArray(content.getOctets());
        return new ReturnStatement((ASN1Sequence) seq);
    }
    
    private BorrowStatement getBorrowStatement(SignedData signedData) throws IOException {
        ContentInfo encapContentInfo = signedData.getEncapContentInfo();
        DEROctetString content = (DEROctetString) encapContentInfo.getContent();
        ASN1Object seq = ASN1Sequence.fromByteArray(content.getOctets());
        return new BorrowStatement( (ASN1Sequence) seq );
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            Utils.terminate("Usage: java JudgeResolution libraryEvidece lenderEvidence");
        }
        new JudgeResolution(args[0], args[1]).resolve();
    }

}
