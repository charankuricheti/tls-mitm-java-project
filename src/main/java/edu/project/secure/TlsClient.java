package edu.project.secure;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.time.Duration;

/**
 * HTTPS client for the classroom demo.
 * Uses normal hostname verification and validates certificates using a truststore.
 * Never trusts all certificates.
 */
public final class TlsClient {
    private TlsClient() {}

    public static void main(String[] args) throws Exception {
        String url = args.length > 0
                ? args[0] : "https://localhost:8443/api/health";

        String trustStorePath = System.getenv("TLS_TRUSTSTORE");
        String trustStorePassword = System.getenv("TLS_TRUSTSTORE_PASSWORD");

        SSLContext sslContext = createSslContext(trustStorePath, trustStorePassword);

        HttpClient client = HttpClient.newBuilder()
                .sslContext(sslContext)
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "text/plain")
                .GET()
                .build();

        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("HTTP status: " + response.statusCode());
            System.out.println("Response: " + response.body());
        } catch (javax.net.ssl.SSLException e) {
            System.err.println("SECURE CONNECTION REJECTED");
            System.err.println("TLS certificate/hostname verification failed.");
            System.err.println("Details: " + e.getMessage());
            System.exit(2);
        }
    }

    private static SSLContext createSslContext(String path, String password)
            throws Exception {
        if (path == null || path.isBlank()) {
            return SSLContext.getDefault();
        }
        if (password == null) {
            throw new IllegalArgumentException(
                    "Set TLS_TRUSTSTORE_PASSWORD when TLS_TRUSTSTORE is set.");
        }

        KeyStore store = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(Path.of(path))) {
            store.load(in, password.toCharArray());
        }

        TrustManagerFactory factory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        factory.init(store);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, factory.getTrustManagers(), null);
        return context;
    }
}
