package io.stepprflow.dashboard.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stepprflow.ui")
@Data
public class UiProperties {

    /**
     * Enable UI module.
     */
    private boolean enabled = true;

    /**
     * Base path for the dashboard.
     */
    private String basePath = "/dashboard";

    /**
     * Title of the dashboard.
     */
    private String title = "StepprFlow Dashboard";

    /**
     * Refresh interval in seconds.
     */
    private int refreshInterval = 5;

    /**
     * Enable dark mode by default.
     */
    private boolean darkMode = false;

    /**
     * CORS configuration.
     */
    private Cors cors = new Cors();

    @Data
    public static class Cors {
        /**
         * Allowed origins for CORS.
         * Default: empty (same-origin only).
         * Set to specific origins like "http://localhost:3000" for development
         * or "https://yourdomain.com" for production.
         */
        private String[] allowedOrigins = {};

        /**
         * Allowed HTTP methods for CORS.
         */
        private String[] allowedMethods = {"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"};

        /**
         * Allowed headers for CORS.
         */
        private String[] allowedHeaders = {"*"};

        /**
         * Whether to allow credentials (cookies, authorization headers).
         */
        private boolean allowCredentials = false;

        /**
         * Max age of the CORS preflight cache in seconds.
         */
        private long maxAge = 3600;
    }
}
