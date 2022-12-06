package SimpleContainer;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import util.JsonCreator;

public class RobotAgent extends Agent {

    String id;

    public RobotAgent(String id){
        this.id = id;   
    }

    @Override
    public void setup() {
        addBehaviour(registration);
    }

    TickerBehaviour registration = new TickerBehaviour(this, 5000) {
        @Override
        public void onTick() {
            ACLMessage message=receive();
            if (message!=null && message.getContent().equals("RegistrationAck")) {
                removeBehaviour(registration);
            }
            else {
                System.out.print("Sending a registration request\n");
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID("CentralMonitor", AID.ISLOCALNAME));
                String request = JsonCreator.createRegistrationRequest("26670");
                msg.setContent(request);
                send(msg);
            }
        }
    };
}