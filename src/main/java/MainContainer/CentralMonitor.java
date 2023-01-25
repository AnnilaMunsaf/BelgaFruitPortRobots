package MainContainer;
import java.util.ArrayList;
import jade.core.AID;
import jade.core.Agent;
import jade.core.AgentContainer;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ActionExecutor;
import jade.core.behaviours.OutcomeManager;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.JADEAgentManagement.CreateAgent;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.lang.acl.ACLMessage;
import util.JsonCreator;
import util.WorkItem;
import util.TagIdMqtt;
import org.eclipse.paho.client.mqttv3.*;




public class CentralMonitor extends Agent {
    ArrayList<WorkItem> workItems = new ArrayList<WorkItem>();

    // INVARIANT FOR THESE 3 ARRAYS: INTERSECTION IS EMPTY
    ArrayList<TagIdMqtt> idleRobots = new ArrayList<TagIdMqtt>();
    ArrayList<TagIdMqtt> busyRobots = new ArrayList<TagIdMqtt>();    
    ArrayList<TagIdMqtt> chargingRobots = new ArrayList<TagIdMqtt>();   

    ArrayList<TagIdMqtt> stoppedRobots = new ArrayList<TagIdMqtt>();   

    @Override
    public void setup() {
        addBehaviour(messageHandler);
        addBehaviour(scheduler);
        addBehaviour(collisionDetector);
        addBehaviour(collisionReleaser);
        workItems.add(new WorkItem(config.testPoints.get("A"), config.testPoints.get("C")));
        workItems.add(new WorkItem(config.testPoints.get("D"), config.testPoints.get("B")));
        workItems.add(new WorkItem(config.testPoints.get("E"), config.testPoints.get("F")));


    }


    CyclicBehaviour messageHandler = new CyclicBehaviour() {
        @Override
        public void action() {
            ACLMessage message=receive();

            // REGISTRATION REQUEST
            if (message!=null && JsonCreator.parseMessageType(message.getContent()).equals("registration")) {
                System.out.print("Reading a registration request\n");
                // RETRIEVE ROBOT ID
                String robot_id = JsonCreator.parseRegistrationId(message.getContent());
                
                // CREATE TWIN AGENT AND SEND ID TO TWIN
                createTwin(robot_id);
                //sendMessage("RobotTwin-"+robot_id, JsonCreator.createIdUpdateMessage(robot_id)); // THIS DOESNT WORK -> GONNA DO IT MANUALLY

                // SEND AN ACKNOWLEDGMENT TO THE ROBOT AGENT
                sendMessage("RobotAgent-"+robot_id, JsonCreator.createRegistrationAck());
        
                try {
                    idleRobots.add(new TagIdMqtt(robot_id));
                }
                catch (MqttException me) {
                    System.out.println("Something Went Wrong");
                }
            }

            else if (message!=null && JsonCreator.parseMessageType(message.getContent()).equals("workItemFinished")) {
                String robot_id = JsonCreator.parseWorkItemFinished(message.getContent());
                int index = findBusyRobot(robot_id);
                idleRobots.add(busyRobots.remove(index));
            }
            else if (message!=null && JsonCreator.parseMessageType(message.getContent()).equals("chargingNotification")) {
                String robot_id = JsonCreator.parseWorkItemFinished(message.getContent());
                int index = findBusyRobot(robot_id);
                if (index > -1) {
                    chargingRobots.add(busyRobots.remove(index));
                }
                else {
                    index = findIdleRobot(robot_id);
                    chargingRobots.add(idleRobots.remove(index));
                }
            }
            else if (message!=null && JsonCreator.parseMessageType(message.getContent()).equals("chargingFinishedNotification")) {
                String robot_id = JsonCreator.parseWorkItemFinished(message.getContent());
                int index = findChargingRobot(robot_id);
                idleRobots.add(chargingRobots.remove(index));
            }
        }
    };

    TickerBehaviour scheduler = new TickerBehaviour(this, 5000) {
        @Override
        public void onTick() {
            if (!(workItems.isEmpty() || idleRobots.isEmpty())) {
                // GET WORK ITEM
                WorkItem toDoWorkItem = workItems.remove(0);
                
                // GET ROBOT WHICH IS THE CLOSEST TO THE PICKUP LOCATION OF THE WORK ITEM
                TagIdMqtt toDoRobot = idleRobots.remove(0);
                
                // CREATE MESSAGE
                String msg = JsonCreator.createWorkItemMessage(toDoWorkItem);

                // SEND TO WORK ITEM TO ROBOT TWIN
                sendMessage("RobotTwin-" + toDoRobot.getTagId(), msg);
                
                busyRobots.add(toDoRobot);
            }
        }
    };

