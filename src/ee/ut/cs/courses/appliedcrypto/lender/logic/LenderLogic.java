package ee.ut.cs.courses.appliedcrypto.lender.logic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import ee.ut.cs.courses.appliedcrypto.model.asn1.LibraryItem;


public interface LenderLogic {
    public void searchItems(InputStream inputStream, OutputStream outputStream, String text) throws IOException;
    
    public void searchItems(InputStream inputStream, OutputStream outputStream, int year) throws IOException;
    
    public String borrowItem(InputStream inputStream, OutputStream outputStream, LibraryItem libraryItem) throws IOException;
    
    public String returnItem(InputStream inputStream, OutputStream outputStream, LibraryItem libraryItem) throws IOException;

    public List<LibraryItem> getFoundItems();

    public List<LibraryItem> getBorrowedItems();
}
