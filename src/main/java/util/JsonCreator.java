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

    public static String createRegistrationAck() {
        JsonObject request = new JsonObject();
        request.addProperty("messageType", "registrationAck");
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

    public static String createSensorsFeedbackMessage(int frontDistance, int leftDistance, int rightDistance) {
        JsonObject message = new JsonObject();
        message.addProperty("messageType", "sensorsFeedback");
        JsonObject data = new JsonObject();
        data.addProperty("frontDistance", frontDistance);
        data.addProperty("leftDistance", leftDistance);
        data.addProperty("rightDistance", rightDistance);
        message.add("data", data);
        return message.toString();
    }

    public static int parseSensorsFrontDistance(String message) {
        JsonObject msg = JsonParser.parseString(message).getAsJsonObject();
        JsonObject data = msg.get("data").getAsJsonObject();
        Integer frontDistance = data.get("frontDistance").getAsInt();
        return frontDistance;
    }

    public static int parseSensorsLeftDistance(String message) {
        JsonObject msg = JsonParser.parseString(message).getAsJsonObject();
        JsonObject data = msg.get("data").getAsJsonObject();
        Integer leftDistance = data.get("leftDistance").getAsInt();
        return leftDistance;
    }

    public static int parseSensorsRightDistance(String message) {
        JsonObject msg = JsonParser.parseString(message).getAsJsonObject();
        JsonObject data = msg.get("data").getAsJsonObject();
        Integer rightDistance = data.get("rightDistance").getAsInt();
        return rightDistance;
    }

    public static String createForwardOrder(int speed) {
        JsonObject message = new JsonObject();
        message.addProperty("messageType", "order");
        JsonObject data = new JsonObject();
        data.addProperty("type", "forward");
        data.addProperty("speed", speed);
        message.add("data", data);
        return message.toString();
    }

    public static String createLeftOrder(int speed) {
        JsonObject message = new JsonObject();
        message.addProperty("messageType", "order");
        JsonObject data = new JsonObject();
        data.addProperty("type", "left");
        data.addProperty("speed", speed);
        message.add("data", data);
        return message.toString();
    }

    public static String createRightOrder(int speed) {
        JsonObject message = new JsonObject();
        message.addProperty("messageType", "order");
        JsonObject data = new JsonObject();
        data.addProperty("type", "right");
        data.addProperty("speed", speed);
        message.add("data", data);
        return message.toString();
    }

    public static String createStopOrder() {
        JsonObject message = new JsonObject();
        message.addProperty("messageType", "order");
        JsonObject data = new JsonObject();
        data.addProperty("type", "stop");
        message.add("data", data);
        return message.toString();
    }

    public static String createBackwardOrder(int speed) {
        JsonObject message = new JsonObject();
        message.addProperty("messageType", "order");
        JsonObject data = new JsonObject();
        data.addProperty("type", "backward");
        data.addProperty("speed", speed);
        message.add("data", data);
        return message.toString();
    }

    public static String createObstacleAvoidingOrder() {
        JsonObject message = new JsonObject();
        message.addProperty("messageType", "order");
        JsonObject data = new JsonObject();
        data.addProperty("type", "obstacleAvoiding");
        message.add("data", data);
        return message.toString();
    }

    public static String parseOrderType(String message) {
        JsonObject msg = JsonParser.parseString(message).getAsJsonObject();
        JsonObject data = msg.get("data").getAsJsonObject();
        String order = data.get("type").getAsString();
        return order;
    }

    public static int parseOrderSpeed(String message) {
        JsonObject msg = JsonParser.parseString(message).getAsJsonObject();
        JsonObject data = msg.get("data").getAsJsonObject();
        Integer speed = data.get("speed").getAsInt();
        return speed;
    }

    

    

    
}
