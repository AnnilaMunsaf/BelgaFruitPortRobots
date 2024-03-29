package SimpleContainer;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

public class SimpleContainer {
    public static void main(String[] args) {

        try {
            // SET THIS TWO FIELDS BEFORE DEPLOYING TO THE ROBOT
            String robot_ip = "192.168.0.171";
            String tag_id = "6a75";

            //String robot_ip = "192.168.0.111";
            //String tag_id = "6841";

            Runtime runtime = Runtime.instance();
            String target ="192.168.0.120";
            String source = robot_ip;
            ProfileImpl p = new ProfileImpl(target,1099,null,false);

            p.setParameter(Profile.LOCAL_HOST,source);
            p.setParameter(Profile.LOCAL_PORT,"1099");

            AgentContainer agentContainer=runtime.createAgentContainer(p);
            SimpleContainer.start();
            
            RobotAgent newRobotAgent = new RobotAgent(tag_id);
            AgentController robotAgent = agentContainer.acceptNewAgent("RobotAgent-" + tag_id, newRobotAgent);

            robotAgent.start();

        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    private static void start() {

    }
}
