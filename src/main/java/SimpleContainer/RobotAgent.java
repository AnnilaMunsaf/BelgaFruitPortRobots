package SimpleContainer;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import util.JsonCreator;
import util.TagIdMqtt;
import org.eclipse.paho.client.mqttv3.*;
import util.Point2D;




public class RobotAgent extends Agent {

    String id;
    TagIdMqtt tag;
    TagIdMqtt targetTag;
    long start = System.currentTimeMillis();
    long obstacleTimer;

    // THRESHHOLDS
    int frontThreshold = 30;
    int backwardThreshold = 15;
    int sideThreshold = 15;

    public RobotAgent(String id) {
        this.id = id;
        String targetTagId = "682e";
        try {
            this.tag = new TagIdMqtt(id);
            this.targetTag = new TagIdMqtt(targetTagId);
        }
        catch (MqttException me) {
            System.out.println("Something Went Wrong");
        }
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
                addBehaviour(goToLocation);
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

    TickerBehaviour sensorsFeedback = new TickerBehaviour(this, 1000) {
        
        @Override
        protected void onTick() {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("RobotTwin-" + id, AID.ISLOCALNAME));
            String message = JsonCreator.createSensorsFeedbackMessage(Device.getFrontDistance(), Device.getLeftDistance(), Device.getRightDistance());
            msg.setContent(message);
            send(msg);
        }

    };

    CyclicBehaviour goToLocation = new CyclicBehaviour() {

        @Override
        public void action() {
            try {
                Point2D target_location = new Point2D(targetTag.getSmoothenedLocation(10).x,targetTag.getSmoothenedLocation(10).y);
                int x = tag.getSmoothenedLocation(10).x;
                int y = tag.getSmoothenedLocation(10).y;

                if (x != 0 && y != 0) {
                    // SOME CALCULATIONS
                    int target_x = target_location.x;
                    int target_y = target_location.y;
                    float yaw = (float) Math.toDegrees(tag.getYaw());
                    double dist = tag.getSmoothenedLocation(10).dist(target_location);
                    float target_angle = (float) Math.toDegrees(Math.atan2(target_y - y, target_x - x));
                    float diff_angle = target_angle - yaw;

                    // SOME NORMALIZATION
                    diff_angle = diff_angle % 360;
                    while (diff_angle < 0) {
                        diff_angle += 360.0;
                    }
                    
                    // INFORMATION PRINTED EVERY 5 SECONDS
                    long end = System.currentTimeMillis();
                    long elapsedTime = end - start;

                    if(elapsedTime > 1000){
                        start = System.currentTimeMillis();
                        System.out.println("Distance: "+ dist);
                        System.out.println("DeltaX: " + Math.abs(target_x-x) + "DeltaY: " + Math.abs(target_y - y));
                    }

                    ///// 5 POSSIBLE CASES AFTER CALCULATION

                    // DESTINATION IS REACHED
                    if (dist < 200) { 
                        //target_location = null;
                        //removeBehaviour(goToLocation);
                        Device.stop();
                    }

                    // OBSTACLE DETECTION
                    else if (Device.getFrontDistance() < frontThreshold) {
                        System.out.println("OBSTACLE AVOIDING STARTED");
                        obstacleTimer = System.currentTimeMillis();
                        addBehaviour(obstacleAvoidance);
                        removeBehaviour(goToLocation);
                    }

                    // TOO MUCH LEFT
                    else if (diff_angle > 5 && diff_angle <= 180) {
                        Device.setSpeed(200);
                        Device.turnRight();
                    } 
                    // TOO MUCH RIGHT
                    else if (diff_angle < 355 && diff_angle > 180) {
                        Device.setSpeed(200);
                        Device.turnLeft();
                    }
                    
                    // JUST GO FORWARD
                    else {
                        Device.setSpeed(200);
                        Device.forward();
                    }
                }

            } catch (Exception e) {
                System.out.println(e);
            }
        }
    };


    CyclicBehaviour obstacleAvoidance = new CyclicBehaviour() {
        @Override
        public void action() {
            int frontDistance = Device.getFrontDistance();
            int leftDistance = Device.getLeftDistance();
            int rightDistance = Device.getRightDistance();
            int frontThreshold = 30;
            int backwardThreshold = 15;
            int sideThreshold = 15;

            if (frontDistance < backwardThreshold) {
                obstacleTimer = System.currentTimeMillis();
                Device.backward();
            }
            else if (frontDistance < frontThreshold || rightDistance < sideThreshold || leftDistance < sideThreshold) {
                if (leftDistance > rightDistance) {
                    while (frontDistance < frontThreshold || rightDistance < sideThreshold || leftDistance < sideThreshold) {
                        Device.turnLeft();
                        frontDistance = Device.getFrontDistance();
                        leftDistance = Device.getLeftDistance();
                        rightDistance = Device.getRightDistance();
                    }
                    obstacleTimer = System.currentTimeMillis();
                }
                else {
                    while (frontDistance < frontThreshold || rightDistance < sideThreshold || leftDistance < sideThreshold) {
                        Device.turnRight();
                        frontDistance = Device.getFrontDistance();
                        leftDistance = Device.getLeftDistance();
                        rightDistance = Device.getRightDistance();
                    }
                    obstacleTimer = System.currentTimeMillis();
                }
            }
            else {
                Device.forward();
                if (System.currentTimeMillis() - obstacleTimer < 5000) {
                    Device.stop();
                    removeBehaviour(obstacleAvoidance);
                    addBehaviour(goToLocation);
                }
            }
        }
    };
}