    CyclicBehaviour collisionDetector = new CyclicBehaviour() {
        @Override
        public void action() {
            // COLLISION DETECTOR
            if (busyRobots.size() >= 2) {
                System.out.println("Distance between robots: " + busyRobots.get(0).getLocation().dist(busyRobots.get(1).getLocation()));
                if (busyRobots.get(0).getLocation().dist(busyRobots.get(1).getLocation()) < 1000 && stoppedRobots.size() == 0) {
                    System.out.println("We are going to stop");
                    // SEND MESSAGE TO BUSY ROBOT i TO STOP
                    sendMessage("RobotTwin-" + busyRobots.get(0).getTagId(), JsonCreator.createStopOrder());
                    stoppedRobots.add(busyRobots.get(0));
                }
            }
        }
    };
    
    TickerBehaviour collisionReleaser = new TickerBehaviour(this, 5000) {
        @Override
        public void onTick() {
            // RELEASE STOPPED ROBOTS IF NO LONGER DANGER
            ArrayList<TagIdMqtt> newStoppedRobots = new ArrayList<TagIdMqtt>();  
     outer: for (int i = 0; i < stoppedRobots.size(); i++) {
                // IS THERE ANY BUSY ROBOT NEAR?
                for (int j = 0; j < busyRobots.size(); j++) {
                    if (stoppedRobots.get(i).getLocation().dist(busyRobots.get(j).getLocation()) < 700 && !stoppedRobots.get(i).getTagId().equals(busyRobots.get(i).getTagId())) {
                        newStoppedRobots.add(stoppedRobots.get(i));
                        continue outer;
                    }
                }
                sendMessage("RobotTwin-" + stoppedRobots.get(i).getTagId(), JsonCreator.createResumeOrder());
            }
            stoppedRobots = newStoppedRobots;
        }
    };


    void sendMessage(String receiver, String message) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        msg.setContent(message);
        send(msg);
    }

    void createTwin(String twin_id) {
        addBehaviour(new WakerBehaviour(this, 100) {
            @Override
            public void onWake() {
                // Request the AMS to perform the CreateAgent action of the JADEManagementOntology
                // To do this use an ActionExecutor behaviour requesting the CreateAgent action and expecting no result (Void)
                CreateAgent ca = new CreateAgent();
                ca.setAgentName("RobotTwin-" + twin_id);
                ca.setClassName("MainContainer.RobotTwin");
                ca.setContainer(new ContainerID(AgentContainer.MAIN_CONTAINER_NAME, null));
                ActionExecutor<CreateAgent, Void> ae = new ActionExecutor<CreateAgent, Void>(ca, JADEManagementOntology.getInstance(), getAMS()) {
                    @Override
                    public int onEnd() {
                        int ret = super.onEnd();
                        if (getExitCode() == OutcomeManager.OK) {
                            // Creation successful
                            System.out.println("Twin successfully created");
                        }
                        else {
                            // Something went wrong
                            System.out.println("Twin creation error. "+getErrorMsg());
                        }
                        return ret;
                    }
                };
                addBehaviour(ae);
            }
        });
    }

    int findBusyRobot(String robot_id) {
        for (int i = 0; i < busyRobots.size(); i++) {
            if (busyRobots.get(i).getTagId().equals(robot_id)) {
                return i;
            }
        }
        return -1;
    }

    int findIdleRobot(String robot_id) {
        for (int i = 0; i < idleRobots.size(); i++) {
            if (idleRobots.get(i).getTagId().equals(robot_id)) {
                return i;
            }
        }
        return -1;
    }

    int findChargingRobot(String robot_id) {
        for (int i = 0; i < chargingRobots.size(); i++) {
            if (chargingRobots.get(i).getTagId().equals(robot_id)) {
                return i;
            }
        }
        return -1;
    }

    int findStoppedRobots(String robot_id) {
        for (int i = 0; i < stoppedRobots.size(); i++) {
            if (stoppedRobots.get(i).getTagId().equals(robot_id)) {
                return i;
            }
        }
        return -1;
    }
}
