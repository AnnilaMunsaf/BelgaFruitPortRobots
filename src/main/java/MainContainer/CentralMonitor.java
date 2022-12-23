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
import jade.security.Credentials;


public class CentralMonitor extends Agent {
    ArrayList<WorkItem> workItems = new ArrayList<WorkItem>();
    ArrayList<String> freeRobots = new ArrayList<String>();
    ArrayList<String> busyRobots = new ArrayList<String>();


    @Override
    public void setup() {
        addBehaviour(messageHandler);
        addBehaviour(assignWorkItems);
        //workItems.add(new WorkItem(15285, 14860, 14180, 13756));
        workItems.add(new WorkItem(12655,14880, 14830, 13837 ));

    }


    CyclicBehaviour messageHandler = new CyclicBehaviour() {
        @Override
        public void action() {
            ACLMessage message=receive();

            if (message!=null && JsonCreator.parseMessageType(message.getContent()).equals("registration")) {
                System.out.print("Reading a registration request\n");
                // RETRIEVE ROBOT ID
                String robot_id = JsonCreator.parseRegistrationId(message.getContent());
                // CREATE TWIN AGENT AND ID N
                createTwin(robot_id);
                // SEND AN ACKNOWLEDGMENT
                sendMessage("RobotAgent-"+robot_id, JsonCreator.createRegistrationAck());

                freeRobots.add(robot_id);
            }
            else if (message!=null && JsonCreator.parseMessageType(message.getContent()).equals("workItemFinished")) {
                String robot_id = JsonCreator.parseWorkItemFinished(message.getContent());
                freeRobots.add(robot_id);
                busyRobots.remove(robot_id);
            }
        }
    };

    TickerBehaviour assignWorkItems = new TickerBehaviour(this, 5000) {
        @Override
        public void onTick() {
            if (!workItems.isEmpty() && !freeRobots.isEmpty()) {
                // GET WORK ITEM + SOME FREE ROBOT ID
                WorkItem toDoWorkItem = workItems.remove(0);
                String toDoRobotId = freeRobots.remove(0);
                
                // MAKE MESSAGE
                String msg = JsonCreator.createWorkItemMessage(toDoWorkItem);

                // SEND TO ROBOT ID TWIN
                sendMessage("RobotTwin-" + toDoRobotId, msg);
                
                busyRobots.add(toDoRobotId);
            }
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
}
