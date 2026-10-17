# TLS and MITM Protection — Java Spring Boot Project

## Purpose

This educational project demonstrates secure HTTPS communication and common defenses against man-in-the-middle (MITM) attacks. It uses Java 17, Spring Boot, TLS 1.2/1.3, certificate validation, hostname verification, and input validation.

This is not a production payment application. It does not store payment or customer data.

## Requirements

- JDK 17+
- Maven 3.9+
- `keytool` (included with the JDK)

## 1. Generate a local development certificate

Run in the project root:

```bash
keytool -genkeypair \
  -alias localhost \
  -keyalg RSA \
  -keysize 3072 \
  -validity 365 \
  -storetype PKCS12 \
  -keystore localhost.p12 \
  -storepass changeit \
  -keypass changeit \
  -dname "CN=localhost, OU=Class Project, O=Local Test, L=Local, ST=NA, C=US" \
  -ext "SAN=dns:localhost,ip:127.0.0.1"
```

Do not upload `localhost.p12` to GitHub. It contains a private key.

Export the public certificate:

```bash
keytool -exportcert \
  -alias localhost \
  -keystore localhost.p12 \
  -storetype PKCS12 \
  -storepass changeit \
  -rfc \
  -file localhost.crt
```

Create a client truststore:

```bash
keytool -importcert \
  -alias localhost \
  -file localhost.crt \
  -storetype PKCS12 \
  -keystore truststore.p12 \
  -storepass changeit \
  -noprompt
```

The `.p12` files are excluded from Git by `.gitignore`.

## 2. Run the HTTPS server

```bash
mvn spring-boot:run
```

The server should listen on `https://localhost:8443`.

## 3. Test HTTPS using curl

```bash
curl --cacert localhost.crt https://localhost:8443/api/health
```

Expected response:

```text
Secure HTTPS connection is working.
```

Send a valid message:

```bash
curl --cacert localhost.crt \
  -H "Content-Type: application/json" \
  -d '{"message":"Hello from the secure client"}' \
  https://localhost:8443/api/messages
```

Expected response:

```text
Message received securely: Hello from the secure client
```

Test invalid input:

```bash
curl --cacert localhost.crt \
  -H "Content-Type: application/json" \
  -d '{"message":"   "}' \
  https://localhost:8443/api/messages
```

Expected result: HTTP 400 Bad Request.

## 4. Run the Java TLS client

Open a second terminal in the project root.

macOS/Linux:

```bash
export TLS_TRUSTSTORE="$PWD/truststore.p12"
export TLS_TRUSTSTORE_PASSWORD="changeit"
mvn -q -DskipTests package
java -cp target/classes edu.project.secure.TlsClient
```

Expected result: HTTP 200 and `Secure HTTPS connection is working.`

## 5. Test an untrusted certificate

Stop the client and run:

```bash
unset TLS_TRUSTSTORE
unset TLS_TRUSTSTORE_PASSWORD
java -cp target/classes edu.project.secure.TlsClient
```

Expected result: the connection is rejected because the self-signed certificate is not in the JDK's standard truststore.

## 6. Run automated tests

```bash
mvn test
```

Tests cover the health endpoint, valid messages, blank messages, and messages over the size limit. The tests disable TLS inside MockMvc so they test controller behavior. The manual HTTPS commands and TLS client demonstrate actual TLS behavior.

## Pseudocode

| Step | Process |
|---:|---|
| 1 | The client requests a secure connection using HTTPS. |
| 2 | The server presents its digital certificate. |
| 3 | The client validates the certificate chain, validity, and hostname. |
| 4 | Reject the connection if any required check fails. |
| 5 | Establish TLS only after verification succeeds. |
| 6 | Exchange messages through the encrypted connection. |
| 7 | Do not fall back to plain HTTP if TLS fails. |
| 8 | Log security failures without recording sensitive data. |

## Simple explanation

TLS encrypts information exchanged between the client and server. The client verifies the server certificate and hostname before sending information. If the certificate is untrusted or the hostname does not match, the client rejects the connection. These checks help protect against common MITM attacks.

## Screenshots to include

1. Spring Boot application started on port 8443.
2. Successful HTTPS health request.
3. Successful message request.
4. Untrusted certificate rejected by the Java client.
5. Invalid blank message rejected with HTTP 400.
6. `mvn test` output.

Capture real results after running the project. Do not claim a test passed until you have verified its output.

## Conclusion

This project demonstrates secure HTTPS communication using TLS, certificate validation, hostname verification, and input validation. The client rejects untrusted certificates instead of bypassing security checks, helping reduce the risk of common MITM attacks.

## References

- Rescorla, E. (2018). *The Transport Layer Security (TLS) Protocol Version 1.3* (RFC 8446). RFC Editor. https://www.rfc-editor.org/rfc/rfc8446
- National Institute of Standards and Technology. (2019). *Guidelines for the Selection, Configuration, and Use of Transport Layer Security (TLS) Implementations* (NIST SP 800-52 Rev. 2). https://doi.org/10.6028/NIST.SP.800-52r2
- OWASP Foundation. (n.d.). *Transport Layer Security Cheat Sheet*. https://cheatsheetseries.owasp.org/cheatsheets/Transport_Layer_Security_Cheat_Sheet.html
