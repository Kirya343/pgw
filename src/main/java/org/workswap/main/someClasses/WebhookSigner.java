package org.workswap.main.someClasses;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class WebhookSigner {

    private static final String SECRET = "utKlZfl2YNBJIMDiAWNOW35p991P1vm8U";

    public static String generateSignature(String body) {
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secretKey);

            byte[] hashBytes = hmacSha256.doFinal(body.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }

            return "sha256=" + hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC-SHA256 signature", e);
        }
    }
}