Applied cryptography course project: Secure Library
===
Library project documentation
===
The server can be started by running LibraryServer class with port number as a parameter.
The client can be started by running LenderClient class with host and port number as parameters.
Alternatively, they can be run using ant targets run-server and run-client. Parameters can
be set in build.xml.
The judge can be started by running JudgeResolution and providing 2 file names for client and
server evidence. The evidence files are encoded SignedData objects. The server saves its 
last evidence by default to "evidence/library.evidence" after each borrow event. The client 
saves its last evidence by default  to "evidence/lender.evidence" after each return event.
The judge can be run using ant run-judge.

Certificates
===
The CA's certificate is by default called "cacert.pem" in the "ca" directory.
"ca/private" contains all the private keys and "ca/newcerts" contains all 
certificates signed by the CA (both SSL and signature certs).

The new certificates can be created with openssl in a standard way:

cd ca

openssl req -new -keyout private/[KEY_NAME].pem -config ./openssl.conf -out [REQ_NAME].pem -sha1 -days 365

openssl ca -config ./openssl.conf -extensions [v3_req|ssl_server|ssl_client] -policy policy_match \

        -out newcerts/[CERT_NAME].crt -infiles [REQ_NAME].pem

Here the extension "v3_req" is used to create certificates for signing data, 
"ssl_server" is for SSL server and "ssl_client" is for SSL clients. 

The default CRL file is located in "ca/crl/ca.crl". It can be generate using:

cd ca

openssl ca -config ./openssl.conf -gencrl -out crl/ca.crl

The default password for private keys is "password".

Keystores
===
The library server and the client use java keystores to load keys and certificates.
There are 5 keystores in "keystores" directory:

library-ssl.jks		- 1 PrivateKeyEntry "library-ssl" and 1 trustedCertEntry "root"

trust-ssl.jks		- 3 trustedCertEntry "lender", "library," and "root"

lender-sign.jks		- 1 PrivateKeyEntry "lender-sign" and 1 trustedCertEntry "root"

library-sign.jks 	- 1 PrivateKeyEntry "library-sign" and 1 trustedCertEntry "root"

lender-ssl.jks		- 1 PrivateKeyEntry "lender-ssl" and 1 trustedCertEntry "root"
					  OPTIONAL, if present, 2-way authentication is used
All keystores have a trustedCertEntry with alias "root" with the CA's certificate. 
The default password for all keystores is "password".

The library-* and lender-* keystores can be created from the pem certificates and
keys using ee.ut.cs.courses.appliedcrypto.util.Keystore program. 
The trust-ssl.jks keystore can be created from the pem certificates using
keys using ee.ut.cs.courses.appliedcrypto.util.TrustKeystore program.

There are 3 ant targets to generate keystores automatically:
keystore-gen, trust-gen and gen-all-keystores.  The first can generate any
library-* and lender-* keystores, the second trust-ssl type keystore and the third can
generate everything. Parameters can be changed in build.xml.

When verifying, the server loads the root certificate from its keystore and checks if
the certificate used by the client to sign data was signed with the root. The client
checks the server's certificate in the same way.  

Client commands
===
Available client commands:

(s)earch [text|year] 	 - search the library for items

(b)orrow n           	 - borrow item n from the found items list

(r)eturn n           	 - return item n from the borrowed items list

(l)ist               	 - list borrowed items

(h)elp               	 - print this help

(q)uit               	 - disconnect and quit

Logging
===
The client and server logging message levels can be configured in src/log4j.properties.

Authors
===
Dmitri Melnikov
3 Jun 2010

