package rabbitMQ;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class Publisher {
  private ConnectionFactory factory;
  private Connection connection;
  private RMQChannelFactory channelFactory;
  private RMQChannelPool pool;
//  private ObjectPool<Channel> pool;
  private final String SERVER = "localhost";
  private final String QUEUE_NAME = "skier_queue";
  private final int NUM_CHANS = 30;  // Number of channels to add to pools


  public Publisher() throws IOException, TimeoutException {
    this.factory = new ConnectionFactory();
    this.factory.setHost(SERVER);
    this.factory.setUsername("guest");
    this.factory.setPassword("guest");
    this.factory.setVirtualHost("/");
    this.factory.setPort(5672);
    this.connection = this.factory.newConnection();
    System.out.println("INFO: RabbitMQ connection established");
    this.channelFactory = new RMQChannelFactory(connection);
    System.out.println("line between channelFactory and pool");
    this.pool = new RMQChannelPool(NUM_CHANS, channelFactory);
//    this.pool = new GenericObjectPool<>(channelFactory);
    System.out.println("Constructor completed");
  }

  public void sendRMQ (String jsonResponse) {
    Channel channel = null;
    try {
      channel = pool.borrowObject(); //get a channel from the pool;
      //publish liftRide as the payload
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);
      byte[] payLoad = jsonResponse.getBytes("utf-8");
      channel.basicPublish("", QUEUE_NAME, null, payLoad);
      pool.returnObject(channel);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}
