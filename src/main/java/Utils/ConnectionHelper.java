package Utils;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class ConnectionHelper {
    private static String broker = "tcp://localhost:1883";
    private static String clientId = "MAT-Theo";
    private static MqttClient client;

    public void connect() throws MqttException {
        MemoryPersistence persistence = new MemoryPersistence();
        client = new MqttClient(broker, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        client.connect(connOpts);
        client.setCallback(new SimpleCallback());
    }

    public void subscribe(String topic) {
        try {
            client.subscribe(topic);
        } catch (MqttException me) {
            System.out.println("msg " + me.getMessage());
            me.printStackTrace();
        }
    }

    public void publish(String topic, String content) {
        MqttMessage message = new MqttMessage(content.getBytes());
        message.setQos(1);
        try {
            client.publish(topic, message);
        } catch (MqttException me) {
            System.out.println("msg " + me.getMessage());
            me.printStackTrace();
        }
    }
}