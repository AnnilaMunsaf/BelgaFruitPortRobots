package SimpleContainer;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import util.JsonCreator;

public class RobotAgent extends Agent {

    String ID;

    @Override
    public void setup() {
        addBehaviour(registration);
    }

    OneShotBehaviour registration = new OneShotBehaviour() {
        @Override
        public void action() {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("CentralMonitor", AID.ISLOCALNAME));
            String request = JsonCreator.createRegistrationRequest(ID);
            msg.setContent(request);
            send(msg);
        }
    };
}