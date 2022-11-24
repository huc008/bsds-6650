package Servlet;

import DataAccessLayer.LiftRideDao;
import Model.DummyObject;
import Model.LiftRide;
import Model.RequestBody;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;


public class
SkiersServlet extends HttpServlet {

  private final static String QUEUE_NAME = "SKIER_TASK";
  private final static String RMQ_HOST = "54.218.104.130";
  private final static String USERNAME = "user";
  private final static String PASSWORD = "password";
  private static final int MAX_POOL_SIZE = 100;
  private Gson gson = new Gson();
  private ConnectionFactory factory;
  private Connection connection;
  Logger logger = LoggerFactory.getLogger(SkiersServlet.class);
  GenericObjectPool pool;
  EventCountCircuitBreaker breaker;
  @Override

  public void init() throws ServletException {
    super.init();
    factory = new ConnectionFactory();
    factory.setHost(RMQ_HOST);
    factory.setUsername(USERNAME);
    factory.setPassword(PASSWORD);
    logger.info(" [*] Waiting for messages. To exit press CTRL+C");
    try {
      connection = factory.newConnection();
    } catch (IOException | TimeoutException ex) {
      logger.error(ex.getMessage());
    }

    BasePooledObjectFactory<Channel> channelFactory = new BasePooledObjectFactory<>() {
      @Override
      public Channel create() throws Exception {
        return connection.createChannel();
      }
      @Override
      public PooledObject<Channel> wrap(Channel channel) {
        return new DefaultPooledObject<>(channel);
      }
    };
    pool = new GenericObjectPool<>(channelFactory);
    breaker = new EventCountCircuitBreaker(3000, 1000, TimeUnit.MILLISECONDS, 1500, 1000,
        TimeUnit.MILLISECONDS);
  }

  @Override
  public void destroy() {
    super.destroy();
    try {
      pool.close();
      connection.close();
    } catch (IOException e) {
      e.printStackTrace();
      logger.error(e.getMessage());
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setContentType("text/plain");
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
      res.getWriter().write("Please provide url");
      return;
    }

    String[] urlParts = urlPath.substring(1).split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (isUrlValidGet(urlParts) != 0) {
      res.setContentType("text/plain");
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid format: "+ urlPath + "Reason: " + isUrlValidGet(urlParts) );
      res.getWriter().write("Please provide follow the correct format");
      return;
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      DummyObject dummyObject = new DummyObject(req.getRequestURI());
      String objJsonString = this.gson.toJson(dummyObject);
      PrintWriter out = res.getWriter();
      res.setContentType("application/json");
      res.setCharacterEncoding("UTF-8");
      out.print(objJsonString);
      out.flush();

    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    String urlPath = req.getPathInfo();
    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setContentType("text/plain");
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
      res.getWriter().write("Please provide url");
      return;
    }
    String[] urlParts = urlPath.substring(1).split("/");
    if (isUrlValidPost(urlParts) != 0) {
      res.setContentType("text/plain");
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid format: "+ urlPath + "Reason: " + isUrlValidGet(urlParts) );
      res.getWriter().write("Please provide follow the correct format");
      return;
    } else {

      //This step should be very straightforward once you have tested that your DAO object works. For example, in POST method on /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID},
      //in your doPost method in the corresponding Servlet, you basically need to
      //extract values from URL path params and request body
      //construct a LiftRide object with those values, and
      //pass that object to the DAO layer
      //of course, don’t forget path validations and some other necessary steps.
      PrintWriter out = res.getWriter();
      res.setContentType("application/json");
      res.setCharacterEncoding("UTF-8");
      // request body
      String message = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

      Map<String, Double> map = gson.fromJson(message, Map.class);

//       request body using gson
//      RequestBody body = gson.fromJson(req.getReader(), RequestBody.class);
//       liftRide str

      Channel channel  = null;
      try {
        channel = (Channel) pool.borrowObject();
      } catch (Exception e) {
        e.printStackTrace();
      }


//      channel = connection.createChannel();

      channel.queueDeclare(QUEUE_NAME, false, false, false, null);
      if(breaker.incrementAndCheckState()) {
        LiftRide liftRide = insertLiftRide(urlParts, map);
        String liftRideJson = this.gson.toJson(liftRide);
        channel.basicPublish("", QUEUE_NAME, null, liftRideJson.getBytes(StandardCharsets.UTF_8));
        System.out.println(" [x] Sent '" + liftRideJson + "'");
        logger.info(" [x] Sent '" + message + "'");
//      try {
//        channel.close();
//      } catch (TimeoutException e) {
//        e.printStackTrace();
//      }
        pool.returnObject(channel);
        res.setStatus(HttpServletResponse.SC_CREATED);
        System.out.println(message);
        out.write(liftRide.toString());
        out.flush();
      }
       else{
        res.setStatus(HttpServletResponse.SC_REQUEST_TIMEOUT);
        out.flush();
      }
    }
  }

  private LiftRide insertLiftRide(String[] urlPath, Map<String, Double> parameterMap) {
    int resortID = Integer.valueOf(urlPath[0]);
    int seasonID = Integer.valueOf(urlPath[2]);
    int dayID = Integer.valueOf(urlPath[4]);
    int skierID = Integer.valueOf(urlPath[6]);
    int time = (int)parameterMap.get("time").doubleValue();
    int liftID =  (int)parameterMap.get("liftID").doubleValue();
    int wait = (int)parameterMap.get("waitTime").doubleValue();

    LiftRide newLiftRide = new LiftRide(skierID, resortID, seasonID, dayID, time,liftID);
    newLiftRide.setWaitTime(wait);
    LiftRideDao liftRideDao = new LiftRideDao();
//    liftRideDao.createLiftRide(newLiftRide);
    return newLiftRide;
  }



  private int isUrlValidGet(String[] urlPath) {
    /**
     *
     * GET /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
     * GET /skiers/{skierID}/vertical
     */
    try{
      if(urlPath.length == 7){
        Long resortID = Long.valueOf(urlPath[0]);
        Long seasonID = Long.valueOf(urlPath[2]);
        Long dayID = Long.valueOf(urlPath[4]);
        Long skierID = Long.valueOf(urlPath[6]);
        if(urlPath[1].equals("seasons") && urlPath[3].equals("days")&&urlPath[5].equals("skiers") ){
          return 0;
        }
      }
      else if(urlPath.length == 2){
        Long skierID = Long.valueOf(urlPath[0]);
        if(urlPath[1].equals("vertical")){
          return 0;
        }
      }
    }
    catch (NumberFormatException e){
      System.out.println(e.getMessage());
      return -2;
    }
    return -1;
  }


  private int isUrlValidPost(String[] urlPath) {
    /**
     * POST /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
     */
    try{
      if(urlPath.length == 7){
        Long resortID = Long.valueOf(urlPath[0]);
        Long seasonID = Long.valueOf(urlPath[2]);
        Long dayID = Long.valueOf(urlPath[4]);
        Long skierID = Long.valueOf(urlPath[6]);
        if(urlPath[1].equals("seasons") && urlPath[3].equals("days")&&urlPath[5].equals("skiers") ){
          return 0;
        }
      }
    }
    catch (NumberFormatException e){
      System.out.println(e.getMessage());
      return -2;
    }
    return -1;
  }

}
