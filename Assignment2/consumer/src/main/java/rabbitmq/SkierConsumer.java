package rabbitmq;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class SkierConsumer {
  private final static String QUEUE_NAME = "skier_queue";
//  private final static String SERVER = "54.185.45.231";
private final static String SERVER = "localhost";
  private final static Integer NUM_THREAD = 32;
  private static Gson gson = new Gson();
  private static ConcurrentHashMap<Integer, List<JsonObject>> hashMap = new ConcurrentHashMap<>();

  public static void main(String[] args) throws IOException, TimeoutException {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(SERVER);
    Connection connection = factory.newConnection();
    System.out.println("INFO: connected to SERVER: " + SERVER);
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          Channel channel = connection.createChannel();
          channel.queueDeclare(QUEUE_NAME, false, false, false, null);
          channel.basicQos(1);          // accept only 1 unacknowledged message
          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
            addToMap(message);
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//            System.out.println("Hashmap: " + hashMap);
          };
          channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    };

    for (int i  = 0; i < NUM_THREAD; i++) {
      Thread thread = new Thread(runnable);
      thread.start();
    }


  }

  private static void addToMap(String msg) {
    JsonObject jsonObject = gson.fromJson(msg, JsonObject.class);
    int key = Integer.valueOf(String.valueOf(jsonObject.get("skierId")));
    JsonObject value = (JsonObject) jsonObject.get("liftRide");
    if (hashMap.contains(key)) {
      hashMap.get(key).add(jsonObject);
    } else {
      List<JsonObject> valueList = new ArrayList<>();
      valueList.add(value);
      hashMap.put(key, valueList);
    }
  }

}
