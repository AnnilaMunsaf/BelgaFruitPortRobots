package util;

import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class JsonCreator {

    // --- REGISTRATION ---
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

    public static String createIdUpdateMessage(String id) {
        JsonObject message = new JsonObject();
        message.addProperty("messageType", "idUpdate");

        JsonObject data = new JsonObject();
        data.addProperty("id", id);

        message.add("data", data);
        return message.toString();
    }

    // WORK ITEM
    public static String createWorkItemMessage(WorkItem workItem) {
        JsonObject message = new JsonObject();
        message.addProperty("messageType", "workItem");
        JsonObject data = new JsonObject();
        data.addProperty("pickupX", workItem.getPickup().x);
        data.addProperty("pickupY", workItem.getPickup().y);
        data.addProperty("dropoffX", workItem.getDropoff().x);
        data.addProperty("dropoffY", workItem.getDropoff().y);

        message.add("data", data);
        return message.toString();
    }

    public static String createWorkItemFinishedMessage(String id) {
        JsonObject message = new JsonObject();
        message.addProperty("messageType", "workItemFinished");

        JsonObject data = new JsonObject();
        data.addProperty("id", id);

        message.add("data", data);
        return message.toString();
    }

    // LOCATION

    public static String createTargetLocationUpdateMessage(Point2D location) {
        JsonObject message = new JsonObject();
        message.addProperty("messageType", "targetLocationUpdate");

        JsonObject data = new JsonObject();
        data.addProperty("x", location.x);
        data.addProperty("y", location.y);

        message.add("data", data);
        return message.toString();
    }
    
    public static String createLocationReachedMessage() {
        JsonObject message = new JsonObject();
        message.addProperty("messageType", "locationReached");
        return message.toString();
    }


    // --- PARSING ---

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

    // REGISTRATION
    public static String parseRegistrationId(String message) {
        JsonObject msg = JsonParser.parseString(message).getAsJsonObject();
        JsonObject data = msg.get("data").getAsJsonObject();
        String id = data.get("id").getAsString();
        return id;
    }

    public static String parseIdUpdate(String message) {
        JsonObject msg = JsonParser.parseString(message).getAsJsonObject();
        JsonObject data = msg.get("data").getAsJsonObject();
        String robot_id = data.get("id").getAsString();
        return robot_id;
    }


    // WORK ITEM
    public static Point2D parseWorkItemPickUp(String message) {
        JsonObject msg = JsonParser.parseString(message).getAsJsonObject();
        JsonObject data = msg.get("data").getAsJsonObject();

        int pickupX = data.get("pickupX").getAsInt();
        int pickupY = data.get("pickupY").getAsInt();

        return new Point2D(pickupX, pickupY);
    }

    public static Point2D parseWorkItemDropOff(String message) {
        JsonObject msg = JsonParser.parseString(message).getAsJsonObject();
        JsonObject data = msg.get("data").getAsJsonObject();

        int dropoffX = data.get("dropoffX").getAsInt();
        int dropoffY = data.get("dropoffY").getAsInt();

        return new Point2D(dropoffX, dropoffY);
    }

    public static String parseWorkItemFinished(String message) {
        JsonObject msg = JsonParser.parseString(message).getAsJsonObject();
        JsonObject data = msg.get("data").getAsJsonObject();

        String robot_id = data.get("id").getAsString();
        return robot_id;
    }

    // LOCATION

    public static Point2D parseTargetLocationUpdate(String message) {
        JsonObject msg = JsonParser.parseString(message).getAsJsonObject();
        JsonObject data = msg.get("data").getAsJsonObject();
        int x = data.get("x").getAsInt();
        int y = data.get("y").getAsInt();

        return new Point2D(x, y);
    }    
}
