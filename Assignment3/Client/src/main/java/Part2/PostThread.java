package Part2;

import static Part2.Config.*;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class PostThread extends Thread {

  private final int resortID;
  private final int seasonID;
  private final int dayID;
  private final int liftID;
  private final int startSkierID;
  private final int endSkierID;
  private final int startTime;
  private final int endTime;
  private final CountDownLatch latch;
  private final int numRequests;
  private PhaseStatus status;
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
    for (int i = 0; i < this.numRequests; i++) {
      responseCode = -1;
      LiftRide liftRide = new LiftRide();
      liftRide = liftRide.time( this.startTime + ThreadLocalRandom.current().nextInt(this.endTime - this.startTime));
      liftRide = liftRide.liftID(1 + ThreadLocalRandom.current().nextInt(this.liftID));
      liftRide = liftRide.waitTime(ThreadLocalRandom.current().nextInt(10));
      skierID = this.startSkierID + ThreadLocalRandom.current().nextInt(this.endSkierID - this.startSkierID);
      thStartTime = System.currentTimeMillis();
//      Thread.sleep(1000);
      try {
        apiInstance.writeNewLiftRide(liftRide, this.resortID, String.valueOf(this.seasonID), String.valueOf(this.dayID), skierID);
        responseCode = SC_CREATED;
        success++;
      } catch (ApiException e) {
        e.printStackTrace();
        System.out.println("WriteNewLiftRide error...Retry at most five times");
        retry(liftRide);
      }
      thEndTime = System.currentTimeMillis();
      long latency = thEndTime - thStartTime;
      String record = thStartTime + ",POST," + latency + "," + responseCode + "\n";
      data.add(record);
      /**
       * a skierID from the range of ids passed to the thread
       * a lift number (liftID)
       * a time value from the range of minutes passed to each thread (between start and end time)
       * a wait time between 0 and 10444
       * With our example, if numRuns=20, each thread will send 4x(1024/16) POST requests.
       *
       * The server will return an HTTP 201 response code for a successful POST operation. As soon as the 201 is received, the client should immediately send the next request until it has exhausted the number of requests to send.
       *
       * If the client receives a 5XX response code (Web server error), or a 4XX response code (from your servlet), it should retry the request up to 5 times before counting it as a failed request.
       */
    }

    this.status.addSuccess(success);
    this.status.addFail(fail);
    this.status.addData(data);
    this.latch.countDown();
    this.allPhasesLatch.countDown();


  }

  private void retry(LiftRide liftRide) {
    int remain = 5;
    while(remain>0 && responseCode != SC_CREATED){
      try {
        apiInstance.writeNewLiftRide(liftRide, this.resortID, String.valueOf(this.seasonID), String.valueOf(this.dayID), skierID);
        responseCode = SC_CREATED;
        success++;
      } catch (ApiException e) {
        e.printStackTrace();
        remain--;
        responseCode = e.getCode();
        System.out.println("WriteNewLiftRide error...Retry at most "+ remain +" times");
        System.out.println(e.getCode());
        System.out.println(e.getResponseBody());
        System.out.println(e.getResponseHeaders());
        System.out.println(status.getSuccess());
      }
    }
    if(remain == 0 && responseCode != SC_CREATED){
      fail++;
    }
  }



}