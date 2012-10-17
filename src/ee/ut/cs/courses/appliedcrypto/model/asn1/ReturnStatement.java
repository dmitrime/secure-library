package ee.ut.cs.courses.appliedcrypto.model.asn1;

import java.text.ParseException;
import java.util.Date;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * ReturnStatement ::= SEQUENCE {
 * item LibraryItem,
 * inTime GeneralizedTime
 * }
 * </pre>
 */
public class ReturnStatement extends ASN1Encodable {
    private DEREncodable item;
    private DERGeneralizedTime inTime;
    
    public ReturnStatement(LibraryItem item, Date inTime) {
        this.item = item;
        this.inTime = new DERGeneralizedTime(inTime);
    }

    public ReturnStatement(ASN1Sequence sequence) {
        assert sequence.size() == 2;
        
        item = sequence.getObjectAt(0);
        inTime = (DERGeneralizedTime) sequence.getObjectAt(1);
    }

    public DERObject toASN1Object() {
        ASN1Encodable[] contents = new ASN1Encodable[] {(ASN1Encodable)item, inTime};
        return new DERSequence(contents);
    }

    @Override
    public String toString() {
        return "ReturnStatement [inTime=" + inTime + ", item=" + item + "]";
    }
    
    public LibraryItem getLibraryItem() {
        return LibraryItem.getInstance(item);
    }
    
    public Date getInTime() {
        try {
            return inTime.getDate();
        } catch (ParseException e) {
            return null;
        }
    }
}
