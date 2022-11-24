
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


public class ResortService {

  public final static String VH = "/";
  public final static int PORT = 5672;
  public final static String QUEUE_NAME = "SKIER_TASK";
  public final static String USERNAME = "user";
  public final static String PASSWORD = "password";
  public final static int MAX_THREADS = 256;
  public final static String SEP = "__";
  public static final String UNQ_SKIER = "unqSkier";
  public static final String RESORT = "RESORT";
  public static final String RIDES_TOTAl = "rides";
  private static final int DB_RESORT = 1;


  public static void main(String[] argv) throws Exception {
     Gson gson = new Gson();
    String RMQ_HOST = argv[0].trim();
    String REDIS_HOST = argv[1].trim();
    JedisPoolUtil.setHOST(REDIS_HOST);
//    ConcurrentHashMap<Integer, CopyOnWriteArrayList<String>> map = new ConcurrentHashMap<>();
//    Logger logger = LoggerFactory.getLogger(ResortService.class);
    //
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(RMQ_HOST);
    factory.setVirtualHost(VH);
    factory.setPort(PORT);
    factory.setUsername(USERNAME);
    factory.setPassword(PASSWORD);
//    final Connection connection = factory.newConnection();
    Connection connection = factory.newConnection();
    // /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
    //write a new lift ride for the skier


//    final JedisPoolConfig poolConfig = buildPoolConfig();
//    Pool jedisPool = new JedisPool(poolConfig, "localhost");

    JedisPool jedisPool = JedisPoolUtil.getJedisPoolInstance();
    Jedis jedis = jedisPool.getResource();
    jedis.select(DB_RESORT);

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          Jedis jedis = jedisPool.getResource();
          jedis.select(1);
          Channel channel = connection.createChannel();
          channel.queueDeclare(QUEUE_NAME, false, false, false, null);
          // max one message per receiver
          channel.basicQos(1);
          System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");
//          logger.info(String.format(" [*] Thread waiting for messages. To exit press CTRL+C"));

          //flushdb :
          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            LiftRide liftRide = gson.fromJson(message, LiftRide.class);
            System.out.println(" [x] Received '" + liftRide + "'");
//            logger.info(String.format("Received (channel %d) %s",channel.getChannelNumber(),new String(delivery.getBody())));
            String skierId = String.valueOf(liftRide.getSkierId());
            String resortId = String.valueOf(liftRide.getResortId());
            String dayId = String.valueOf(liftRide.getDayId());
            String seasonId = String.valueOf(liftRide.getSeasonId());
            String liftId = String.valueOf(liftRide.getLiftId());
//            String json = gson.toJson(liftRide,LiftRide.class);
//            jedis.hset(RESORT,skierId,json);
            String v = String.valueOf(1);
            String k = "k";
            String k2 = "k2";


              try {
                //GET/resorts/{resortID}/seasons/{seasonID}/day/{dayID}/skiers
                //“How many unique skiers visited resort X on day N?”   [unique skier/resortX, dayN]
                v = String.valueOf(1);
                k = RESORT+dayId+SEP+resortId+SEP+UNQ_SKIER;
                k2 = RESORT+skierId+SEP+dayId+SEP+resortId+UNQ_SKIER;;

                if(!jedis.exists(k)) {
                  jedis.set(k2, v);
                  jedis.set(k,v);
                }
                else if(!jedis.exists(k2)){
                  jedis.incr(k);
                }
              } catch (Exception e) {
                System.err.println("expire key: "+". Error: "+e);
                jedisPool.returnBrokenResource(jedis);
              }

            try {
//              “How many rides on lift N happened on day N?” [ #liftrides on N/day N]
              k = RESORT+liftId+SEP+dayId+SEP+RIDES_TOTAl;
              if(!jedis.exists(k)) {
                jedis.set(k, v);
              }
              else{
                jedis.incr(k);
              }

              //“On day N, show me how many lift rides took place in each hour of the ski day”
              k = RESORT+liftId+SEP+dayId+SEP+RIDES_TOTAl;
              if(!jedis.exists(k)) {
                jedis.set(k, v);
              }
              else{
                jedis.incr(k);
              }

            } catch (Exception e) {
              System.err.println("expire key: "+". Error: "+e);
              jedisPool.returnBrokenResource(jedis);
            }

//            System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
//            logger.info("Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
//            System.out.println( String.format("Processed %s", new String(delivery.getBody())));
//            logger.info(String.format("Processed %s", new String(delivery.getBody())));
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//            jedisPool.returnResource(jedis);     //In JedisPool mode, the Jedis resource is returned to the resource pool.
// https://partners-intl.aliyun.com/help/en/doc-detail/98726.htm
          };
          // process messages
          channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    };

    // start threads and block to receive messages
//    Thread recv1 = new Thread(runnable);
//    Thread recv2 = new Thread(runnable);
//    recv1.start();
//    recv2.start();
//    Properties pro = new Properties();
//    pro.load(new FileReader("props.txt"));
//    int MAX_THREADS = Integer.valueOf(pro.getProperty("MAX_THREAD"));
    ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

    for (int i = 0; i < MAX_THREADS ; i++) {
      executor.submit(runnable);
    }
  }
}
