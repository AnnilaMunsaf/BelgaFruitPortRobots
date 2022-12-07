package SimpleContainer;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import util.JsonCreator;

enum Order {
    forward,
    backward,
    turn_left,
    turn_right,
    stop
}

public class RobotAgent extends Agent {
     ParallelBehaviour test = new ParallelBehaviour();

    String id;
    Order current_order; 

    public RobotAgent(String id){
        this.id = id;
        this.current_order = Order.stop;
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
                addBehaviour(readOrders);
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


    CyclicBehaviour readOrders = new CyclicBehaviour() {
        @Override
        public void action() {
            ACLMessage message=receive();
            if (message!=null && JsonCreator.parseMessageType(message.getContent()).equals("order")) {
                // FORWARD ORDER
                if (JsonCreator.parseOrderType(message.getContent()).equals("forward")) {
                    current_order = Order.forward;
                    int speed = JsonCreator.parseOrderSpeed(message.getContent());
                    Device.setSpeed(speed);
                }
                // BACKWARD ORDER
                else if (JsonCreator.parseOrderType(message.getContent()).equals("backward")) {
                    current_order = Order.backward;
                    int speed = JsonCreator.parseOrderSpeed(message.getContent());
                    Device.setSpeed(speed);
                }
                // TURN LEFT ORDER
                else if (JsonCreator.parseOrderType(message.getContent()).equals("left")) {
                    current_order = Order.turn_left;
                    int speed = JsonCreator.parseOrderSpeed(message.getContent());
                    Device.setSpeed(speed);
                }
                // TURN RIGHT ORDER
                else if (JsonCreator.parseOrderType(message.getContent()).equals("right")) {
                    current_order = Order.turn_right;
                    int speed = JsonCreator.parseOrderSpeed(message.getContent());
                    Device.setSpeed(speed);
                }
                // STOP ORDER
                else if (JsonCreator.parseOrderType(message.getContent()).equals("stop")) {
                    current_order = Order.stop;
                    Device.setSpeed(0);
                }
                addBehaviour(executeCurrentOrder);
            }
        }
    };

    OneShotBehaviour executeCurrentOrder = new OneShotBehaviour() {
        @Override
        public void action() {
            switch (current_order) {
                case forward:
                    Device.forward();
                    break;
                case stop:
                    Device.stop();
                    break;
                case backward:
                    Device.backward();
                    break;
                case turn_left:
                    Device.turnLeft();
                    break;
                case turn_right:
                    Device.turnRight();
                    break;
            }
        }
    };
}