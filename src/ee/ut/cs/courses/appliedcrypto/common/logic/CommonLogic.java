package ee.ut.cs.courses.appliedcrypto.common.logic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.cms.CMSSignedData;

import ee.ut.cs.courses.appliedcrypto.model.asn1.Identifiers;

public interface CommonLogic {
    public void writeData(OutputStream outputStream, ASN1Encodable obj) throws IOException;

    public DERObject readData(InputStream inputStream) throws IOException;

    public CMSSignedData createSignedData(ASN1Encodable borrowStatement);

    public String verifySignedData(SignedData signedData, Identifiers id);
}
