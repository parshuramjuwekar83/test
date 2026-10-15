package in.sahamati.token.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.sahamati.token.dto.TokenResponse;
import in.sahamati.token.entity.AppConfig;
import in.sahamati.token.entity.MemberToken;
import in.sahamati.token.repository.AppConfigRepository;
import in.sahamati.token.repository.MemberTokenRepository;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TokenService {
    private static final Logger LOG = Logger.getLogger(TokenService.class);

    @Inject
    MemberTokenRepository memberTokenRepository;

    @Inject
    AppConfigRepository appConfigRepository;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Generate or return existing valid entity member token.
     * Checks previous token expiry; if expired obtains new token from external endpoint and updates DB.
     */
    @Transactional
    public TokenResponse generateEntityMemberToken(String memberId) {
        Optional<MemberToken> existingOpt = memberTokenRepository.findByMemberId(memberId);

        if (existingOpt.isPresent()) {
            MemberToken mt = existingOpt.get();
            if (mt.getExpiresAt() != null && mt.getExpiresAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC))) {
                LOG.infof("Returning existing valid token for memberId=%s", memberId);
                return toDto(mt);
            } else {
                LOG.infof("Existing token expired or missing for memberId=%s; obtaining new token", memberId);
                TokenResponse newToken = obtainAndPersistEntityToken(memberId, mt);
                return newToken;
            }
        } else {
            LOG.infof("No token found for memberId=%s; creating new entry and obtaining token", memberId);
            MemberToken mt = new MemberToken();
            mt.setMemberId(memberId);
            mt.setId(UUID.randomUUID());
            memberTokenRepository.persist(mt); // Persist stub, then obtain and update
            TokenResponse newToken = obtainAndPersistEntityToken(memberId, mt);
            return newToken;
        }
    }

    private TokenResponse toDto(MemberToken mt) {
        return new TokenResponse(mt.getMemberId(), mt.getToken(), mt.getExpiresAt() != null ? mt.getExpiresAt().toString() : null);
    }

    /**
     * Uses AppConfig entry with id 'entity' by convention to fetch endpoint/credentials.
     * If no config found, fallback to environment-configured endpoints.
     */
    private TokenResponse obtainAndPersistEntityToken(String memberId, MemberToken mt) {
        try {
            AppConfig cfg = appConfigRepository.findByIdKey("entity").orElse(null);

            String endpoint = cfg != null && cfg.getEndpoint() != null ? cfg.getEndpoint() : System.getProperty("sahamati.external.entity-token-url");
            String username = cfg != null ? cfg.getUsername() : null;
            String password = cfg != null ? cfg.getPassword() : null;
            String clientSecret = cfg != null ? cfg.getClientSecret() : null;

            if (endpoint == null) {
                throw new IllegalStateException("Entity token endpoint not configured (app_config 'entity' or application.properties)");
            }

            // Build request payload according to central-registry specification.
            // The exact JSON keys depend on the external API; adapt this implementation to the real API.
            String requestBody = mapper.createObjectNode()
                    .put("memberId", memberId)
                    .put("clientSecret", clientSecret != null ? clientSecret : "")
                    .toString();

            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody));

            // If username/password provided, set Basic auth
            if (username != null && password != null) {
                String basic = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
                reqBuilder.header("Authorization", "Basic " + basic);
            }

            HttpRequest request = reqBuilder.build();
            LOG.debugf("Calling entity token endpoint %s for memberId %s", endpoint, memberId);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                JsonNode json = mapper.readTree(response.body());
                // The external API contract may return token and expiry seconds — adapt parsing accordingly.
                String token = json.has("token") ? json.get("token").asText() : json.path("access_token").asText(null);
                long expiresInSeconds = json.has("expires_in") ? json.get("expires_in").asLong(3600) : 3600L;

                OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(expiresInSeconds);

                mt.setToken(token);
                mt.setExpiresAt(expiresAt);
                memberTokenRepository.persist(mt);

                LOG.infof("Obtained new token for memberId=%s, expiresAt=%s", memberId, expiresAt);

                return new TokenResponse(memberId, token, expiresAt.toString());
            } else {
                LOG.errorf("Failed to obtain entity token from endpoint. status=%d body=%s", status, response.body());
                throw new RuntimeException("Failed to obtain entity token: HTTP " + status);
            }

        } catch (Exception e) {
            LOG.error("Error obtaining/persisting entity token", e);
            throw new RuntimeException("Unable to generate token", e);
        }
    }

    // ---- Helper methods for User Token, Entity Secret Read, Entity Secret Reset ----
    // These methods illustrate how to call external endpoints; they are not exposed as REST endpoints here.
    public String generateUserToken(String configId, String username, String password) {
        // Fetch config for 'user' if available
        AppConfig cfg = appConfigRepository.findByIdKey(configId).orElse(null);
        String endpoint = cfg != null && cfg.getEndpoint() != null ? cfg.getEndpoint() : System.getProperty("sahamati.external.user-token-url");
        // Build and call external endpoint (omitted detailed parsing here)
        // For demonstration, returns a dummy token
        return "user-token-" + UUID.randomUUID();
    }

    public String readEntitySecret(String memberId) {
        // Fetch config 'secret' for endpoint and credentials
        AppConfig cfg = appConfigRepository.findByIdKey("secret").orElse(null);
        String endpoint = cfg != null ? cfg.getEndpoint() : System.getProperty("sahamati.external.entity-secret-read-url");
        // Real implementation: call endpoint with appropriate auth and return secret
        return "PLACEHOLDER_SECRET_FOR_" + memberId;
    }

    @Transactional
    public boolean resetEntitySecret(String memberId) {
        // Call external reset endpoint, update config/db if necessary
        LOG.infof("Resetting secret for memberId=%s (demo stub)", memberId);
        // In real implementation, call external API and store new secret in config table if returned
        AppConfig cfg = appConfigRepository.findByIdKey("entity").orElse(null);
        if (cfg != null) {
            cfg.setClientSecret("new-secret-" + UUID.randomUUID());
            appConfigRepository.persist(cfg);
            return true;
        }
        return false;
    }
}
