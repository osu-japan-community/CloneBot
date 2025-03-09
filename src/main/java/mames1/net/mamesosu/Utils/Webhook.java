package mames1.net.mamesosu.Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.entities.Message;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public abstract class Webhook {

    public static void sendAndCopyWebhookMessage(String webhook, String threadID, Message m) throws IOException, InterruptedException {

        String url = webhook;

        if (threadID != null) {
            url += "?thread_id=" + threadID;
        }

        String avatar = m.getAuthor().getAvatarUrl();

        if (avatar == null) {
            avatar = "https://cdn.logojoy.com/wp-content/uploads/20210422095037/discord-mascot.png";
        }

        String jsonPayload = String.format("""
                {
                    "content": "%s",
                    "username": "%s",
                    "avatar_url": "%s"
                }
                """, m.getContentRaw().trim().replaceAll("[ \\t]+", " ").replaceAll("\\s*\\n\\s*", "\\\\n"), m.getAuthor().getEffectiveName(), avatar);

        JsonObject jsonObject = new JsonObject();
        Scanner scanner = new Scanner(jsonPayload);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                int posDoublePoint = line.indexOf(":");
                if (posDoublePoint > 0) {
                    String key = line.substring(0, posDoublePoint).trim().replaceAll("^\"|\"$", "");
                    String value = line.substring(posDoublePoint + 1).trim();

                    if (!value.isEmpty()) {
                        value = value.replaceAll("^\"|\"$", "");
                        value = value.replace("\\", "\\\\");
                        value = value.replace("\"", "\\\"");
                        jsonObject.addProperty(key, value);
                        continue;
                    }
                }
            }
        }
        scanner.close();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(jsonObject);

        System.out.println(jsonPayload);

        if (!m.getAttachments().isEmpty()) {
            return;
        }

        HttpClient client = HttpClient.newHttpClient();

        int attempt = 0;
        int maxRetries = 10;

        while (attempt < maxRetries) {
            attempt++;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();

            if (statusCode == 204) {
                break;
            } else if (statusCode == 429) {
                // レートタイム0.3s
                Thread.sleep(300L);
            } else {
                break;
            }
        }
    }
}
