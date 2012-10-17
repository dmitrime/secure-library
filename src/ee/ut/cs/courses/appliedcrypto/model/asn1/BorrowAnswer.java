package ee.ut.cs.courses.appliedcrypto.model.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERObject;

/**
 * BorrowAnswer ::= NULL
 */
public class BorrowAnswer extends ASN1Encodable {
    private DERNull answer;
    
    public BorrowAnswer() {
        answer = DERNull.INSTANCE;
    }
    
    public BorrowAnswer(DERObject object) {
        assert (object instanceof DERNull);
        
        answer = (DERNull) object;
    }

    public static BorrowAnswer getInstance(ASN1TaggedObject obj, boolean explicit) {
        return new BorrowAnswer(obj.getObject());
    }
    
    public boolean isOk() {
        return answer.equals(DERNull.INSTANCE);
    }
    
    @Override
    public DERObject toASN1Object() {
        return answer;
    }

    @Override
    public String toString() {
        return "BorrowAnswer [answer=" + answer + "]";
    }
}
