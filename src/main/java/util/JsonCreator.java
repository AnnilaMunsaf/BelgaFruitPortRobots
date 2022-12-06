package util;
import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class JsonCreator {
    public static String createRegistrationRequest(String id) {
        JsonObject request = new JsonObject();
        request.addProperty("messageType", "registration");
        JsonObject data = new JsonObject();
        data.addProperty("id", id);
        request.add("data", data);
        return request.toString();
    }

    public static String parseMessageType(String message) {
        try {
            JsonObject msg = JsonParser.parseString(message).getAsJsonObject();
            String msgType = msg.get("messageType").getAsString();
            return msgType;
        }
        catch (Exception e) {
            return "null";
        }
    }

    public static String parseRegistrationId(String message) {
        JsonObject msg = JsonParser.parseString(message).getAsJsonObject();
        JsonObject data = msg.get("data").getAsJsonObject();
        String id = data.get("id").getAsString();
        return id;
    }
}
