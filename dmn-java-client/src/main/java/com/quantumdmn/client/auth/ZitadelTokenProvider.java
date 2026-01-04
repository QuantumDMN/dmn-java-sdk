package com.quantumdmn.client.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * A TokenProvider that authenticates using a Zitadel Service Account JSON Key file.
 * Automatically handles JWT signing and token exchange.
 */
public class ZitadelTokenProvider implements Supplier<String> {

    private final String userId;
    private final String keyId;
    private final PrivateKey privateKey;
    private final String issuer;
    
    private String cachedToken;
    private Instant tokenExpiry = Instant.MIN;

    public ZitadelTokenProvider(String jsonKeyPath, String issuer) throws IOException {
        this(Path.of(jsonKeyPath), issuer);
    }

    public ZitadelTokenProvider(Path jsonKeyPath, String issuer) throws IOException {
        this.issuer = issuer;
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonKeyPath.toFile());
        
        if (!root.has("userId") || !root.has("key") || !root.has("keyId")) {
            throw new IllegalArgumentException("Invalid Zitadel JSON Key file format");
        }
        
        this.userId = root.get("userId").asText();
        this.keyId = root.get("keyId").asText();
        String keyPem = root.get("key").asText();
        
        this.privateKey = parsePrivateKey(keyPem);
    }
    
    private PrivateKey parsePrivateKey(String keyPem) {
        try {
            String privateKeyContent = keyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
            
            byte[] encoded = Base64.getDecoder().decode(privateKeyContent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse private key", e);
        }
    }

    @Override
    public synchronized String get() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiry.minusSeconds(60))) {
            return cachedToken;
        }

        try {
            return fetchNewToken();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch Zitadel token", e);
        }
    }

    private String fetchNewToken() throws Exception {
        String jwt = Jwts.builder()
            .issuer(userId)
            .subject(userId)
            .audience().add(issuer).and()
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 3600 * 1000))
            .id(UUID.randomUUID().toString())
            .header().keyId(keyId).and()
            .signWith(privateKey)
            .compact();

        String scope = URLEncoder.encode("openid profile urn:zitadel:iam:user:resourceowner", StandardCharsets.UTF_8);
        String body = "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&scope=" + scope + "&assertion=" + jwt;
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(issuer + "/oauth/v2/token"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Token request failed: " + response.statusCode() + " " + response.body());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode respNode = mapper.readTree(response.body());
        
        this.cachedToken = respNode.get("access_token").asText();
        int expiresIn = respNode.get("expires_in").asInt();
        this.tokenExpiry = Instant.now().plusSeconds(expiresIn);
        
        return cachedToken;
    }
}
