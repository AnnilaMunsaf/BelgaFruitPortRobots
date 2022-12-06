package MainContainer;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
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
    
}
