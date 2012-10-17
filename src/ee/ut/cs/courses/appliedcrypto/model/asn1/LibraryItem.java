package ee.ut.cs.courses.appliedcrypto.model.asn1;

import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERTaggedObject;

import ee.ut.cs.courses.appliedcrypto.model.Searchable;

/**
 * Encapsulates the LibraryItem structure.
 * 
 * <pre>
 * LibraryItem ::= CHOICE {
 *   book [0] {@link Book},
 *   magazine [1] {@link Magazine}
 * </pre>
 */
public class LibraryItem extends ASN1Encodable implements ASN1Choice {
    /** Tag for the book field. */
    public static final int BOOK = 0;
    /** Tag for the magazine field. */
    public static final int MAGAZINE = 1;

    /** Whether we use explicit (as opposed to implicit) tagging. */
    private static final boolean EXPLICIT = true;

    private int tag;
    /** Type of value is determined by tag field. */
    private ASN1Encodable value;

    /** Create a book. */
    public LibraryItem(Book book) {
        tag = BOOK;
        value = book;
    }

    /** Create a magazine. */
    public LibraryItem(Magazine magazine) {
        tag = MAGAZINE;
        value = magazine;
    }

    /**
     * Returns type of the contents.
     */
    public int getType() {
        return tag;
    }

    /** Returns book contents. */
    public Book getBook() {
        assert tag == BOOK;

        return (Book) value;
    }

    /** Returns magazine contents. */
    public Magazine getMagazine() {
        assert tag == MAGAZINE;

        return (Magazine) value;
    }

    public Searchable getSearchable() {
        return (Searchable) value;
    }

    /**
     * This method is part of the getInstance pattern that is used
     * with tagged objects.
     * It takes as input an ASN.1 object and tries to construct {@link LibraryItem} from it.
     */
    public static LibraryItem getInstance(Object obj) {
        // Maybe everything is fine?
        if (obj == null || obj instanceof LibraryItem) {
            return (LibraryItem) obj;
        }

        if (obj instanceof ASN1TaggedObject) {
            ASN1TaggedObject tagObj = (ASN1TaggedObject) obj;

            // Check the tag to see which option it is.
            switch (tagObj.getTagNo()) {
            case BOOK:
                return new LibraryItem(
                            Book.getInstance(tagObj, EXPLICIT));
            case MAGAZINE:
                return new LibraryItem(
                            Magazine.getInstance(tagObj, EXPLICIT));
            default:
                throw new IllegalArgumentException(
                            "Unknown tag: " + tagObj.getTagNo());
            }
        }

        // Cannot parse anything but tagged objects.
        throw new IllegalArgumentException("Unknown object in getInstance: "
                + obj.getClass().getName());
    }

    /** Encodes the contents as a tagged value. */
    public DERObject toASN1Object() {
        return new DERTaggedObject(EXPLICIT, tag, value);
    }

    public String toString() {
        return value.toString();
    }
}
