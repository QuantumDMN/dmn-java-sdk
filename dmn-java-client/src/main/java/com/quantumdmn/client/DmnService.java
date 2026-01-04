package com.quantumdmn.client;

import com.quantumdmn.client.api.DefaultApi;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.function.Supplier;

/**
 * QuantumDMN Service wrapper that handles authentication and provides
 * a simplified interface for interacting with the DMN Engine API.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * DmnService service = new DmnService("https://api.quantumdmn.com", () -> getToken());
 * List<Project> projects = service.getApi().listProjects();
 * }</pre>
 */
public class DmnService {
    
    private final String baseUrl;
    private final Supplier<String> tokenProvider;
    private ApiClient apiClient;
    private DefaultApi api;

    /**
     * Creates a new DmnService with a token provider for authentication.
     *
     * @param baseUrl       The API base URL (e.g., "https://api.quantumdmn.com")
     * @param tokenProvider A supplier that returns a valid access token
     */
    public DmnService(String baseUrl, Supplier<String> tokenProvider) {
        this.baseUrl = baseUrl;
        this.tokenProvider = tokenProvider;
        initializeClient();
    }

    /**
     * Creates a new DmnService with a static token.
     *
     * @param baseUrl The API base URL
     * @param token   The bearer token
     */
    public DmnService(String baseUrl, String token) {
        this(baseUrl, () -> token);
    }

    private void initializeClient() {
        this.apiClient = new ApiClient();
        this.apiClient.updateBaseUri(baseUrl);
        this.apiClient.setRequestInterceptor(this::addAuthHeader);
        this.api = new DefaultApi(apiClient);
    }

    private HttpRequest.Builder addAuthHeader(HttpRequest.Builder builder) {
        String token = tokenProvider.get();
        return builder.header("Authorization", "Bearer " + token);
    }

    /**
     * Returns the underlying API client for advanced usage.
     */
    public ApiClient getApiClient() {
        return apiClient;
    }

    /**
     * Returns the DefaultApi for making API calls.
     */
    public DefaultApi getApi() {
        return api;
    }

    /**
     * Returns the base URL.
     */
    public String getBaseUrl() {
        return baseUrl;
    }
}
