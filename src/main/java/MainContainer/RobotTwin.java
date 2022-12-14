package MainContainer;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import util.JsonCreator;
import util.TagIdMqtt;
import util.Point2D;
import org.eclipse.paho.client.mqttv3.*;

enum Status {
    picking_up,
    dropping_off,
    idle
}

enum BEHAVIOR {
    LEFT,
    RIGHT,
    FORWARD
}

public class RobotTwin extends Agent{

    BEHAVIOR lastBehavior = null;

    long start = System.currentTimeMillis();

    Status currentStatus = Status.idle;
    String id;

    String targetId;
    Point2D target_location;

    TagIdMqtt tag;

    TagIdMqtt targetTag;
    int frontDistance;

    @Override
    public void setup() {
        this.id = "6823";
        this.targetId = "682e";
        try {
            this.tag = new TagIdMqtt(id);
            this.targetTag = new TagIdMqtt(targetId);
        }
        catch (MqttException me) {
            System.out.println("something Went Wrong");
        }
        System.out.print("Digital Twin Created\n");

        //addBehaviour(readSensorsFeedback);
        target_location = new Point2D(targetTag.getSmoothenedLocation(10).x,targetTag.getSmoothenedLocation(10).y);
        addBehaviour(goToLocation);
    }

    CyclicBehaviour readSensorsFeedback = new CyclicBehaviour() {
        @Override
        public void action() {
            ACLMessage message = receive();
            if (message!=null && JsonCreator.parseMessageType(message.getContent()).equals("sensorsFeedback")) {
                frontDistance = JsonCreator.parseSensorsFeedbackMessage(message.getContent());
            }
        }
    };

    OneShotBehaviour sendForward = new OneShotBehaviour() {
        @Override
        public void action() {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("RobotAgent-"+id, AID.ISLOCALNAME));
            String message = JsonCreator.createForwardOrder(200);
            msg.setContent(message); // THIS NEEDS TO BE SOME JSON STRING
            send(msg);
        }
    };

    OneShotBehaviour sendLeft = new OneShotBehaviour() {
        @Override
        public void action() {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("RobotAgent-"+id, AID.ISLOCALNAME));
            String message = JsonCreator.createLeftOrder(50);
            msg.setContent(message); // THIS NEEDS TO BE SOME JSON STRING
            send(msg);
        }
    };

    OneShotBehaviour sendRight = new OneShotBehaviour() {
        @Override
        public void action() {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("RobotAgent-"+id, AID.ISLOCALNAME));
            String message = JsonCreator.createRightOrder(50);
            msg.setContent(message); // THIS NEEDS TO BE SOME JSON STRING
            send(msg);
        }
    };

    OneShotBehaviour sendStop = new OneShotBehaviour() {
        @Override
        public void action() {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("RobotAgent-"+id, AID.ISLOCALNAME));
            String message = JsonCreator.createStopOrder();
            msg.setContent(message); // THIS NEEDS TO BE SOME JSON STRING
            send(msg);
        }
    };

    CyclicBehaviour goToLocation = new CyclicBehaviour() {

        @Override
        public void action() {
            try {
                target_location.x = targetTag.getSmoothenedLocation(10).x;
                target_location.y = targetTag.getSmoothenedLocation(10).y;
                int x = tag.getSmoothenedLocation(10).x;
                int y = tag.getSmoothenedLocation(10).y;

                if (x != 0 && y != 0) {
                    int target_x = target_location.x;
                    int target_y = target_location.y;

                    float yaw = (float) Math.toDegrees(tag.getYaw());

                   //double dist = Point2D.distance(x, y, target_x, target_y);
                    double dist = tag.getSmoothenedLocation(10).dist(target_location);
                   // System.out.println("DISTANCE: "+ dist);

                    float target_angle = (float) Math.toDegrees(Math.atan2(y - target_y, target_x - x)); //??
                    float diff_angle = target_angle - yaw;

                    diff_angle = diff_angle % 360;
                    while (diff_angle < 0) { //pretty sure this comparison is valid for doubles and floats
                        diff_angle += 360.0;
                    }

// some time passes
                    long end = System.currentTimeMillis();
                    long elapsedTime = end - start;

                    if(elapsedTime > 1000){
                        System.out.println("diff_angle: "+ diff_angle);
                        System.out.println("yaw: "+ yaw);
                        start = System.currentTimeMillis();
                        System.out.println("target_location.x: "+ target_location.x);
                        System.out.println("target_location.y: "+ target_location.y);
                        System.out.println("lastBehavior: "+ lastBehavior.toString());

                    }

                    // DESTINATION IS REACHED
                    if (Math.abs(target_x - x) < 100 && Math.abs(target_y - y) < 100) {  
                        target_location = null;
                       removeBehaviour(goToLocation);
                    } 
                    // TOO MUCH RIGHT
                    else if (diff_angle > 10 && diff_angle <= 180) {
                        //Device.setSpeed(200);
                        if(lastBehavior != BEHAVIOR.RIGHT){
                            addBehaviour(sendRight);
                        }
                       // System.out.println("RIGHT");
                        lastBehavior = BEHAVIOR.RIGHT;
                    } 
                    // TOO MUCH LEFT
                    else if (diff_angle < 350 && diff_angle > 180) {
                        //Device.setSpeed(200);
                        if(lastBehavior != BEHAVIOR.LEFT){
                            addBehaviour(sendLeft);
                        }
                       // System.out.println("LEFT");
                        lastBehavior = BEHAVIOR.LEFT;
                    }
//                    else if (obstacle) {
//                        addBehaviour(stop);
//                    }

                    // JUST GO FORWARD
                    else {
                        //Device.fuzzy_speed_distance(dist,yaw); // fuzzy_speed
                        if(lastBehavior != BEHAVIOR.FORWARD){
                            addBehaviour(sendForward);
                        }
                       // System.out.println("FORWARD");
                        lastBehavior = BEHAVIOR.FORWARD;
                    }
                  //  block(5000);
                }

            } catch (Exception e) {
                System.out.println(e);
            }
        }
        
    };

}
