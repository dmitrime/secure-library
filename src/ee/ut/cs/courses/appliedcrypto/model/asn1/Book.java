package ee.ut.cs.courses.appliedcrypto.model.asn1;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;

import ee.ut.cs.courses.appliedcrypto.model.Searchable;

/**
 * Encodes the Book data structure.
 * <pre>
 * Book ::= SEQUENCE {
 *   author UTF8String,
 *   title UTF8String,
 *   publisher UTF8String,
 *   year INTEGER
 * }
 * </pre>
 */
public class Book extends ASN1Encodable implements Searchable {
    private DERUTF8String author;
    private DERUTF8String title;
    private DERUTF8String publisher;
    private DERInteger year;

    public Book(String author, String title, String publisher, int year) {
        this.author = new DERUTF8String(author);
        this.title = new DERUTF8String(title);
        this.publisher = new DERUTF8String(publisher);
        this.year = new DERInteger(year);
    }

    /**
     * Decode the book object.
     */
    public Book(ASN1Sequence sequence) {
        assert sequence.size() == 4;

        author = (DERUTF8String) sequence.getObjectAt(0);
        title = (DERUTF8String) sequence.getObjectAt(1);
        publisher = (DERUTF8String) sequence.getObjectAt(2);
        year = (DERInteger) sequence.getObjectAt(3);
    }

    /**
     * Encode the book object.
     */
    public DERObject toASN1Object() {
        ASN1Encodable[] contents = new ASN1Encodable[] {
                author, title, publisher, year};
        // Output sequence containing the fields.
        return new DERSequence(contents);
    }

    /**
     * This is used by LibraryItem to decode {@link Book} objects.
     */
    public static Book getInstance(ASN1TaggedObject obj, boolean explicit) {
        return new Book(ASN1Sequence.getInstance(obj, explicit));
    }

    public String toString() {
        return "\"" + title + "\" by " + author
                + ", published in " + year + " by " + publisher;
    }

    @Override
    public boolean contains(String pattern) {
        if (title.getString().matches(".*"+pattern+".*")  ||
            author.getString().matches(".*"+pattern+".*") ||
            publisher.getString().matches(".*"+pattern+".*")) {
            
            return true;
        }
        return false;
    }
    
    @Override
    public boolean issuedIn(int year) {
        return this.year.getValue().intValue() == year;
    }
}
