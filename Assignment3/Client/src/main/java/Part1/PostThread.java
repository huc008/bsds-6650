package Part1;

import static Part1.Config.*;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;


public class PostThread extends Thread {

  private final int resortID;
  private final int seasonID;
  private final int dayID;
  private final int liftID;
  private final int startSkierID;
  private final int endSkierID;
  private final int startTime;
  private final int endTime;
  private CountDownLatch latch;
  private final int numRequests;
  private final PhaseStatus status;
  private final CountDownLatch allPhasesLatch;
  List<String> data;
  SkiersApi apiInstance;
  ApiClient client;

  int skierID = -1;
  int responseCode = -1;
  long thStartTime = 0;
  long thEndTime = 0;
  int success = 0;
  int fail = 0;

  public PostThread(int resortID, int seasonID, int dayID, int liftID,
      int startSkierID, int endSkierID, int startTime, int endTime,
       CountDownLatch latch, int numRequests, PhaseStatus status,
      CountDownLatch allPhasesLatch) {
    this.resortID = resortID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.liftID = liftID;
    this.startSkierID = startSkierID;
    this.endSkierID = endSkierID;
    this.startTime = startTime;
    this.endTime = endTime;
    this.latch = latch;
    this.numRequests = numRequests;
    this.status = status;
    this.allPhasesLatch = allPhasesLatch;
    data = new ArrayList<>();
    apiInstance = new SkiersApi();
    client = apiInstance.getApiClient();
    client.setBasePath(BASE_PATH);
  }

  @Override
  public void run() {
    success = 0;
    fail = 0;
    EventCountCircuitBreaker breaker = new EventCountCircuitBreaker(1000, 1000, TimeUnit.MILLISECONDS, 500, 1000,
        TimeUnit.MILLISECONDS);
    AtomicInteger i = new AtomicInteger(0);
    while (i.get() < numRequests) {
      responseCode = -1;
      thStartTime = System.currentTimeMillis();
      LiftRide liftRide = new LiftRide();
      liftRide = liftRide.time( this.startTime + ThreadLocalRandom.current().nextInt(this.endTime - this.startTime));
      liftRide = liftRide.liftID( 1 + ThreadLocalRandom.current().nextInt(this.liftID));
      liftRide = liftRide.waitTime(ThreadLocalRandom.current().nextInt(10));
      skierID = this.startSkierID + ThreadLocalRandom.current().nextInt(this.endSkierID - this.startSkierID);
      thStartTime = System.currentTimeMillis();
      if (breaker.incrementAndCheckState()) {
        try {
          apiInstance.writeNewLiftRide(liftRide, this.resortID, String.valueOf(this.seasonID), String.valueOf(this.dayID), skierID);
          responseCode = SC_CREATED;
          success++;
          i.incrementAndGet();
        } catch (ApiException e) {
          System.out.println("WriteNewLiftRide error...Retry at most five times");
          System.out.println("Server response with: " + e.getCode());
          int remain = 5;
          while(remain>0 && responseCode != SC_CREATED) {
            if (breaker.incrementAndCheckState()) {
              try {
                apiInstance.writeNewLiftRide(liftRide, this.resortID, String.valueOf(this.seasonID),
                    String.valueOf(this.dayID), skierID);
                responseCode = SC_CREATED;
                success++;
                i.incrementAndGet();
              } catch (ApiException e2) {
                remain--;
                responseCode = e2.getCode();
                try {
                  Thread.sleep((5-remain)*100);
                } catch (InterruptedException ex) {
                  ex.printStackTrace();
                }
                System.out.println("WriteNewLiftRide error...Retry at most " + remain + " times");
              }
            }
          }
          if(remain == 0 && responseCode != SC_CREATED){
            fail++;
          }
        }
      }
      thEndTime = System.currentTimeMillis();

    }
    // once a PostThread finish sending all posts, latch count down
    this.status.addSuccess(success);
    this.status.addFail(fail);
    this.latch.countDown();
    this.allPhasesLatch.countDown();

  }

//  @Override
//  public void run() {
//    success = 0;
//    fail = 0;
//    int i = 0;
//    while (i < numRequests) {
//      responseCode = -1;
//      thStartTime = System.currentTimeMillis();
//      LiftRide liftRide = new LiftRide();
//      liftRide = liftRide.time( this.startTime + ThreadLocalRandom.current().nextInt(this.endTime - this.startTime));
//      liftRide = liftRide.liftID( 1 + ThreadLocalRandom.current().nextInt(this.liftID));
//      liftRide = liftRide.waitTime(ThreadLocalRandom.current().nextInt(10));
//      skierID = this.startSkierID + ThreadLocalRandom.current().nextInt(this.endSkierID - this.startSkierID);
//      thStartTime = System.currentTimeMillis();
//          try {
//            apiInstance.writeNewLiftRide(liftRide, this.resortID, String.valueOf(this.seasonID), String.valueOf(this.dayID), skierID);
//            responseCode = SC_CREATED;
//            success++;
//
//          } catch (ApiException e) {
//            System.out.println(e.getResponseBody());
//            System.out.println(e.getCode());
//            System.out.println(e.getResponseHeaders());
//            System.out.println(e.getMessage());
//              System.out.println("WriteNewLiftRide error...Retry at most five times");
//              int remain = 5;
//              while(remain>0 && responseCode != SC_CREATED) {
//                  try {
//                    apiInstance.writeNewLiftRide(liftRide, this.resortID, String.valueOf(this.seasonID),
//                        String.valueOf(this.dayID), skierID);
//                    responseCode = SC_CREATED;
//                    success++;
//                    i++;
//                  } catch (ApiException e2) {
//                    remain--;
//                    responseCode = e2.getCode();
//                    try {
//                      Thread.sleep(5-remain);
//                    } catch (InterruptedException ex) {
//                      ex.printStackTrace();
//                    }
//                    System.out.println("WriteNewLiftRide error...Retry at most " + remain + " times");
//                  }
//
//              }
//              if(remain == 0 && responseCode != SC_CREATED){
//                fail++;
//              }
//            i++;
//      }
//      thEndTime = System.currentTimeMillis();
//
//    }
//    // once a PostThread finish sending all posts, latch count down
//    this.status.addSuccess(success);
//    this.status.addFail(fail);
//    this.latch.countDown();
//    this.allPhasesLatch.countDown();
//
//  }




}