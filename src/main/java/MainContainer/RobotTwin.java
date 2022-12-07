package MainContainer;
import SimpleContainer.Device;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lejos.utility.Delay;
import util.JsonCreator;
import util.TagIdMqtt;
import util.Point2D;
import org.eclipse.paho.client.mqttv3.*;

enum Status {
    picking_up,
    dropping_off,
    idle
}

public class RobotTwin extends Agent{
 
    Status currentStatus = Status.idle;
    String id;
    Point2D target_location;

    TagIdMqtt tag;
    int frontDistance;

    @Override
    public void setup() {
        this.id = "6823";
        try {
            this.tag = new TagIdMqtt(id);
        }
        catch (MqttException me) {
            System.out.println("something Went Wrong");
        }
        System.out.print("Digital Twin Created\n");

        addBehaviour(readSensorsFeedback);
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
    
    CyclicBehaviour monitorLocation = new CyclicBehaviour() {
        @Override
        public void action() {
            Point2D location = tag.getLocation();

            // IF LOCATION AROUND THE DESIRED LOCATION ADD SENDSTOP BEHAVIOUR


        }
    };

    CyclicBehaviour goToLocation = new CyclicBehaviour() {

        @Override
        public void action() {
            try {
                int x = tag.getSmoothenedLocation(10).x;
                int y = tag.getSmoothenedLocation(10).y;

                if (x != 0 && y != 0) {
                    int target_x = target_location.x;
                    int target_y = target_location.y;

                    System.out.println("x: " + x);
                    System.out.println("y: " + y);

                    float yaw = (float) Math.toDegrees(tag.getYaw());
                   // yaw = (float) (yaw-301.5);
                    System.out.println("yaw mission="+yaw);

                    double diff_y = target_y - y;
                    double diff_x = target_x - x;

                    System.out.println("diff_y="+diff_y);
                    System.out.println("diff_x="+diff_x);


                   double dist = Point2D.distance(x, y, target_x, target_y);

                                       //double atan2 =Math.atan2(diff_y, diff_x);

                    //System.out.println("atan2="+atan2);

                    //float target_angle = (float) Math.toDegrees(atan2); //??


                    float target_angle = (float) Math.toDegrees(Math.atan2(y - target_y, target_x - x)); //??


                    System.out.println("Target Angle"+target_angle);

                    float diff_angle = target_angle - yaw;
                    System.out.println("Diff Angle"+diff_angle);



                     diff_angle = diff_angle % 360;
                     while (diff_angle < 0) { //pretty sure this comparison is valid for doubles and floats
                     diff_angle += 360.0;
                     }

                    System.out.println("- AFTER Diff Angle**"+diff_angle);


                    if (Math.abs(target_x - x) < 100 && Math.abs(target_y - y) < 100) {    // old params diff_angle > 10 && diff_angle <= 180, diff_angle < 350 && diff_angle > 180
                        path_iterator += 2;
                    } else if (diff_angle > 10 && diff_angle <= 180) {
                        Device.setSpeed(200);
                        addBehaviour(turn_right);
                        System.out.println("RIGHT");
                    } else if (diff_angle < 350 && diff_angle > 180) {
                        Device.setSpeed(200);
                        addBehaviour(turn_left);
                        System.out.println("LEFT");
                    }
//                    else if (obstacle) {
//                        addBehaviour(stop);
//                    }
                    else {
                        Device.fuzzy_speed_distance(dist,yaw); // fuzzy_speed
                       addBehaviour(go_forward);
                        System.out.println("FORWARD");
                    }



                  //  block(5000);
                }

            } catch (Exception e) {
                System.out.println(e);
            }
        }
        
    };

}
