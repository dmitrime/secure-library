package ee.ut.cs.courses.appliedcrypto.model.asn1;

import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * SearchAnswer ::= SEQUENCE OF LibraryItem
 * </pre>
 */
public class SearchAnswer extends ASN1Encodable {
    private List<LibraryItem> items = new ArrayList<LibraryItem>();

    public SearchAnswer() {
    }
    
    public SearchAnswer(List<LibraryItem> items) {
        addAll(items);
    }
    
    public SearchAnswer(ASN1Sequence sequence) {
        for (int i = 0; i < sequence.size(); ++i) {
            items.add(LibraryItem.getInstance(sequence.getObjectAt(i)));
        }
    }
    
    public static SearchAnswer getInstance(ASN1TaggedObject obj, boolean explicit) {
        return new SearchAnswer(ASN1Sequence.getInstance(obj, explicit));
    }
    
    public void add(LibraryItem item) {
        items.add(item);
    }
    
    public void addAll(List<LibraryItem> items) {
        this.items.addAll(items);
    }
    
    public List<LibraryItem> getItems() {
        return items;
    }

    public DERObject toASN1Object() {
        return new DERSequence(items.toArray(new LibraryItem[0]));
    }
    
    public String toString() {
        return items.toString();
    }
}
