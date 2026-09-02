import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.*;
import java.util.Base64;

public class Main {

    // Keep credentials out of source control. Set these as environment variables.
    private static final String API_KEY = System.getenv("KRAKEN_API_KEY");
    private static final String API_SECRET = System.getenv("KRAKEN_API_SECRET");

    public static void main(String[] args) {
        if (API_KEY == null || API_KEY.isBlank() || API_SECRET == null || API_SECRET.isBlank()) {
            System.err.println("Missing Kraken credentials. Set KRAKEN_API_KEY and KRAKEN_API_SECRET environment variables.");
            System.exit(1);
        }

        try {
            // 1. AddOrder: validation-only sell order. It will NOT execute a real trade.
            String addOrderUrl = "https://api.kraken.com/0/private/AddOrder";
            String addOrderPath = "/0/private/AddOrder";

            String nonce = String.valueOf(System.currentTimeMillis());

            Map<String, String> addOrderData = new LinkedHashMap<>();
            addOrderData.put("nonce", nonce);
            addOrderData.put("ordertype", "limit");
            addOrderData.put("type", "sell");
            addOrderData.put("volume", "1.0");
            addOrderData.put("pair", "XBTUSD");
            addOrderData.put("price", "50000");
            addOrderData.put("validate", "true");

            String postData = getPostData(addOrderData);
            String signature = getKrakenSignature(addOrderPath, postData, nonce);

            System.out.println("AddOrder Signature: " + signature);
            System.out.println("AddOrder Body: " + postData);

            String addOrderResponse = sendKrakenRequest(addOrderUrl, postData, signature);
            System.out.println("AddOrder Response: " + addOrderResponse);

            // Ensure the next nonce is greater than the first one.
            Thread.sleep(2);

            // 2. Balance
            String balanceUrl = "https://api.kraken.com/0/private/Balance";
            String balancePath = "/0/private/Balance";
            String balanceNonce = String.valueOf(System.currentTimeMillis());

            String balanceData = "nonce=" + URLEncoder.encode(balanceNonce, StandardCharsets.UTF_8);
            String balanceSignature = getKrakenSignature(balancePath, balanceData, balanceNonce);

            System.out.println("Balance Signature: " + balanceSignature);
            System.out.println("Balance Body: " + balanceData);

            String balanceResponse = sendKrakenRequest(balanceUrl, balanceData, balanceSignature);
            System.out.println("Balance Response: " + balanceResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getPostData(Map<String, String> params) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (result.length() > 0) result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return result.toString();
    }

    private static String getKrakenSignature(String urlPath, String postData, String nonce) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] hash = sha256.digest((nonce + postData).getBytes(StandardCharsets.UTF_8));

        byte[] pathAndHash = concatenate(urlPath.getBytes(StandardCharsets.UTF_8), hash);
        byte[] decodedSecret = Base64.getDecoder().decode(API_SECRET);

        SecretKeySpec key = new SecretKeySpec(decodedSecret, "HmacSHA512");
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(key);

        byte[] macHash = mac.doFinal(pathAndHash);
        return Base64.getEncoder().encodeToString(macHash);
    }

    private static byte[] concatenate(byte[] a, byte[] b) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(a);
        outputStream.write(b);
        return outputStream.toByteArray();
    }

    private static String sendKrakenRequest(String urlString, String postData, String signature) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("API-Key", API_KEY);
        connection.setRequestProperty("API-Sign", signature);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(postData.getBytes(StandardCharsets.UTF_8));
        }

        int statusCode = connection.getResponseCode();
        InputStream stream = statusCode < 400 ? connection.getInputStream() : connection.getErrorStream();

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        return "HTTP " + statusCode + " - " + response;
    }
}
