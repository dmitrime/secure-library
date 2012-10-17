package ee.ut.cs.courses.appliedcrypto.model.asn1;

import java.text.ParseException;
import java.util.Date;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cms.SignedData;

/**
 * <pre>
 * BorrowQuery ::= SEQUENCE {
 * item LibraryItem,
 * outTime GeneralizedTime,
 * signature SignedData
 * }
 * </pre>
 */
public class BorrowQuery extends ASN1Encodable {
    private DEREncodable item;
    private DERGeneralizedTime outTime;
    private DEREncodable signature;
    
    public BorrowQuery(LibraryItem item, Date outTime, DEREncodable signature) {
        this.item = item;
        this.outTime = new DERGeneralizedTime(outTime);
        this.signature = signature;
    }

    public BorrowQuery(ASN1Sequence sequence) {
        assert sequence.size() == 3;
        
        item = sequence.getObjectAt(0);
        outTime = (DERGeneralizedTime) sequence.getObjectAt(1);
        signature = sequence.getObjectAt(2);
    }

    public DERObject toASN1Object() {
        ASN1Encodable[] contents = new ASN1Encodable[] {(ASN1Encodable)item, outTime, (ASN1Encodable)signature};
        return new DERSequence(contents);
    }
    
    public static BorrowQuery getInstance(ASN1TaggedObject obj, boolean explicit) {
        return new BorrowQuery(ASN1Sequence.getInstance(obj, explicit));
    }

    @Override
    public String toString() {
        return "BorrowQuery [item=" + item + ", outTime=" + outTime + ", signature=" + signature + "]";
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
    
    public SignedData getSignedData() {
        return SignedData.getInstance(signature);
    }
}
