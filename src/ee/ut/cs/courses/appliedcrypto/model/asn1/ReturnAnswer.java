package ee.ut.cs.courses.appliedcrypto.model.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cms.SignedData;

public class ReturnAnswer extends ASN1Encodable {
    private DEREncodable signature;
    
    public ReturnAnswer(DEREncodable signature) {
        this.signature = signature;
    }

    public ReturnAnswer(ASN1Sequence sequence) {
        assert sequence.size() == 1;
        
        signature = sequence.getObjectAt(0);
    }

    public DERObject toASN1Object() {
        ASN1Encodable[] contents = new ASN1Encodable[] {(ASN1Encodable)signature};
        return new DERSequence(contents);
    }
    
    public static ReturnAnswer getInstance(ASN1TaggedObject obj, boolean explicit) {
        return new ReturnAnswer(ASN1Sequence.getInstance(obj, explicit));
    }

    @Override
    public String toString() {
        return "ReturnAnswer [signature=" + signature + "]";
    }

    public SignedData getSignedData() {
        return SignedData.getInstance(signature);
    }
}
