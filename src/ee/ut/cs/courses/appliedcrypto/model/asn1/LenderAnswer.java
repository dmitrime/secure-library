package ee.ut.cs.courses.appliedcrypto.model.asn1;

import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * <pre>
 * LenderAnswer ::= CHOICE {
 *     searchA     [0] SearchAnswer,
 *     borrowA     [1] BorrowAnswer,
 *     returnA     [2] ReturnAnswer,
 *     failA       [3] UTF8String
 * }
 * </pre>
 */
public class LenderAnswer extends ASN1Encodable implements ASN1Choice {
    public static final int SEARCH = 0;
    public static final int BORROW = 1;
    public static final int RETURN = 2;
    public static final int FAIL   = 3;

    private static final boolean EXPLICIT = true;

    private int tag;
    private ASN1Encodable value;

    public LenderAnswer(SearchAnswer searchAnswer) {
        tag = SEARCH;
        value = searchAnswer;
    }

    public LenderAnswer(BorrowAnswer borrowAnswer) {
        tag = BORROW;
        value = borrowAnswer;
    }
    
    public LenderAnswer(ReturnAnswer returnAnswer) {
        tag = RETURN;
        value = returnAnswer;
    }
    
    public LenderAnswer(DERUTF8String failAnswer) {
        tag = FAIL;
        value = failAnswer;
    }
    
    public LenderAnswer(String failAnswer) {
        tag = FAIL;
        value = new DERUTF8String(failAnswer);
    }

    public int getType() {
        return tag;
    }

    public SearchAnswer getSearchAnswer() {
        assert tag == SEARCH;
        
        return (SearchAnswer) value;
    }
    
    public BorrowAnswer getBorrowAnswer() {
        assert tag == BORROW;
        
        return (BorrowAnswer) value;
    }
    
    public ReturnAnswer getReturnAnswer() {
        assert tag == RETURN;
        
        return (ReturnAnswer) value;
    }
    
    public DERUTF8String getFailAnswer() {
        assert tag == FAIL;
        
        return (DERUTF8String) value;
    }

    public static LenderAnswer getInstance(Object obj) {
        if (obj == null || obj instanceof LenderAnswer) {
            return (LenderAnswer) obj;
        }
        
        if (obj instanceof ASN1TaggedObject) {
            ASN1TaggedObject tagObj = (ASN1TaggedObject) obj;
            
            // Check the tag to see which option it is.
            switch (tagObj.getTagNo()) {
                case SEARCH:
                    return new LenderAnswer(
                            SearchAnswer.getInstance(tagObj, EXPLICIT));
                case BORROW:
                    return new LenderAnswer(
                            BorrowAnswer.getInstance(tagObj, EXPLICIT));
                case RETURN:
                    return new LenderAnswer(
                            ReturnAnswer.getInstance(tagObj, EXPLICIT));
                case FAIL:
                    return new LenderAnswer(
                            (DERUTF8String) tagObj.getObject().getDERObject());
                default:
                    throw new IllegalArgumentException(
                            "Unknown tag: " + tagObj.getTagNo());
            }
        }

        throw new IllegalArgumentException("Unknown object in getInstance: "
                + obj.getClass().getName());
    }

    public DERObject toASN1Object() {
        return new DERTaggedObject(EXPLICIT, tag, value);
    }

    public String toString() {
        return value.toString();
    }
}
