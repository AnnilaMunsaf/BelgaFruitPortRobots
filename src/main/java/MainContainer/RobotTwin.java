package MainContainer;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import lejos.utility.Delay;
import util.Point2D;


enum Status {
    picking_up,
    dropping_off,
    idle
}

public class RobotTwin extends Agent{
    int ID;
    Status currentStatus = Status.idle;
    Point2D location = null;
    
    @Override
    public void setup() {
        
    }
    
}
