package SimpleContainer;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import lejos.utility.Delay;

public class RobotAgent extends Agent{

    // THRESHOLDS
    int pid_threshold = 60;
    int turn_threshold = 30;
    int stop_threshold = 20;
    double side_factor = 2;

    // SPEEDS
    int backward_speed = 240;
    int turn_speed = 200;
    int pid_base_speed = 240;
    int free_speed = 360;

    @Override
    public void setup() {
        addBehaviour(readWalk);
    }

    CyclicBehaviour readWalk = new CyclicBehaviour() {
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
    };


    public void respondWalk() {
        Delay.msDelay(1500);

        System.out.println("FROM ROBOT TO COMPUTER");

        ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
        msg1.addReceiver(new AID("ComputerAgent", AID.ISLOCALNAME));
        msg1.setContent("ack");
        addBehaviour(free_walk);
        removeBehaviour(readWalk);
        send(msg1);

    }

    CyclicBehaviour free_walk = new CyclicBehaviour() {
        @Override
        public void action() {
            try {
                // GET DISTANCES
                int front_distance = Device.getFrontDistance();
                int left_distance = Device.getLeftDistance();
                int right_distance = Device.getRightDistance();
                
                if (front_distance < stop_threshold) {
                    // GO BACKWARD
                    System.out.println("BACKWARD");
                    Device.setSpeed(backward_speed);
                    Device.backward();
                }
                else if (front_distance < turn_threshold || right_distance < turn_threshold/side_factor || left_distance < turn_threshold/side_factor) {
                    // TURN
                    System.out.println("TURNING");
                    Device.setSpeed(turn_speed);
                    if (left_distance > right_distance) {
                        while (front_distance < turn_threshold || left_distance < turn_threshold/side_factor || right_distance < turn_threshold/side_factor) {
                            Device.turnLeft();
                            front_distance = Device.getFrontDistance();
                            left_distance = Device.getLeftDistance();
                            right_distance = Device.getRightDistance();
                        }
                    }
                    else {
                        while (front_distance < turn_threshold || left_distance < turn_threshold/side_factor|| right_distance < turn_threshold/side_factor) {
                            Device.turnRight();
                            front_distance = Device.getFrontDistance();
                            left_distance = Device.getLeftDistance();
                            right_distance = Device.getRightDistance();
                        }
                    }
                }
                else if (front_distance < pid_threshold) {
                    // PID WALK
                    System.out.println("PID WALKING");
                    int pid_speed = (int) PID(front_distance-turn_threshold);
                    int next_speed = pid_base_speed + pid_speed;
                    Device.setSpeed(next_speed);
                    Device.forward();
                }
                else {
                    // FREE WALK
                    System.out.println("FREE WALKING");
                    Device.setSpeed(free_speed);
                    Device.forward();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public double PID(int error) {
        double Kp = 4;
        // double Ki = 0;
        // double Kd = 0;

        return Kp*error;
    }
}