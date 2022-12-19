package MainContainer;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import util.JsonCreator;
import util.TagIdMqtt;
import util.Point2D;
import org.eclipse.paho.client.mqttv3.*;

enum Status {
    picking_up,
    dropping_off,
    idle
}

enum BEHAVIOR {
    LEFT,
    RIGHT,
    FORWARD,
    STOP,
    BACKWARD
}

public class RobotTwin extends Agent{
    // IDs
    String id;
    String targetId;
    
    // STATUS
    Status currentStatus = Status.idle;
    BEHAVIOR lastBehavior = null;

    // TAGS
    TagIdMqtt tag;
    TagIdMqtt targetTag;

    // SENSORS
    int frontDistance;
    int leftDistance;
    int rightDistance;

    // THRESHHOLDS
    int frontThreshold = 30;
    int backwardThreshold = 15;
    int sideThreshold = 15;


    // UTIL
    Point2D target_location;
    long start = System.currentTimeMillis();

    @Override
    public void setup() {
        this.id = "6823";
        this.targetId = "682e";
        try {
            this.tag = new TagIdMqtt(id);
            this.targetTag = new TagIdMqtt(targetId);
        }
        catch (MqttException me) {
            System.out.println("something Went Wrong");
        }
        System.out.print("Digital Twin Created\n");

        addBehaviour(readSensorsFeedback);
    }

    CyclicBehaviour readSensorsFeedback = new CyclicBehaviour() {
        @Override
        public void action() {
            ACLMessage message = receive();
            if (message!=null && JsonCreator.parseMessageType(message.getContent()).equals("sensorsFeedback")) {
                frontDistance = JsonCreator.parseSensorsFrontDistance(message.getContent());
                leftDistance = JsonCreator.parseSensorsLeftDistance(message.getContent());
                rightDistance = JsonCreator.parseSensorsRightDistance(message.getContent());
            }
        }
    };
}
