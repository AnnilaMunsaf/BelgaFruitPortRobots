package util;
import org.antlr.works.stats.StatisticsManager;

import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lejos.robotics.geometry.Point;

public final class JsonCreator {

    // MESSAGING
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

    public static String createWorkItemFinishedMessage(String id) {
        JsonObject message = new JsonObject();
        message.addProperty("messageType", "workItemFinished");

        JsonObject data = new JsonObject();
        data.addProperty("id", id);

        message.add("data", data);
        return message.toString();
    }

    // PARSING

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

    public static Point2D parseTargetLocationUpdate(String message) {
        JsonObject msg = JsonParser.parseString(message).getAsJsonObject();
        JsonObject data = msg.get("data").getAsJsonObject();
        int x = data.get("x").getAsInt();
        int y = data.get("y").getAsInt();

        return new Point2D(x, y);
    }

}
