package com.quantumdmn.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for QuantumDMN client.
 * 
 * <p>Configure in application.yml:</p>
 * <pre>
 * quantumdmn:
 *   base-url: https://api.quantumdmn.com
 *   token: your-static-token  # or use token-provider bean
 * </pre>
 */
@ConfigurationProperties(prefix = "quantumdmn")
public class DmnClientProperties {
    
    /**
     * API base URL (e.g., https://api.quantumdmn.com)
     */
    private String baseUrl = "https://api.quantumdmn.com";
    
    /**
     * Static bearer token (optional, use TokenProvider bean for dynamic tokens)
     */
    private String token;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    /**
     * Authentication configuration
     */
    private Auth auth = new Auth();

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public static class Auth {
        private Zitadel zitadel = new Zitadel();

        public Zitadel getZitadel() {
            return zitadel;
        }

        public void setZitadel(Zitadel zitadel) {
            this.zitadel = zitadel;
        }

        public static class Zitadel {
            /**
             * Path to Zitadel JSON key file
             */
            private String keyFile;
            
            /**
             * Zitadel Issuer URL
             */
            private String issuer = "https://auth.quantumdmn.com";
            
            /**
             * Zitadel Project ID (required for audience scope)
             */
            private String projectId;

            public String getKeyFile() {
                return keyFile;
            }

            public void setKeyFile(String keyFile) {
                this.keyFile = keyFile;
            }

            public String getIssuer() {
                return issuer;
            }

            public void setIssuer(String issuer) {
                this.issuer = issuer;
            }

            public String getProjectId() {
                return projectId;
            }

            public void setProjectId(String projectId) {
                this.projectId = projectId;
            }
        }
    }
}
