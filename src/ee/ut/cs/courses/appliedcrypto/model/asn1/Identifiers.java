package ee.ut.cs.courses.appliedcrypto.model.asn1;

public enum Identifiers {
    yearQuery(1), textQuery(2), borrowStatement(3), returnStatement(4);
    
    private static final String BASE_IDENTIFIER = "1.3.6.1.4.1.3516.15.1.1";
    
    private int code;
    
    private Identifiers(int code) {
        this.code = code;
    }
    
    @Override
    public String toString() {
        return BASE_IDENTIFIER + "." + code;
    }
    
    public boolean stringEquals(String id) {
        return toString().equals(id);
    }
}
