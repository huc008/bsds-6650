package rabbitMQ;


import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.LiftRide;

public class RabbitMQTest {
  private static Connection connection;
  private Channel channel;
  private static final String QUEUE_NAME = "skier_queue";
  private static final int NUM_CHANS = 30;  // Number of channels to add to pools
  private static final int NUM_THREADS = 200; // Number of threads to publish payload
  private static final int NUM_ITERATIONS = 100; //Number of messages to publish in each thread

  public RabbitMQTest() throws IOException, TimeoutException {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    factory.setUsername("guest");
    factory.setPassword("guest");

    connection = factory.newConnection();
    System.out.println("INFO: RabbitMQ connection established");
//    channel = connection.createChannel();
  }

  public void QueuePoolPublisher() throws InterruptedException {
    // The channel facory generates new channels on demand, as needed by the channel pool
    RMQChannelFactory channelFactory = new RMQChannelFactory (connection);
    // create the fixed size channel pool
    RMQChannelPool pool = new RMQChannelPool(NUM_CHANS, channelFactory);

    // latch is used for the main thread to block until all test treads complete
    final CountDownLatch latch = new CountDownLatch(NUM_THREADS);

    // create N threads, each of which uses the channel pool to publish messages
    // TO DO refactor loop into a method for use by both tests
    for (int i=0; i < NUM_THREADS; i++) {

      Runnable testThread = () -> {
        for (int j = 0; j < NUM_ITERATIONS; j++) {
          try {
            Channel channel;
            // get a channel from the pool
            channel = pool.borrowObject();

            // publish message
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            LiftRide liftRide = new LiftRide(20,30);
            Gson gson = new Gson();
            String jsonResponse = gson.toJson(liftRide);
            byte[] payLoad = jsonResponse.getBytes("utf-8"); //todo: substitute payload as skier json
            channel.basicPublish("", QUEUE_NAME, null, payLoad);

            // return channel to the pool
            pool.returnObject(channel);

          } catch (IOException ex) {
            Logger.getLogger(RabbitMQTest.class.getName()).log(Level.INFO, null, ex);
          } catch (Exception ex) {
            Logger.getLogger(RabbitMQTest.class.getName()).log(Level.INFO, null, ex);
          }
        }
        // I've finished - inform parent thread
        latch.countDown();

      };
      new Thread(testThread).start();
    }
    // Block until all threads complete
    latch.await();
    System.out.println("INFO: Queue based pool test finished");
  }

  public static void main(String[] args) throws IOException, TimeoutException {
    RabbitMQTest test = new RabbitMQTest();
    try {
      test.QueuePoolPublisher();
    } catch (InterruptedException ex) {
      Logger.getLogger(RabbitMQTest.class.getName()).log(Level.SEVERE, null, ex);
    }
    System.out.println("INFO: QueuePoolPublisher Test Complete");
    test.connection.close();
  }
}
