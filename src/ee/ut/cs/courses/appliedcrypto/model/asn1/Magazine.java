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
 * Encodes the Magazine data structure.
 * <pre>
 * Magazine ::= SEQUENCE {
 *   name UTF8String,
 *   year INTEGER,
 *   issue INTEGER
 * }
 * </pre>
 */
public class Magazine extends ASN1Encodable implements Searchable {
    private DERUTF8String name;
    private DERInteger year;
    private DERInteger issue;

    public Magazine(String name, int year, int issue) {
        this.name = new DERUTF8String(name);
        this.year = new DERInteger(year);
        this.issue = new DERInteger(issue);
    }
    
    /**
     * Decode the magazine object.
     */
    public Magazine(ASN1Sequence sequence) {
        assert sequence.size() == 3;
        
        name = (DERUTF8String) sequence.getObjectAt(0);
        year = (DERInteger) sequence.getObjectAt(1);
        issue = (DERInteger) sequence.getObjectAt(2);
    }
    
    /**
     * Encode the magazine.
     */
    public DERObject toASN1Object() {
        return new DERSequence(
                new ASN1Encodable[] { name, year, issue });
    }

    /**
     * This is used by LibraryItem to decode {@link Magazine} objects.
     */
    public static Magazine getInstance(ASN1TaggedObject obj, boolean explicit) {
        return new Magazine(ASN1Sequence.getInstance(obj, explicit));
    }

    public String toString() {
        return name + ", " + year + "/" + issue;
    }

    @Override
    public boolean contains(String pattern) {
        if (name.getString().matches(".*"+pattern+".*")) {
            return true;
        }
        return false;
    }

    @Override
    public boolean issuedIn(int year) {
        return this.year.getValue().intValue() == year;
    }
}
