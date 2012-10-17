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
 * BorrowStatement ::= SEQUENCE {
 * item LibraryItem,
 * outTime GeneralizedTime
 * }
 * </pre>
 */
public class BorrowStatement extends ASN1Encodable {
    private DEREncodable item;
    private DERGeneralizedTime outTime;
    
    public BorrowStatement(LibraryItem item, Date outTime) {
        this.item = item;
        this.outTime = new DERGeneralizedTime(outTime);
    }

    public BorrowStatement(ASN1Sequence sequence) {
        assert sequence.size() == 2;
        
        item = sequence.getObjectAt(0);
        outTime = (DERGeneralizedTime) sequence.getObjectAt(1);
    }

    public DERObject toASN1Object() {
        ASN1Encodable[] contents = new ASN1Encodable[] {(ASN1Encodable)item, outTime};
        return new DERSequence(contents);
    }

    @Override
    public String toString() {
        return "BorrowStatement [item=" + item + ", outTime=" + outTime.getTime() + "]";
    }
    
    public LibraryItem getLibraryItem() {
        return LibraryItem.getInstance(item);
    }
    
    public Date getOutTime() {
        try {
            return outTime.getDate();
        } catch (ParseException e) {
            return null;
        }
    }
}
