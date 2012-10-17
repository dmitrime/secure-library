package ee.ut.cs.courses.appliedcrypto.model;

public interface Searchable {
    public boolean contains(String pattern);
    
    public boolean issuedIn(int year);
}
