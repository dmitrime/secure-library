package ee.ut.cs.courses.appliedcrypto.model.asn1;

import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * <pre>
 * LenderQuery ::= CHOICE {
 *     searchQ     [0] SearchQuery,
 *     borrowQ     [1] BorrowQuery,
 *     returnQ     [2] ReturnQuery
 *    }
 * </pre>
 */
public class LenderQuery extends ASN1Encodable implements ASN1Choice {
    public static final int SEARCH_QUERY = 0;
    public static final int BORROW_QUERY = 1;
    public static final int RETURN_QUERY = 2;

    private static final boolean EXPLICIT = true;

    private int tag;
    private ASN1Encodable value;

    public LenderQuery(SearchQuery searchQuery) {
        tag = SEARCH_QUERY;
        value = searchQuery;
    }

    public LenderQuery(BorrowQuery borrowQuery) {
        tag = BORROW_QUERY;
        value = borrowQuery;
    }
    
    public LenderQuery(ReturnQuery returnQuery) {
        tag = RETURN_QUERY;
        value = returnQuery;
    }

    public int getType() {
        return tag;
    }

    public SearchQuery getSearchQuery() {
        assert tag == SEARCH_QUERY;
        
        return (SearchQuery) value;
    }
    
    public BorrowQuery getBorrowQuery() {
        assert tag == BORROW_QUERY;
        
        return (BorrowQuery) value;
    }
    
    public ReturnQuery getReturnQuery() {
        assert tag == RETURN_QUERY;
        
        return (ReturnQuery) value;
    }

    public static LenderQuery getInstance(Object obj) {
        if (obj == null || obj instanceof LenderQuery) {
            return (LenderQuery) obj;
        }
        
        if (obj instanceof ASN1TaggedObject) {
            ASN1TaggedObject tagObj = (ASN1TaggedObject) obj;
            
            // Check the tag to see which option it is.
            switch (tagObj.getTagNo()) {
                case SEARCH_QUERY:
                    return new LenderQuery(
                            SearchQuery.getInstance(tagObj, EXPLICIT));
                case BORROW_QUERY:
                    return new LenderQuery(
                            BorrowQuery.getInstance(tagObj, EXPLICIT));
                case RETURN_QUERY:
                    return new LenderQuery(
                            ReturnQuery.getInstance(tagObj, EXPLICIT));
                            
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
