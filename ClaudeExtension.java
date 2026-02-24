import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * ClaudeExtension provides a simple integration with the Anthropic Claude API.
 * It allows sending messages to Claude and receiving AI-generated responses.
 */
public class ClaudeExtension {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String API_VERSION = "2023-06-01";
    private static final String DEFAULT_MODEL = "claude-opus-4-5";

    private final String apiKey;
    private final String model;

    public ClaudeExtension(String apiKey) {
        this(apiKey, DEFAULT_MODEL);
    }

    public ClaudeExtension(String apiKey, String model) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("API key must not be null or empty");
        }
        this.apiKey = apiKey;
        this.model = model;
    }

    /**
     * Sends a message to Claude and returns the response text.
     *
     * @param userMessage the message to send to Claude
     * @return the response text from Claude
     * @throws IOException if an I/O error occurs during the API call
     */
    public String sendMessage(String userMessage) throws IOException {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("x-api-key", apiKey);
            connection.setRequestProperty("anthropic-version", API_VERSION);
            connection.setDoOutput(true);

            String requestBody = buildRequestBody(userMessage);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return parseResponse(readStream(connection.getInputStream()));
            } else {
                String errorBody = readStream(connection.getErrorStream());
                throw new IOException("API request failed with status " + responseCode + ": " + errorBody);
            }
        } finally {
            connection.disconnect();
        }
    }

    private String buildRequestBody(String userMessage) {
        String escapedMessage = userMessage
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        return "{\"model\":\"" + model + "\","
                + "\"max_tokens\":1024,"
                + "\"messages\":[{\"role\":\"user\",\"content\":\"" + escapedMessage + "\"}]}";
    }

    private String readStream(java.io.InputStream inputStream) throws IOException {
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    private String parseResponse(String responseBody) {
        // Extract text from: {"content":[{"type":"text","text":"..."}],...}
        String marker = "\"text\":\"";
        int start = responseBody.indexOf(marker);
        if (start == -1) {
            return responseBody;
        }
        start += marker.length();
        int end = start;
        while (end < responseBody.length()) {
            char c = responseBody.charAt(end);
            if (c == '\\') {
                end += 2; // skip escaped character
            } else if (c == '"') {
                break;
            } else {
                end++;
            }
        }
        return responseBody.substring(start, end)
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    public static void main(String[] args) throws IOException {
        String apiKey = System.getenv("ANTHROPIC_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Error: ANTHROPIC_API_KEY environment variable is not set.");
            System.exit(1);
        }

        ClaudeExtension claude = new ClaudeExtension(apiKey);
        String prompt = args.length > 0 ? String.join(" ", args) : "Hello! What can you help me with?";

        System.out.println("Sending message to Claude: " + prompt);
        String response = claude.sendMessage(prompt);
        System.out.println("Claude: " + response);
    }
}
