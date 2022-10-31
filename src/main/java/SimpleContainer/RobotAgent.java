package SimpleContainer;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import lejos.utility.Delay;

public class RobotAgent extends Agent{

    int distance_threshold = 30;

    @Override
    public void setup() {
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {

                ACLMessage message=receive();
                if (message!=null) {
                    if (message.getContent().equals("walk")) {
                        System.out.println(" from ComputerAgent: "+message.getContent());
                        respondWalk();
                    }
                }
            }
        });
    }
    public void respondWalk() {
        Delay.msDelay(1500);

        System.out.println("FROM ROBOT TO COMPUTER");

        ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
        msg1.addReceiver(new AID("ComputerAgent", AID.ISLOCALNAME));
        msg1.setContent("ack");
        addBehaviour(go_forward);
        send(msg1);

    }

    CyclicBehaviour go_forward = new CyclicBehaviour() {
        @Override
        public void action() {
            try {
                int distance = Device.getFrontDistance();
                int nextSpeed = (int) PID(distance-distance_threshold);
                System.out.println(distance-distance_threshold);
                if (nextSpeed < 0) {
                    nextSpeed = 0;
                }
                Device.setSpeed(nextSpeed);
                Device.forward();
                System.out.println("Forward");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public double PID(int error) {
        double Kp = 2;
        // double Ki = 0;
        // double Kd = 0;

        return Kp*error;
    }
}