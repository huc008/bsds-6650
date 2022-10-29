package rabbitMQ;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import model.Skier;

public class Publisher {
  private ConnectionFactory factory;
  private Connection connection;
  private RMQChannelFactory channelFactory;
  private RMQChannelPool pool;
  private final String SERVER = "54.185.45.231";
//  private final String SERVER = "localhost";
  private final String QUEUE_NAME = "skier_queue";
  private final int NUM_CHANS = 30;  // Number of channels to add to pools


  public Publisher() throws IOException, TimeoutException {
    this.factory = new ConnectionFactory();
    this.factory.setHost(SERVER);
    this.factory.setUsername("guest");
    this.factory.setPassword("guest");

    this.connection = this.factory.newConnection();
    System.out.println("INFO: RabbitMQ connection established: " + SERVER);
    this.channelFactory = new RMQChannelFactory(connection);
    this.pool = new RMQChannelPool(NUM_CHANS, channelFactory);
  }

  public void sendRMQ (Skier skier) {
    try {
      Channel channel = pool.borrowObject(); //get a channel from the pool;
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);
      Gson gson = new Gson();
      byte[] payLoad = gson.toJson(skier).getBytes("utf-8");
      channel.basicPublish("", QUEUE_NAME, null, payLoad);
      pool.returnObject(channel);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
