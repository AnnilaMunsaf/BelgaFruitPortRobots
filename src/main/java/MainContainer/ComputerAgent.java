package MainContainer;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import lejos.utility.Delay;


public class ComputerAgent extends Agent {
    @Override
    public void setup() {
        addBehaviour(sendWalk);
    }


    CyclicBehaviour sendWalk = new CyclicBehaviour() {
        @Override
        public void action() {

            ACLMessage message=receive();
            if (message!=null) {
                if (message.getContent().equals("ack")) {
                    removeBehaviour(sendWalk);
                }
            }
            else {
                System.out.println("FROM COMPUTER TO ROBOT");

                Delay.msDelay(1500);
                ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
                msg1.addReceiver(new AID("RobotAgent", AID.ISLOCALNAME));
                msg1.setContent("walk");
                send(msg1);
            }
        }
    };
}
