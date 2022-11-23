package pozyx;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class main {

    public static void main(String[] args) {

        String broker       = "wss://mqtt.cloud.pozyxlabs.com:443";
        String topic        = "61d730870295a7f3798fdb31";
        String apiKey		= "1d761f94-6fe7-4549-aaa5-73a4ffecc2ee";
        String clientId		= "61d730870295a7f3798fdb31";

        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient mqttClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(true);
            mqttConnectOptions.setUserName(topic);
            mqttConnectOptions.setPassword(apiKey.toCharArray());

            System.out.println("Connecting to broker: "+broker);
            mqttClient.connect(mqttConnectOptions);
            System.out.println("Connected");

            mqttClient.setCallback(new MqttCallback() {
                public void connectionLost(Throwable cause) {
                    System.out.println("Connection lost");
                    System.out.println(cause);
                }
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                }
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("Delivery complete");
                }
            });

            System.out.println("Subscribing to topic: "+topic);
            mqttClient.subscribe(topic);
            System.out.println("Subscribed to topic: "+topic);
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }

}