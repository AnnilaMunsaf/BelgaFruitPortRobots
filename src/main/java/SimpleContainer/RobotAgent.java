package SimpleContainer;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import util.JsonCreator;
import util.TagIdMqtt;
import org.eclipse.paho.client.mqttv3.*;
import util.Point2D;



public class RobotAgent extends Agent {
    // IDS AND TAGS
    String id;
    TagIdMqtt tag;

    // TARGETS
    Point2D targetLocation = null;

    // TIMERS
    long start = System.currentTimeMillis();
    long obstacleTimer;

    // THRESHHOLDS
    int frontThreshold = 30;
    int backwardThreshold = 15;
    int sideThreshold = 15;

    // BATTERY
    int battery = 60 * 5;   // 5 minutes

    public RobotAgent(String id) {
        this.id = id;
        try {
            this.tag = new TagIdMqtt(id);
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
                addBehaviour(messageHandler);
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

    CyclicBehaviour messageHandler = new CyclicBehaviour() {
        @Override
        public void action() {
            ACLMessage message=receive();
            if (message!=null && JsonCreator.parseMessageType(message.getContent()).equals("targetLocationUpdate")) {
                targetLocation = JsonCreator.parseTargetLocationUpdate(message.getContent());
                addBehaviour(goToLocation);
            }
            else if (message != null && JsonCreator.parseMessageType(message.getContent()).equals("stop")) {
                removeBehaviour(goToLocation);
                removeBehaviour(obstacleAvoidance);
            }
            else if (message != null && JsonCreator.parseMessageType(message.getContent()).equals("resume")) {
                if (targetLocation != null) {
                    addBehaviour(goToLocation);
                }
            }
        }
    };

    
    // FIXED BEHAVIOURS
    CyclicBehaviour goToLocation = new CyclicBehaviour() {

        @Override
        public void action() {
            if (targetLocation != null) {
                try {
                    
                    int x = tag.getSmoothenedLocation(10).x;
                    int y = tag.getSmoothenedLocation(10).y;

                    int frontDistance = Device.getFrontDistance();
                    int leftDistance = Device.getLeftDistance();
                    int rightDistance = Device.getRightDistance();

                    if (x != 0 && y != 0) {
                        // SOME CALCULATIONS
                        int target_x = targetLocation.x;
                        int target_y = targetLocation.y;

                        float yaw = (float) Math.toDegrees(tag.getYaw());
                        double dist = tag.getSmoothenedLocation(10).dist(targetLocation);
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
                            System.out.println("DeltaX: " + Math.abs(target_x - x) + "DeltaY: " + Math.abs(target_y - y));
                        }

                        // FIX ORIENTATION
                        Device.setSpeed(100);
                        while (!(diff_angle >= 358 || diff_angle <= 2)) {
                            if (diff_angle < 358 && diff_angle > 180) {
                                Device.turnLeft();
                            }
                            else {
                                Device.turnRight();
                            }
                            yaw = (float) Math.toDegrees(tag.getYaw());
                            target_angle = (float) Math.toDegrees(Math.atan2(target_y - y, target_x - x));
                            diff_angle = target_angle - yaw;
                            diff_angle = diff_angle % 360;
                            while (diff_angle < 0) {
                                diff_angle += 360.0;
                            }
                            frontDistance = Device.getFrontDistance();
                            leftDistance = Device.getLeftDistance();
                            rightDistance = Device.getRightDistance();
                            if (frontDistance < frontThreshold || rightDistance < sideThreshold || leftDistance < sideThreshold) {
                                System.out.println("OBSTACLE AVOIDING STARTED");
                                obstacleTimer = System.currentTimeMillis();
                                addBehaviour(obstacleAvoidance);
                                removeBehaviour(goToLocation);
                            }
                        }

                        ///// 5 POSSIBLE CASES AFTER CALCULATION

                        // DESTINATION IS REACHED
                        if (dist < 200) { 
                            Device.stop();
                            targetLocation = null;
                            sendMessage("RobotTwin-" + id, JsonCreator.createLocationReachedMessage());
                            removeBehaviour(goToLocation);
                        }

                        // OBSTACLE DETECTION
                        else if (frontDistance < frontThreshold || rightDistance < sideThreshold || leftDistance < sideThreshold) {
                            System.out.println("OBSTACLE AVOIDING STARTED");
                            obstacleTimer = System.currentTimeMillis();
                            addBehaviour(obstacleAvoidance);
                            removeBehaviour(goToLocation);
                        }
                        
                        // JUST GO FORWARD
                        else {
                            Device.setSpeed(500);
                            Device.forward();
                        }
                    }

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    };

    CyclicBehaviour obstacleAvoidance = new CyclicBehaviour() {
        @Override
        public void action() {
            int frontDistance = Device.getFrontDistance();
            int leftDistance = Device.getLeftDistance();
            int rightDistance = Device.getRightDistance();

            if (frontDistance < backwardThreshold) {
                obstacleTimer = System.currentTimeMillis();
                Device.setSpeed(200);
                Device.backward();
            }
            else if (frontDistance < frontThreshold || rightDistance < sideThreshold || leftDistance < sideThreshold) {
                if (leftDistance > rightDistance) {
                    while (frontDistance < frontThreshold || rightDistance < sideThreshold || leftDistance < sideThreshold) {
                        Device.setSpeed(200);   
                        Device.turnLeft();
                        frontDistance = Device.getFrontDistance();
                        leftDistance = Device.getLeftDistance();
                        rightDistance = Device.getRightDistance();
                    }
                    obstacleTimer = System.currentTimeMillis();
                }
                else {
                    while (frontDistance < frontThreshold || rightDistance < sideThreshold || leftDistance < sideThreshold) {
                        Device.setSpeed(200); 
                        Device.turnRight();
                        frontDistance = Device.getFrontDistance();
                        leftDistance = Device.getLeftDistance();
                        rightDistance = Device.getRightDistance();
                    }
                    obstacleTimer = System.currentTimeMillis();
                }
            }
            else {
                Device.setSpeed(300);
                Device.forward();
                if (System.currentTimeMillis() - obstacleTimer > 4000) {
                    Device.stop();
                    removeBehaviour(obstacleAvoidance);
                    addBehaviour(goToLocation);
                    System.out.println("OBSTACLE AVOIDING FINISHED");
                }
            }
        }
    };

    void sendMessage(String receiver, String message) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        msg.setContent(message);
        send(msg);
    }
}