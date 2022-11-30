package SimpleContainer;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class RobotAgent extends Agent {

    int ID;

    @Override
    public void setup() {
        addBehaviour(registration);
    }

    TickerBehaviour registration = new TickerBehaviour(this, 1000) {
        @Override
        public void onTick() {
            ACLMessage message = receive();

            // CASE ACKNOWLEDGE RECEIVED
            if (message!=null && message.getContent().equals("RegistrationAck")) {
                //SET ID

                //REMOVE BEHAVIOUR
                removeBehaviour(registration);
            }

            // CASE ACKNOWDLEGE NOT RECEIVED -> SEND REGISTRATION REQUEST
            else {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID("CentralMonitor", AID.ISLOCALNAME));
                msg.setContent("Registration"); // THIS NEEDS TO BE SOME JSON STRING
                send(msg);
            }
        }
    };
}