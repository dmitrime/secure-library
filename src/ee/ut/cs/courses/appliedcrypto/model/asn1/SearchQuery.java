package ee.ut.cs.courses.appliedcrypto.model.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * <pre>
 * SearchQuery ::= SEQUENCE {
 *     searchFormat        OBJECT IDENTIFIER,
 *     searchContent       ANY DEFINED BY searchFormat
 * }
 * </pre>
 */
public class SearchQuery extends ASN1Encodable {
    private DERObjectIdentifier searchFormat;
    private DEREncodable searchContent;

    public SearchQuery(String format, int year) {
        this.searchFormat = new DERObjectIdentifier(format);
        this.searchContent = new DERInteger(year);
    }
    
    public SearchQuery(String format, String text) {
        this.searchFormat = new DERObjectIdentifier(format);
        this.searchContent = new DERUTF8String(text);
    }
    
    public SearchQuery(ASN1Sequence sequence) {
        assert sequence.size() == 2;
        searchFormat = (DERObjectIdentifier) sequence.getObjectAt(0);
        searchContent = sequence.getObjectAt(1);
    }

    public DERObject toASN1Object() {
        ASN1Encodable[] contents = new ASN1Encodable[] {searchFormat, (ASN1Encodable) searchContent};
        return new DERSequence(contents);
    }

    public static SearchQuery getInstance(ASN1TaggedObject obj, boolean explicit) {
        return new SearchQuery(ASN1Sequence.getInstance(obj, explicit));
    }

    @Override
    public String toString() {
        return "SearchQuery [searchContent=" + searchContent + ", searchFormat=" + searchFormat + "]";
    }
    
    public String getSearchFormat() {
        return searchFormat.getId();
    }
    
    public int getIntSearchContent() {
        return ((DERInteger)searchContent).getValue().intValue();
    }
    
    public String getStringSearchContent() {
        return ((DERUTF8String)searchContent).getString();
    }
}
