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


public class SkierService {

  public final static String VH = "/";
  public final static int PORT = 5672;
  public final static String QUEUE_NAME = "SKIER_TASK";
  public final static String USERNAME = "user";
  public final static String PASSWORD = "password";
  public final static int MAX_THREADS = 128;
  public final static String SEP = "__";
  public final static String VERTICAL_TOTAl = "vertical";
  public final static String DAY_TOTAl = "day";
  public static final String SKIER = "SKIER";
  public static final String LIFT_TOTAl = "lift";
  public static final int DB_SKIER = 0;



  public static void main(String[] argv) throws Exception {
//    ConcurrentHashMap<Integer, CopyOnWriteArrayList<String>> map = new ConcurrentHashMap<>();
//    Logger logger = LoggerFactory.getLogger(SkierService.class);
    String RMQ_HOST = argv[0].trim();
    String REDIS_HOST = argv[1].trim();
    JedisPoolUtil.setHOST(REDIS_HOST);
    Gson gson = new Gson();
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
    jedis.select(DB_SKIER);
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        Jedis jedis = jedisPool.getResource();
        jedis.select(0);
        try{
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
//            jedis.hset(SKIER,skierId,json);
            String v = String.valueOf(1);
            String k = "k";
            String k2 = "k";
//            try {
//
//              //GET/skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
//              //“For skier N, what are the vertical totals for each ski day?” [ #vertical liftID*10 / each day,skierN]
//              k = SKIER+skierId+SEP+dayId+SEP+VERTICAL_TOTAl;
//              long vVer = Long.valueOf(liftId)*10;
//              if(!jedis.exists(k)) {
//                jedis.set(k, vVer+"");
//              }
//              else{
////                jedis.incrBy(k,vVer);
//                long i = Long.valueOf(jedis.get(k))+vVer;
//                jedis.set(k,String.valueOf(i));
//              }
//
//
//
//            } catch (Exception e) {
//              System.err.println("expire key: "+k+". Error: "+e);
//              jedisPool.returnBrokenResource(jedis);
//            }
//
            try {

              // “For skier N, how many days have they skied this season?”  [#days/this season, skierN]
              k= SKIER+skierId+SEP+DAY_TOTAl;
              k2 = SKIER+skierId+SEP+dayId+seasonId+DAY_TOTAl;
              if(!jedis.exists(k)) {
                jedis.set(k2, v);
                jedis.set(k,v);
              }
              else if(!jedis.exists(k2)){
//                jedis.incr(k);
                long i = Long.parseLong(jedis.get(k))+1;
                jedis.set(k,String.valueOf(i));
              }

            } catch (Exception e) {
              System.err.println("expire key: "+DAY_TOTAl+". Error: "+e);
              jedisPool.returnBrokenResource(jedis);
            }

            try {
              //“For skier N, show me the lifts they rode on each ski day” [#lifts / each day, skierN]
              k= SKIER+skierId+SEP+dayId+SEP+LIFT_TOTAl;
              if(!jedis.exists(k)) {
                jedis.set(k, v);
              }
              else{
                //                jedis.incr(k);
                long i = Long.parseLong(jedis.get(k))+1;
                jedis.set(k,String.valueOf(i));
              }
//              jedis.hset(skierId,  "resortId", resortId);
//              jedis.hset(skierId,  "seasonId", seasonId);
//              jedis.hset(skierId, "dayId", dayId);
//              jedis.hset(skierId, "liftId", liftId);
//              jedis.hset(skierId, "time", ""+liftRide.getTime());
//              jedis.hset(skierId,  "waitTime", ""+liftRide.getWaitTime());


            } catch (Exception e) {
              System.err.println("expire key: "+LIFT_TOTAl+". Error: "+e);
              jedisPool.returnBrokenResource(jedis);
            }


//            System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
//            logger.info("Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
//            System.out.println( String.format("Processed %s", new String(delivery.getBody())));
//            logger.info(String.format("Processed %s", new String(delivery.getBody())));
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//                 //In JedisPool mode, the Jedis resource is returned to the resource pool.
// https://partners-intl.aliyun.com/help/en/doc-detail/98726.htm
          };
          // process messages
          channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
        } catch (IOException e) {
          e.printStackTrace();
          System.err.println("Error: "+". Error: "+e);

        }
//        finally {
//          if(jedis != null)
//          jedisPool.returnResource(jedis);
//        }
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
