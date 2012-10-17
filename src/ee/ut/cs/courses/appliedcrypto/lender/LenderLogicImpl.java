package ee.ut.cs.courses.appliedcrypto.lender;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.cms.CMSSignedData;

import ee.ut.cs.courses.appliedcrypto.common.logic.CommonLogicImpl;
import ee.ut.cs.courses.appliedcrypto.lender.logic.LenderLogic;
import ee.ut.cs.courses.appliedcrypto.model.asn1.BorrowAnswer;
import ee.ut.cs.courses.appliedcrypto.model.asn1.BorrowQuery;
import ee.ut.cs.courses.appliedcrypto.model.asn1.BorrowStatement;
import ee.ut.cs.courses.appliedcrypto.model.asn1.Identifiers;
import ee.ut.cs.courses.appliedcrypto.model.asn1.LenderAnswer;
import ee.ut.cs.courses.appliedcrypto.model.asn1.LenderQuery;
import ee.ut.cs.courses.appliedcrypto.model.asn1.LibraryItem;
import ee.ut.cs.courses.appliedcrypto.model.asn1.ReturnAnswer;
import ee.ut.cs.courses.appliedcrypto.model.asn1.ReturnQuery;
import ee.ut.cs.courses.appliedcrypto.model.asn1.ReturnStatement;
import ee.ut.cs.courses.appliedcrypto.model.asn1.SearchAnswer;
import ee.ut.cs.courses.appliedcrypto.model.asn1.SearchQuery;

public class LenderLogicImpl implements LenderLogic {
    private static final Logger log = Logger.getLogger(LenderLogicImpl.class);
    
    private static final String SIGN_KEYSTORE = "keystore/lender-sign.jks";
//    private static final String SIGN_KEYSTORE = "test/test-lender.jks";
    private static final char[] SIGN_KEYSTORE_PASSWORD = "password".toCharArray();
    private static final String SIGN_KEYSTORE_ALIAS = "lender-sign";
    private static final String EVIDENCE_FILE_LENDER = "evidence/lender.evidence";
    
    private CommonLogicImpl commonLogic;
    private List<LibraryItem> foundItems;
    private List<LibraryItem> borrowedItems = new ArrayList<LibraryItem>();

    public LenderLogicImpl() {
        commonLogic = new CommonLogicImpl(SIGN_KEYSTORE, SIGN_KEYSTORE_ALIAS, SIGN_KEYSTORE_PASSWORD);
    }

    @Override
    public void searchItems(InputStream inputStream, OutputStream outputStream, String text) throws IOException {
        SearchQuery searchQuery = new SearchQuery(Identifiers.textQuery.toString(), text);
        searchItems(inputStream, outputStream, searchQuery);
    }
    
    @Override
    public void searchItems(InputStream inputStream, OutputStream outputStream, int year) throws IOException {
        SearchQuery searchQuery = new SearchQuery(Identifiers.yearQuery.toString(), year);
        searchItems(inputStream, outputStream, searchQuery);
    }
    
    @Override
    public List<LibraryItem> getFoundItems() {
        return foundItems;
    }
    
    @Override
    public List<LibraryItem> getBorrowedItems() {
        return borrowedItems;
    }

    private void searchItems(InputStream inputStream, OutputStream outputStream, SearchQuery searchQuery) throws IOException {
        writeLenderQuery(outputStream, new LenderQuery(searchQuery));
        
        LenderAnswer answer = LenderAnswer.getInstance(readLenderAnswer(inputStream));
        if (log.isDebugEnabled()) {
            log.debug("Answer: " + answer);
        }
        
        if (answer.getType() == LenderAnswer.SEARCH) {
            SearchAnswer searchAnswer = answer.getSearchAnswer();
            foundItems = searchAnswer.getItems();
        } else {
            if (answer.getType() == LenderAnswer.FAIL) {
                System.err.println("Search failed: " + answer.getFailAnswer().toString());
                if (log.isDebugEnabled()) {
                    log.debug("FAIL answer recieved: " + answer.getFailAnswer().toString());
                }
            } else {
                throw new IllegalArgumentException("Unexpected borrow answer type: " + answer.getType());
            }
            foundItems = null;
        }
    }

