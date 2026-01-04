package com.quantumdmn.client;

import com.quantumdmn.client.api.DefaultApi;
import com.quantumdmn.client.model.EvaluateStoredRequest;
import com.quantumdmn.client.model.EvaluationResult;
import com.quantumdmn.client.model.FeelValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * High-level client for the QuantumDMN Engine.
 * Wraps the generated API client and provides simplified access to core engine features.
 */
public class DmnEngine {
    
    private final DefaultApi api;
    private final UUID projectId;

    /**
     * Creates a new DmnEngine using an existing DmnService and Project ID.
     */
    public DmnEngine(DmnService service, String projectId) {
        this.api = service.getApi();
        this.projectId = UUID.fromString(projectId);
    }
    
    /**
     * Creates a new DmnEngine from configuration directly.
     */
    public DmnEngine(String baseUrl, String token, String projectId) {
        this(new DmnService(baseUrl, () -> token), projectId);
    }
    
    /**
     * Evaluates a decision definition by its XML ID with an optional version and context.
     *
     * @param xmlId           The XML definition ID (business key)
     * @param version         Optional version number (null for latest)
     * @param evaluationContext Map of input variables (FEEL context)
     * @return Map of decision results
     * @throws ApiException if the request fails
     */
    public Map<String, EvaluationResult> evaluate(String xmlId, Integer version, Map<String, Object> evaluationContext) throws ApiException {
        EvaluateStoredRequest request = new EvaluateStoredRequest();
        
        // Convert context to Map<String, FeelValue>
        Map<String, FeelValue> feelCtx = new HashMap<>();
        if (evaluationContext != null) {
            for (Map.Entry<String, Object> entry : evaluationContext.entrySet()) {
                feelCtx.put(entry.getKey(), FeelValue.fromRaw(entry.getValue()));
            }
        }
        request.setContext(feelCtx);

        return (Map<String, EvaluationResult>) api.evaluateByXMLID(projectId, xmlId, request, version);
    }
}
