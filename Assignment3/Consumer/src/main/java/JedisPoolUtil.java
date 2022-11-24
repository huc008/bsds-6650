

import java.util.ArrayList;
import java.util.List;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

public class JedisPoolUtil {

  public static void setHOST(String HOST) {
    JedisPoolUtil.HOST = HOST;
  }

  private static volatile JedisPool jedisPool = null;
  public static String HOST = "127.0.0.1";
  public static String PASSWORD = "6650";
  private JedisPoolUtil(){

  }
  public  static JedisPool getJedisPoolInstance() {
//    poolInit();
//    Jedis jedis = null;
    try {
      if (null == jedisPool) {
        synchronized (JedisPoolUtil.class){
          if(null == jedisPool){
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(256); //128
            poolConfig.setMaxIdle(256); // 128
            poolConfig.setMinIdle(16);
            poolConfig.setTestWhileIdle(true);
            poolConfig.setMaxWaitMillis(1000*100);
            poolConfig.setNumTestsPerEvictionRun(3);
            poolConfig.setBlockWhenExhausted(true);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
//            jedisPool = new JedisPool(poolConfig, "localhost",6679);
            jedisPool = new JedisPool(poolConfig, HOST,6379, Protocol.DEFAULT_TIMEOUT, PASSWORD);
            Jedis jedis = jedisPool.getResource();
            jedis.flushDB();
            //preload jedis pool
//            List<Jedis> minIdleJedisList = new ArrayList<Jedis>(poolConfig.getMinIdle());
//
//            for (int i = 0; i < poolConfig.getMinIdle(); i++) {
//              Jedis jedis = null;
//              try {
//                jedis = jedisPool.getResource();
//                minIdleJedisList.add(jedis);
//                jedis.ping();
//              } catch (Exception e) {
//                System.err.println(e.getMessage());
//              }
//              finally {
//                minIdleJedisList.get(0).flushDB();
//              }
//            }

//            for (int i = 0; i < poolConfig.getMinIdle(); i++) {
//              Jedis jedis = null;
//              try {
//                jedis = minIdleJedisList.get(i);
//                jedis.close();
//              } catch (Exception e) {
//                System.err.println(e.getMessage());
//              }
//            }
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Get jedis error : " + e);
    }
    return jedisPool;
  }
  public void stop() {
    if (jedisPool != null) {
      jedisPool.destroy();
      jedisPool = null;
    }
  }


}

