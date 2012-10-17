package ee.ut.cs.courses.appliedcrypto.library.logic;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import ee.ut.cs.courses.appliedcrypto.library.repository.Repository;
import ee.ut.cs.courses.appliedcrypto.library.repository.RepositoryImpl;
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

public class LibraryLogicImpl implements LibraryLogic {
    private static final Logger log = Logger.getLogger(LibraryLogicImpl.class);
    
    private static final String SIGN_KEYSTORE = "keystore/library-sign.jks";
    private static final char[] SIGN_KEYSTORE_PASSWORD = "password".toCharArray();
    private static final String SIGN_KEYSTORE_ALIAS = "library-sign";
    private static final String EVIDENCE_FILE_LIBRARY = "evidence/library.evidence";

    private CommonLogicImpl commonLogic;
    private Repository repository;
    
    public LibraryLogicImpl() {
        commonLogic = new CommonLogicImpl(SIGN_KEYSTORE, SIGN_KEYSTORE_ALIAS, SIGN_KEYSTORE_PASSWORD);
        repository = new RepositoryImpl();
    }
    
    @Override
    public LenderQuery readQuery(InputStream inputStream) throws IOException {
        LenderQuery query = LenderQuery.getInstance(readLenderQuery(inputStream));
        if (log.isDebugEnabled()) {
            log.debug("Recieved query: " + query);
        }
        return query;
    }
    
    @Override
    public void processQuery(LenderQuery query, OutputStream outputStream) throws IOException {
        if (query.getType() == LenderQuery.SEARCH_QUERY) {
            SearchQuery searchQuery = query.getSearchQuery();
            processSearchQuery(searchQuery, outputStream);
        } else if (query.getType() == LenderQuery.BORROW_QUERY) {
            BorrowQuery borrowQuery = query.getBorrowQuery();
            processBorrowQuery(borrowQuery, outputStream);
        } else if (query.getType() == LenderQuery.RETURN_QUERY) {
            ReturnQuery returnQuery = query.getReturnQuery();
            processReturnQuery(returnQuery, outputStream);
        } else {
            // Unknown query
            writeFailAnswer("The type (" + query.getType() + ") of the query is undefined", outputStream);
            throw new IllegalArgumentException("Unkown query type: " + query.getType());
        }
    }

    private void processSearchQuery(SearchQuery searchQuery, OutputStream outputStream) throws IOException {
        if (Identifiers.yearQuery.stringEquals(searchQuery.getSearchFormat())) {
            int year = searchQuery.getIntSearchContent();
            List<LibraryItem> searchItems = repository.searchItems(year);
            if (log.isDebugEnabled()) {
                log.debug(searchItems.size() + " items found for year search query \"" + year + "\"");
            }
            SearchAnswer searchAnswer = new SearchAnswer(searchItems);
            
            writeLenderAnswer(outputStream, new LenderAnswer(searchAnswer));
            
        } else if (Identifiers.textQuery.stringEquals(searchQuery.getSearchFormat())) {
            String text = searchQuery.getStringSearchContent();
            if (text == null || text.isEmpty()) {
                writeFailAnswer("The search query is empty", outputStream);
            } else {
                List<LibraryItem> searchItems = repository.searchItems(text);
                if (log.isDebugEnabled()) {
                    log.debug(searchItems.size() + " items found for text search query \"" + text + "\"");
                }
                SearchAnswer searchAnswer = new SearchAnswer(searchItems);
                
                writeLenderAnswer(outputStream, new LenderAnswer(searchAnswer));
            }
            
        } else {
            writeFailAnswer("The search query format (" + searchQuery.getSearchFormat() + ") is undefined", outputStream);
            throw new IllegalArgumentException("Unkown search query type: " + searchQuery.getSearchFormat());
        }
    }
    
    private void processBorrowQuery(BorrowQuery borrowQuery, OutputStream outputStream) throws IOException {
        SignedData signedData = borrowQuery.getSignedData();
        String error = commonLogic.verifySignedData(signedData, Identifiers.borrowStatement);
        if (error == null) {
            Date outTime = borrowQuery.getOutTime();
            LibraryItem libraryItem = borrowQuery.getLibraryItem();
            BorrowStatement borrowStatement = getBorrowStatement(signedData);
            
            // save the evidence
            saveLibraryEvidence(signedData);
            
            // borrow and save the borrowing event
            String status = repository.borrowItem(libraryItem, outTime, borrowStatement, commonLogic.getSignedInfo(signedData));
            if (status == null) {
                BorrowAnswer borrowAnswer = new BorrowAnswer();
                writeLenderAnswer(outputStream, new LenderAnswer(borrowAnswer));
            } else {
                writeFailAnswer(status, outputStream);
            }
        } else {
            writeFailAnswer(error, outputStream);
        }
    }
    
    private void processReturnQuery(ReturnQuery returnQuery, OutputStream outputStream) throws IOException {
        LibraryItem libraryItem = returnQuery.getLibraryItem();
        
        // return the borrowed item to the repository
        String status = repository.returnItem(libraryItem);
        
        if (status == null) {
            Date inTime = new Date();
            ReturnStatement returnStatement = new ReturnStatement(libraryItem, inTime);
            CMSSignedData data = commonLogic.createSignedData(returnStatement);
            if (data != null) {
                ReturnAnswer returnAnswer = new ReturnAnswer(data.getContentInfo().getContent());

                writeLenderAnswer(outputStream, new LenderAnswer(returnAnswer));
            } else {
                log.warn("SignedData is null. Skipping answer");
                writeFailAnswer("Internal error", outputStream);
            }
        } else {
            log.warn("Return failed: " + status);
            writeFailAnswer(status, outputStream);
        }
    }
    
    private void saveLibraryEvidence(SignedData signedData) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(EVIDENCE_FILE_LIBRARY);
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
    
    private BorrowStatement getBorrowStatement(SignedData signedData) throws IOException {
        ContentInfo encapContentInfo = signedData.getEncapContentInfo();
        DEROctetString content = (DEROctetString) encapContentInfo.getContent();
        ASN1Object seq = ASN1Sequence.fromByteArray(content.getOctets());
        return new BorrowStatement( (ASN1Sequence) seq );
    }
    
    private void writeFailAnswer(String msg, OutputStream outputStream) throws IOException {
        writeLenderAnswer(outputStream, new LenderAnswer(msg));
    }

    private DERObject readLenderQuery(InputStream inputStream) throws IOException {
        return commonLogic.readData(inputStream);
    }
    
    private void writeLenderAnswer(OutputStream outputStream, ASN1Encodable obj) throws IOException {
        commonLogic.writeData(outputStream, obj);
    }
}
