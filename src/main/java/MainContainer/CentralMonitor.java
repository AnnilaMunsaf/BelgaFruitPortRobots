package MainContainer;
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
import jade.security.Credentials;


public class CentralMonitor extends Agent {
    int robots_count = 0;

    @Override
    public void setup() {
        addBehaviour(registrationUnit);
    }


    CyclicBehaviour registrationUnit = new CyclicBehaviour() {
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
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID("RobotAgent-"+robot_id, AID.ISLOCALNAME));
                msg.setContent("RegistrationAck"); // THIS NEEDS TO BE SOME JSON STRING
                send(msg);
            }
        }
    };


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
