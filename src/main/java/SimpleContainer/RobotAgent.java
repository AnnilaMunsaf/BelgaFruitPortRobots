package SimpleContainer;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import lejos.utility.Delay;

public class RobotAgent extends Agent {

    int ID;

    @Override
    public void setup() {
        addBehaviour(registration);
    }

    CyclicBehaviour registration = new CyclicBehaviour() {
        @Override
        public void action() {
            ACLMessage message = receive();

            // ACKNOWLEDGE RECEIVED
            if (getMessageType(message) == "RegistrationAck") {
                removeBehaviour(registration);
            }

            // SEND REGISTRATION REQUEST
            else {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID("CentralMonitor", AID.ISLOCALNAME));
                msg.setContent("Registration"); // THIS NEEDS TO BE SOME JSON STRING
                send(msg);
            }
        }
    };


}