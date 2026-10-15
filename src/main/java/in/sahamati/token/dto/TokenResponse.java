package in.sahamati.token.dto;

public class TokenResponse {
    private String memberId;
    private String token;
    private String expiresAt;

    public TokenResponse() {}

    public TokenResponse(String memberId, String token, String expiresAt) {
        this.memberId = memberId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
}
