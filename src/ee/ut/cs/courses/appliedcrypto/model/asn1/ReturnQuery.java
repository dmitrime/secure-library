package ee.ut.cs.courses.appliedcrypto.model.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * ReturnQuery ::= LibraryItem
 * </pre> 
 */
public class ReturnQuery extends ASN1Encodable {
    private DEREncodable item;
    
    public ReturnQuery(LibraryItem item) {
        this.item = item;
    }

    public ReturnQuery(ASN1Sequence sequence) {
        assert sequence.size() == 1;
        
        item = sequence.getObjectAt(0);
    }

    public DERObject toASN1Object() {
        ASN1Encodable[] contents = new ASN1Encodable[] {(ASN1Encodable)item};
        return new DERSequence(contents);
    }
    
    public static ReturnQuery getInstance(ASN1TaggedObject obj, boolean explicit) {
        return new ReturnQuery(ASN1Sequence.getInstance(obj, explicit));
    }

    @Override
    public String toString() {
        return "ReturnQuery [item=" + item + "]";
    }
    
    public LibraryItem getLibraryItem() {
        return LibraryItem.getInstance(item);
    }
}
