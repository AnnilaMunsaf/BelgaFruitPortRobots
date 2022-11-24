package MainContainer;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import lejos.utility.Delay;
import MainContainer.util.Point2D;


enum Status {
    picking_up,
    dropping_off,
    idle
}

public class RobotTwin extends Agent{
    Status currentStatus = null;
    Point2D status = null;
    
    @Override
    public void setup() {
        
    }


    
}
