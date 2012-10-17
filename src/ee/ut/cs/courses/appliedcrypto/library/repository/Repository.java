package ee.ut.cs.courses.appliedcrypto.library.repository;

import java.util.Date;
import java.util.List;

import org.bouncycastle.cms.SignerInformation;

import ee.ut.cs.courses.appliedcrypto.model.asn1.BorrowStatement;
import ee.ut.cs.courses.appliedcrypto.model.asn1.LibraryItem;

public interface Repository {
    public List<LibraryItem> getAllItems();
    
    public List<LibraryItem> searchItems(int year);
    
    public List<LibraryItem> searchItems(String text);

    public String borrowItem(LibraryItem libraryItem, Date outTime, BorrowStatement borrowStatement, SignerInformation signedInfo);

    public String returnItem(LibraryItem libraryItem);
}
