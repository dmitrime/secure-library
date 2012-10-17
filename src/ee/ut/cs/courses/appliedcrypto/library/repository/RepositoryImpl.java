package ee.ut.cs.courses.appliedcrypto.library.repository;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformation;

import ee.ut.cs.courses.appliedcrypto.model.asn1.Book;
import ee.ut.cs.courses.appliedcrypto.model.asn1.BorrowStatement;
import ee.ut.cs.courses.appliedcrypto.model.asn1.LibraryItem;
import ee.ut.cs.courses.appliedcrypto.model.asn1.Magazine;

public class RepositoryImpl implements Repository {
    private static final Logger log = Logger.getLogger(RepositoryImpl.class);
    
    public static final String BOOKS = "shelf/books.txt";
    public static final String MAGAZINES = "shelf/magazines.txt";
    public static final String SEPARATOR = "\\|";

    private List<LibraryItem> items = new ArrayList<LibraryItem>();
    private Map<LibraryItem, SignerId> toBeReturned = new HashMap<LibraryItem, SignerId>();

    public RepositoryImpl() {
        loadLibraryItems();
    }

    @Override
    public List<LibraryItem> getAllItems() {
        return items;
    }

    @Override
    public List<LibraryItem> searchItems(int year) {
        List<LibraryItem> found = new ArrayList<LibraryItem>();
        for (LibraryItem item : items) {
            if (item.getSearchable().issuedIn(year)) {
                found.add(item);
            }
        }
        return found;
    }

    @Override
    public List<LibraryItem> searchItems(String text) {
        if ("*".equals(text)) {
            return getAllItems();
        }
        List<LibraryItem> found = new ArrayList<LibraryItem>();
        for (LibraryItem item : items) {
            if (item.getSearchable().contains(text)) {
                found.add(item);
            }
        }
        return found;
    }

    @Override
    public String borrowItem(LibraryItem libraryItem, Date outTime, BorrowStatement borrowStatement, SignerInformation signedInfo) {
        if (log.isDebugEnabled()) {
            log.debug("Someone is trying to borrow item '" + libraryItem + "' on " + outTime);
        }
        if (items.contains(libraryItem)) {
            toBeReturned.put(libraryItem, signedInfo.getSID());
            items.remove(libraryItem);
        } else {
            if (toBeReturned.containsKey(libraryItem)) {
                log.debug("Item already borrowed: " + libraryItem);
                return "The item '" + libraryItem + "' is already borrowed"; 
            } else {
                log.debug("No such item: " + libraryItem);
                return "Item '" + libraryItem + "' does not exist in the library";
            }
        }
        return null;
    }
    

    @Override
    public String returnItem(LibraryItem libraryItem) {
        if (log.isDebugEnabled()) {
            log.debug("Someone is trying to return item '" + libraryItem + "'");
        }
        if (toBeReturned.containsKey(libraryItem)) {
            // assume that we do not need to check who exactly returns the item
            toBeReturned.remove(libraryItem);
            items.add(libraryItem);
        } else {
            if (items.contains(libraryItem)) {
                log.debug("Item was not borrowed: " + libraryItem);
                return "The item '" + libraryItem + "' was not borrowed";
            } else {
                log.debug("No such item: " + libraryItem);
                return "Item '" + libraryItem + "' does not exist in the library";                
            }
        }
        return null;
    }

    private void loadLibraryItems() {
        items.addAll(loadBooks());
        items.addAll(loadMagazines());
    }

    private Collection<? extends LibraryItem> loadBooks() {
        List<LibraryItem> books = new ArrayList<LibraryItem>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(BOOKS));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(SEPARATOR);
                if (data.length == 4) {
                    int year = 0;
                    try {
                        year = Integer.parseInt(data[3]);
                    } catch (NumberFormatException e) { log.error(e); }
                    LibraryItem item = new LibraryItem(new Book(data[0], data[1], data[2], year));
                    books.add(item);
                }
            }
        } catch (FileNotFoundException e) {
            log.error("File not found: " + BOOKS, e);
        } catch (IOException e) {
            log.error(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) { log.error(e); }
            }
        }
        if (log.isInfoEnabled()) {
            log.info(books.size() + " books loaded");
        }
        return books;
    }

    private Collection<? extends LibraryItem> loadMagazines() {
        List<LibraryItem> magazines = new ArrayList<LibraryItem>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(MAGAZINES));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(SEPARATOR);
                if (data.length == 3) {
                    int year = 0;
                    int issue = 0;
                    try {
                        year = Integer.parseInt(data[1]);
                        issue = Integer.parseInt(data[2]);
                    } catch (NumberFormatException e) { log.error(e); }
                    LibraryItem item = new LibraryItem(new Magazine(data[0], year, issue));
                    magazines.add(item);
                }
            }
        } catch (FileNotFoundException e) {
            log.error("File not found: " + MAGAZINES, e);
        } catch (IOException e) {
            log.error(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) { log.error(e); }
            }
        }
        if (log.isInfoEnabled()) {
            log.info(magazines.size() + " magazines loaded");
        }
        return magazines;
    }
}
