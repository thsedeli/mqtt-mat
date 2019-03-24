import Utils.ConnectionHelper;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MAT {
    private static String TOPIC_SUB = "carCoordinates";

    public static void main(String[] args) throws MqttException {
        ConnectionHelper conn = new ConnectionHelper();
        conn.connect();
        conn.subscribe(TOPIC_SUB);
    }

}
