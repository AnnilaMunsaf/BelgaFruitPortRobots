package SimpleContainer;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
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
        Device.init();
        addBehaviour(registration);
    }

    TickerBehaviour registration = new TickerBehaviour(this, 5000) {
        @Override
        public void onTick() {
            ACLMessage message=receive();
            if (message!=null && JsonCreator.parseMessageType(message.getContent()).equals("registrationAck")) {
                removeBehaviour(registration);
                addBehaviour(sensorsFeedback);
            }
            else {
                System.out.print("Sending a registration request\n");
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID("CentralMonitor", AID.ISLOCALNAME));
                String request = JsonCreator.createRegistrationRequest(id);
                msg.setContent(request);
                send(msg);
            }
        }
    };

    CyclicBehaviour sensorsFeedback = new CyclicBehaviour() {
        @Override
        public void action() {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("RobotTwin-" + id, AID.ISLOCALNAME));
            String message = JsonCreator.createSensorsFeedbackMessage(Device.getFrontDistance());
            msg.setContent(message);
            send(msg);
        }
    };
}