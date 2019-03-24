package Utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import models.CarCoordinates;
import models.CarStatus;
import models.Location;
import models.Type;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SimpleCallback implements MqttCallback {
    private static final double MILLISTOHOURS = 3600000.0;
    private static final double KMHTOMPH = 0.62137;
    private static Map<Integer, Location> startLocations = new HashMap<Integer, Location>();
    private static Map<Integer, Double> startTimes = new HashMap<Integer, Double>();
    private static String TOPIC_PUB_EVENTS = "events";
    private static String TOPIC_PUB_CAR_STATUS = "carStatus";
    private static DistanceFinder distanceFinder;
    private static int refreshIndex;

    static {
        distanceFinder = new DistanceFinder();
        startLocations.put(0, Location.builder().latitude(0).longitude(0).build());
        startTimes.put(0, (double) System.currentTimeMillis());
        startLocations.put(1, Location.builder().latitude(0).longitude(0).build());
        startTimes.put(1, (double) System.currentTimeMillis());
        startLocations.put(2, Location.builder().latitude(0).longitude(0).build());
        startTimes.put(2, (double) System.currentTimeMillis());
        startLocations.put(3, Location.builder().latitude(0).longitude(0).build());
        startTimes.put(3, (double) System.currentTimeMillis());
        startLocations.put(4, Location.builder().latitude(0).longitude(0).build());
        startTimes.put(4, (double) System.currentTimeMillis());
        startLocations.put(5, Location.builder().latitude(0).longitude(0).build());
        startTimes.put(5, (double) System.currentTimeMillis());
        refreshIndex = 0;
    }

    public void connectionLost(Throwable cause) {
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    public void messageArrived(String topic, MqttMessage message) {
        double speed;
        ConnectionHelper conn = new ConnectionHelper();
        Gson gson = new Gson();
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(new String(message.getPayload()));

        CarCoordinates carCoordinates = CarCoordinates.builder()
                .timestamp(jsonElement.getAsJsonObject().get("timestamp").getAsLong())
                .carIndex(jsonElement.getAsJsonObject().get("carIndex").getAsInt())
                .location(Location.builder()
                        .longitude(jsonElement.getAsJsonObject().get("location").getAsJsonObject().get("long").getAsFloat())
                        .latitude(jsonElement.getAsJsonObject().get("location").getAsJsonObject().get("lat").getAsFloat())
                        .build())
                .build();

            speed = getSpeed(carCoordinates);

            //Create carStatus
            CarStatus carStatus = CarStatus.builder()
                    .carIndex(carCoordinates.getCarIndex())
                    .type(Type.SPEED)
                    .value(speed * KMHTOMPH) //kmh to mph
                    .timestamp(carCoordinates.getTimestamp())
                    .build();

            //reinitialize variables
            startTimes.put(carCoordinates.getCarIndex(), (double) System.currentTimeMillis());
            startLocations.put(carCoordinates.getCarIndex(), carCoordinates.getLocation());

            conn.publish(TOPIC_PUB_CAR_STATUS, gson.toJson(carStatus));
    }

    private double getSpeed(CarCoordinates carCoordinates) {
        double distance, time;
        //distance in kms
        distance = getAbsDistance(carCoordinates);

        //time in hours
        time = (carCoordinates.getTimestamp() - startTimes.get(carCoordinates.getCarIndex())) / MILLISTOHOURS;

        return distance / time;
    }

    private double getAbsDistance(CarCoordinates carCoordinates) {
        return Math.abs(distanceFinder
                .distanceBetweenCoordinates(
                        carCoordinates.getLocation().getLatitude(),
                        carCoordinates.getLocation().getLongitude(),
                        startLocations.get(carCoordinates.getCarIndex()).getLatitude(),
                        startLocations.get(carCoordinates.getCarIndex()).getLongitude()
                ));
    }
}