    @Override
    public String borrowItem(InputStream inputStream, OutputStream outputStream, LibraryItem libraryItem) throws IOException {
        Date outTime = new Date();
        BorrowStatement borrowStatement = new BorrowStatement(libraryItem, outTime);
        
        CMSSignedData data = commonLogic.createSignedData(borrowStatement);
        if (data != null) {
            BorrowQuery borrowQuery = new BorrowQuery(libraryItem, outTime, data.getContentInfo().getContent());
            writeLenderQuery(outputStream, new LenderQuery(borrowQuery));

            LenderAnswer answer = LenderAnswer.getInstance(readLenderAnswer(inputStream));
            if (log.isDebugEnabled()) {
                log.debug("Answer: " + answer);
            }
            
            if (answer.getType() == LenderAnswer.BORROW) {
                BorrowAnswer borrowAnswer = answer.getBorrowAnswer();
                if (borrowAnswer.isOk()) {
                    borrowedItems.add(libraryItem);
                    foundItems.clear();
                }
            } else {
                if (answer.getType() == LenderAnswer.FAIL) {
                    if (log.isDebugEnabled()) {
                        log.debug("FAIL answer recieved: " + answer.getFailAnswer().toString());
                    }
                    return answer.getFailAnswer().toString();
                } else {
//                    throw new IllegalArgumentException("Unexpected borrow answer type: " + answer.getType());
                    log.warn("Unexpected borrow answer type: " + answer.getType());
                    return "Unexpected answer recieved";
                }
            }
        } else {
            log.warn("SignedData is null. Skipping query");
            return "Internal error";
        }
        return null;
    }

    @Override
    public String returnItem(InputStream inputStream, OutputStream outputStream, LibraryItem libraryItem) throws IOException {
        ReturnQuery returnQuery = new ReturnQuery(libraryItem);
        
        writeLenderQuery(outputStream, new LenderQuery(returnQuery));
        LenderAnswer answer = LenderAnswer.getInstance(readLenderAnswer(inputStream));
        
        if (log.isDebugEnabled()) {
            log.debug("Answer: " + answer);
        }
        
        if (answer.getType() == LenderAnswer.RETURN) {
            ReturnAnswer returnAnswer = answer.getReturnAnswer();
            ReturnStatement returnStatement = getReturnStatement(returnAnswer.getSignedData());
            log.debug("Return statement: " + returnStatement);
            
            // save the evidence
            saveLenderEvidence(returnAnswer.getSignedData());
            
            borrowedItems.remove(libraryItem);
        } else {
            if (answer.getType() == LenderAnswer.FAIL) {
                log.warn("Fail answer recieved: \"" + answer.getFailAnswer().toString() + "\"");
                return answer.getFailAnswer().toString();
            } else {
//                throw new IllegalArgumentException("Unexpected return answer type: " + answer.getType());
                log.warn("Unexpected return answer type: " + answer.getType());
                return "Unexpected answer recieved";
            }
        }
        return null;
    }
    
    private void saveLenderEvidence(SignedData signedData) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(EVIDENCE_FILE_LENDER);
            os.write(signedData.getEncoded());
        } catch (IOException e) {
            log.warn("IO problem", e);
        } finally {
            if (os != null) {
                try {
                    os.close();
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
    
    private void writeLenderQuery(OutputStream outputStream, ASN1Encodable obj) throws IOException {
        commonLogic.writeData(outputStream, obj);
    }
    
    private DERObject readLenderAnswer(InputStream inputStream) throws IOException {
        return commonLogic.readData(inputStream);
    }
}
