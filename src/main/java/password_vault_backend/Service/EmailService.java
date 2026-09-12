package password_vault_backend.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class EmailService {

    @Value("${BREVO_API_KEY:}")
    private String brevoApiKey;

    @Value("${MAIL_USERNAME:balajigbalaji4321@gmail.com}")
    private String senderEmail;

    /**
     * Send a plain-text email via Brevo HTTPS API (Port 443).
     */
    public void sendEmail(String toEmail, String subject, String body) {
        String htmlBody = "<div style=\"font-family:sans-serif;font-size:15px;line-height:1.6;\">"
                + body.replace("\n", "<br/>")
                + "</div>";
        sendViaBrevo(toEmail, subject, htmlBody);
    }

    /**
     * Send an HTML-formatted email via Brevo HTTPS API.
     */
    public void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        sendViaBrevo(toEmail, subject, htmlBody);
    }

    private void sendViaBrevo(String toEmail, String subject, String contentHtml) {
        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            System.err.println("BREVO_API_KEY is not configured. Skipping email dispatch.");
            return;
        }

        try {
            String escapedSubject = subject.replace("\\", "\\\\").replace("\"", "\\\"");
            String escapedContent = contentHtml.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", "");

            String jsonPayload = """
                {
                  "sender": {"name": "VaultKeep", "email": "%s"},
                  "to": [{"email": "%s"}],
                  "subject": "%s",
                  "htmlContent": "%s"
                }
                """.formatted(senderEmail, toEmail, escapedSubject, escapedContent);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("accept", "application/json")
                    .header("api-key", brevoApiKey.trim())
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Email delivered successfully via Brevo to: " + toEmail);
            } else {
                System.err.println("Brevo API response (" + response.statusCode() + "): " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Failed to send email via Brevo: " + e.getMessage());
        }
    }

    // ── Email Templates for NotificationService ─────────────────────────────

    private String buildEmailWrapper(String title, String contentHtml) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background-color:#f4f6f9;font-family:'Segoe UI',Tahoma,Geneva,Verdana,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f9;padding:30px 0;">
                    <tr><td align="center">
                      <table width="600" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08);">
                        <tr>
                          <td style="background:linear-gradient(135deg,#1a1a2e,#16213e);padding:28px 36px;text-align:center;">
                            <h1 style="color:#e94560;margin:0;font-size:22px;letter-spacing:1px;">VaultKeep</h1>
                            <p style="color:#a8b2d1;margin:6px 0 0;font-size:13px;">Secure Password Manager</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:36px;">
                            <h2 style="color:#1a1a2e;margin:0 0 18px;font-size:20px;">%s</h2>
                            %s
                          </td>
                        </tr>
                        <tr>
                          <td style="background-color:#f8f9fb;padding:20px 36px;text-align:center;border-top:1px solid #eee;">
                            <p style="color:#999;font-size:12px;margin:0;">
                              This is an automated security notification from VaultKeep.<br>
                              Do not share this email with anyone.
                            </p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(title, contentHtml);
    }

    private String infoRow(String label, String value) {
        return """
                <tr>
                  <td style="padding:6px 0;color:#666;font-size:13px;width:120px;vertical-align:top;">%s</td>
                  <td style="padding:6px 0;color:#1a1a2e;font-size:13px;font-weight:500;">%s</td>
                </tr>
                """.formatted(label, value);
    }

    public void sendLoginSuccessEmail(String toEmail, String timestamp, String deviceInfo, String ipAddress) {
        String content = """
                <p style="color:#444;font-size:14px;line-height:1.6;">A new login was detected on your VaultKeep account.</p>
                <table style="margin:18px 0;background:#f8f9fb;border-radius:8px;padding:16px;width:100%%;">
                  %s
                  %s
                  %s
                </table>
                <p style="color:#888;font-size:12px;margin-top:18px;">
                  If this wasn't you, please change your password immediately.
                </p>
                """.formatted(
                infoRow("Date & Time:", timestamp),
                infoRow("Device:", deviceInfo),
                infoRow("IP Address:", ipAddress)
        );
        sendHtmlEmail(toEmail, "VaultKeep — New Login Detected", buildEmailWrapper("New Login Detected", content));
    }

    public void sendFailedLoginAlertEmail(String toEmail, int failedAttempts, String timestamp) {
        String content = """
                <p style="color:#c0392b;font-size:14px;line-height:1.6;">
                  <strong>Security Alert:</strong> Multiple failed login attempts were detected on your VaultKeep account.
                </p>
                <table style="margin:18px 0;background:#fdf2f2;border:1px solid #f5c6cb;border-radius:8px;padding:16px;width:100%%;">
                  %s
                  %s
                </table>
                """.formatted(
                infoRow("Attempts:", String.valueOf(failedAttempts)),
                infoRow("Detected at:", timestamp)
        );
        sendHtmlEmail(toEmail, "VaultKeep — Security Alert: Failed Login Attempts", buildEmailWrapper("Failed Login Attempts Detected", content));
    }

    public void sendCredentialSharedEmail(String toEmail, String senderName, String credentialName) {
        String content = """
                <p style="color:#444;font-size:14px;line-height:1.6;">
                  A credential has been securely shared with you by <strong>%s</strong>.
                </p>
                <table style="margin:18px 0;background:#eaf6f0;border:1px solid #c3e6cb;border-radius:8px;padding:16px;width:100%%;">
                  %s
                </table>
                <p style="color:#888;font-size:12px;margin-top:12px;">
                  For security, the actual password is never included in this email.
                </p>
                """.formatted(
                senderName,
                infoRow("Credential:", credentialName)
        );
        sendHtmlEmail(toEmail, "VaultKeep — Credential Shared With You", buildEmailWrapper("Credential Shared With You", content));
    }

    public void sendPasswordExpiringEmail(String toEmail, String accountName) {
        String content = """
                <p style="color:#444;font-size:14px;line-height:1.6;">
                  Your saved password for <strong>%s</strong> is due for an update.
                </p>
                <table style="margin:18px 0;background:#fff8e1;border:1px solid #ffe082;border-radius:8px;padding:16px;width:100%%;">
                  %s
                </table>
                """.formatted(
                accountName,
                infoRow("Account:", accountName)
        );
        sendHtmlEmail(toEmail, "VaultKeep — Password Update Reminder", buildEmailWrapper("Password Update Reminder", content));
    }

    public void sendSuspiciousRiskEmail(String toEmail, String activityDescription, String timestamp) {
        String content = """
                <p style="color:#c0392b;font-size:14px;line-height:1.6;">
                  <strong>Suspicious activity</strong> was detected on your VaultKeep account.
                </p>
                <table style="margin:18px 0;background:#fdf2f2;border:1px solid #f5c6cb;border-radius:8px;padding:16px;width:100%%;">
                  %s
                  %s
                </table>
                """.formatted(
                infoRow("Details:", activityDescription),
                infoRow("Time:", timestamp)
        );
        sendHtmlEmail(toEmail, "VaultKeep — Suspicious Activity Detected", buildEmailWrapper("Suspicious Activity Detected", content));
    }
}