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
    
    // STATUS
    Status currentStatus = Status.idle;

    // WORK ITEMS
    WorkItem currentWorkItem = null;

    @Override
    public void setup() {
        addBehaviour(messageHandler);
        System.out.print("Digital Twin Created\n");
    }

    CyclicBehaviour messageHandler = new CyclicBehaviour() {
        @Override
        public void action() {
            ACLMessage message = receive();
            // ID UPDATE (REGISTRATION)
            if (message!=null && JsonCreator.parseMessageType(message.getContent()).equals("idUpdate")) {
                String robot_id = JsonCreator.parseIdUpdate(message.getContent());
                id = robot_id;
            }
            // WORK ITEM RECEIVED
            else if (message!=null && JsonCreator.parseMessageType(message.getContent()).equals("workItem")) {
                Point2D pickup = JsonCreator.parseWorkItemPickUp(message.getContent());
                Point2D dropoff = JsonCreator.parseWorkItemDropOff(message.getContent());
                currentWorkItem = new WorkItem(pickup, dropoff);
                currentStatus = Status.picking_up;
                sendMessage("RobotAgent-" + id, JsonCreator.createTargetLocationUpdateMessage(currentWorkItem.getPickup()));
            }
            // LOCATION REACHED
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
