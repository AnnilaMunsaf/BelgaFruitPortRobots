package MainContainer;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.introspection.RemovedBehaviour;
import jade.lang.acl.ACLMessage;
import util.JsonCreator;
import util.WorkItem;
import util.Point2D;

enum Status {
    picking_up,
    dropping_off,
    idle,
    triaging,
    charging
}

public class RobotTwin extends Agent{
    // IDs
    String id = "6a75";
    
    // STATUS
    Status currentStatus = Status.idle;

    // WORK ITEMS
    WorkItem currentWorkItem = null;

    // BATTERY
    int battery = config.batteryTime;


    @Override
    public void setup() {
        addBehaviour(messageHandler);
        addBehaviour(batteryController);
        System.out.print("Digital Twin Created\n");
    }

    CyclicBehaviour messageHandler = new CyclicBehaviour() {
        @Override
        public void action() {
            ACLMessage message = receive();
            System.out.println(currentStatus.name());
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
                    if (config.rng.nextDouble() <= config.rottenProbability) {
                        currentStatus = Status.triaging;
                    }
                    else {
                        currentStatus = Status.dropping_off;
                    }
                    addBehaviour(pickupDelay);
                }
                else if (currentStatus == Status.dropping_off) {
                    currentStatus = Status.idle;
                    currentWorkItem = null;
                    addBehaviour(workItemFinishedDelay);
                }
                else if (currentStatus == Status.triaging) {
                    currentStatus = Status.idle;
                    currentWorkItem = null;
                    addBehaviour(workItemFinishedDelay);
                }
                else if (currentStatus == Status.charging) {
                    addBehaviour(resetBattery);
                }
            }
            // COLLISION DETECTION
            else if (message != null && JsonCreator.parseMessageType(message.getContent()).equals("stop")) {
                sendMessage("RobotAgent-" + id, JsonCreator.createStopOrder());
            }
            else if (message != null && JsonCreator.parseMessageType(message.getContent()).equals("resume")) {
                sendMessage("RobotAgent-" + id, JsonCreator.createResumeOrder());
            }
        }
    };

    TickerBehaviour batteryController = new TickerBehaviour(this, 1000) {
        @Override
        public void onTick() {
            battery--;
            if (battery == 0) {
                if (currentStatus == Status.idle) {
                    // notify central monitor
                    sendMessage("CentralMonitor", JsonCreator.createChargingNotification(id));
                    // send robot towards charging station
                    sendMessage("RobotAgent-" + id, JsonCreator.createTargetLocationUpdateMessage(config.charging_stations.get(id)));
                    currentStatus = Status.charging;
                }
                removeBehaviour(batteryController);
            }
        } 
    };

    WakerBehaviour resetBattery = new WakerBehaviour(this, config.chargingDuration) {
        protected void handleElapsedTimeout() {
            // notify central monitor
            sendMessage("CentralMonitor", JsonCreator.chargingFinishedNotification(id));
            battery = config.batteryTime;
            currentStatus = Status.idle;
            addBehaviour(batteryController);
            removeBehaviour(resetBattery);
        }
    };

    WakerBehaviour workItemFinishedDelay = new WakerBehaviour(this, 5000) {
        protected void handleElapsedTimeout() {
            if (battery == 0) {
                sendMessage("CentralMonitor", JsonCreator.createChargingNotification(id));
                sendMessage("RobotAgent-" + id, JsonCreator.createTargetLocationUpdateMessage(config.charging_stations.get(id)));
                currentStatus = Status.charging;
            }
            else {
                sendMessage("CentralMonitor", JsonCreator.createWorkItemFinishedMessage(id));
            }
            removeBehaviour(workItemFinishedDelay);
        }
    };


    WakerBehaviour pickupDelay = new WakerBehaviour(this, 5000) {
        protected void handleElapsedTimeout() {
            if (currentStatus == Status.dropping_off) {
                sendMessage("RobotAgent-" + id, JsonCreator.createTargetLocationUpdateMessage(currentWorkItem.getDropoff()));
            }
            else if (currentStatus == Status.triaging) {
                sendMessage("RobotAgent-" + id, JsonCreator.createTargetLocationUpdateMessage(config.triaging_station));
            }
            removeBehaviour(pickupDelay);
        }
    };

    void sendMessage(String receiver, String message) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        msg.setContent(message);
        send(msg);
    }

}
