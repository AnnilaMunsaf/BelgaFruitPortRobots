package MainContainer;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import lejos.utility.Delay;


public class CentralMonitor extends Agent {
    @Override
    public void setup() {
        addBehaviour(registrationUnit);
    }


    CyclicBehaviour registrationUnit = new CyclicBehaviour() {
        @Override
        public void action() {
            ACLMessage message=receive();
            if (getMessageType(message) == "Registration") {
                // CHECK IF NOT REGISTERED ALREADY
                
                // CREATE TWIN AGENT
                
                // SEND ACK (WITH TWIN_ID)?
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID("RobotAgent-N", AID.ISLOCALNAME));
                msg.setContent("RegistrationAck"); // THIS NEEDS TO BE SOME JSON STRING
                send(msg);

            }
        }
    };
}
