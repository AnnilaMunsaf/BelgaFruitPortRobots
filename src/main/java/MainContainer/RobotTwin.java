package MainContainer;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import lejos.utility.Delay;
import util.TagIdMqtt;
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

    public RobotTwin(String id){
        this.id = id;
        try {
            this.tag = new TagIdMqtt(id);
        }
        catch (MqttException me) {

        }
    }

    @Override
    public void setup() {
        
    }
    
}
