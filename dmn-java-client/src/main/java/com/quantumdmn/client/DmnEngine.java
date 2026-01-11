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
     * Evaluates a decision definition by its XML ID with optional parameters.
     *
     * @param xmlId           The XML definition ID (business key)
     * @param evaluationContext Map of input variables (FEEL context)
     * @param options         Optional configuration (version, businessId)
     * @return Map of decision results
     * @throws ApiException if the request fails
     */
    public Map<String, EvaluationResult> evaluate(String xmlId, Map<String, Object> evaluationContext, EvaluateOption... options) throws ApiException {
        EvaluateConfig config = new EvaluateConfig();
        for (EvaluateOption option : options) {
            option.apply(config);
        }

        EvaluateStoredRequest request = new EvaluateStoredRequest();
        
        // Convert context to Map<String, FeelValue>
        Map<String, FeelValue> feelCtx = new HashMap<>();
        if (evaluationContext != null) {
            for (Map.Entry<String, Object> entry : evaluationContext.entrySet()) {
                feelCtx.put(entry.getKey(), FeelValue.fromRaw(entry.getValue()));
            }
        }
        request.setContext(feelCtx);
        
        if (config.businessId != null) {
            request.setBusinessId(config.businessId);
        }

        return (Map<String, EvaluationResult>) api.evaluateByXMLID(projectId, xmlId, request, config.version);
    }
    
    // --- Functional Options ---

    @FunctionalInterface
    public interface EvaluateOption {
        void apply(EvaluateConfig config);
    }

    private static class EvaluateConfig {
        Integer version = null;
        String businessId = null;
    }

    public static EvaluateOption withVersion(int version) {
        return config -> config.version = version;
    }

    public static EvaluateOption withBusinessId(String businessId) {
        return config -> config.businessId = businessId;
    }
}
