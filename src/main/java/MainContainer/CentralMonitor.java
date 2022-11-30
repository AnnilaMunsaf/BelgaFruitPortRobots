package MainContainer;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class CentralMonitor extends Agent {
    @Override
    public void setup() {
        addBehaviour(registrationUnit);
    }


    CyclicBehaviour registrationUnit = new CyclicBehaviour() {
        @Override
        public void action() {
            ACLMessage message=receive();
            if (message!=null && message.getContent().equals("Registration")) {
                // CREATE TWIN AGENT AND ID N

                
                // SEND ACK WITH CORRESPONDING ID
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID("RobotAgent-N", AID.ISLOCALNAME));
                msg.setContent("RegistrationAck"); // THIS NEEDS TO BE SOME JSON STRING
                send(msg);
            }
        }
    };
}
