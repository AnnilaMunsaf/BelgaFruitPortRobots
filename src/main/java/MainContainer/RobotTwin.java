package MainContainer;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import util.JsonCreator;
import util.TagIdMqtt;
import util.WorkItem;
import util.Point2D;
import org.eclipse.paho.client.mqttv3.*;

enum Status {
    picking_up,
    dropping_off,
    idle
}
public class RobotTwin extends Agent{
    // IDs
    String id;
    String targetId;
    
    // STATUS
    Status currentStatus = Status.idle;

    // TAGS
    TagIdMqtt tag;

    // WORK ITEMS
    WorkItem currentWorkItem = null;

    // SENSORS
    int frontDistance;
    int leftDistance;
    int rightDistance;


    @Override
    public void setup() {
        this.id = "6823";
        try {
            this.tag = new TagIdMqtt(id);
        }
        catch (MqttException me) {
            System.out.println("something Went Wrong");
        }
        System.out.print("Digital Twin Created\n");

        addBehaviour(messageHandler);
    }

    CyclicBehaviour messageHandler = new CyclicBehaviour() {
        @Override
        public void action() {
            ACLMessage message = receive();
            if (message!=null && JsonCreator.parseMessageType(message.getContent()).equals("sensorsFeedback")) {
                frontDistance = JsonCreator.parseSensorsFrontDistance(message.getContent());
                leftDistance = JsonCreator.parseSensorsLeftDistance(message.getContent());
                rightDistance = JsonCreator.parseSensorsRightDistance(message.getContent());
            }
            else if (message!=null && JsonCreator.parseMessageType(message.getContent()).equals("workItem")) {
                Point2D pickup = JsonCreator.parseWorkItemPickUp(message.getContent());
                Point2D dropoff = JsonCreator.parseWorkItemDropOff(message.getContent());
                currentWorkItem = new WorkItem(pickup, dropoff);
                currentStatus = Status.picking_up;
                sendMessage("RobotAgent-" + id, JsonCreator.createTargetLocationUpdateMessage(currentWorkItem.getPickup()));
            }
            else if (message != null && JsonCreator.parseMessageType(message.getContent()).equals("locationReached")) {
                if (currentStatus == Status.picking_up) {
                    currentStatus = Status.dropping_off;
                    sendMessage("RobotAgent-" + id, JsonCreator.createTargetLocationUpdateMessage(currentWorkItem.getDropoff()));
                }
                else if (currentStatus == Status.dropping_off) {
                    currentStatus = Status.idle;
                    currentWorkItem = null;
                    sendMessage("CentralMonitor", JsonCreator.createWorkItemFinishedMessage(id));
                }
            };
        }
    };

    void sendMessage(String receiver, String message) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        msg.setContent(message);
        send(msg);
    }

}
