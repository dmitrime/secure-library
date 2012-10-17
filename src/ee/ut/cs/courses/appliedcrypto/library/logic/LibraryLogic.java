package ee.ut.cs.courses.appliedcrypto.library.logic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ee.ut.cs.courses.appliedcrypto.model.asn1.LenderQuery;

public interface LibraryLogic {
    public void processQuery(LenderQuery query, OutputStream outputStream) throws IOException;

    public LenderQuery readQuery(InputStream inputStream) throws IOException;
}
