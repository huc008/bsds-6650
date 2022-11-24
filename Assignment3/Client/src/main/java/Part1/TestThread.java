package Part1;

import static Part1.Config.BASE_PATH;
import static Part1.Config.MILLS_TO_SEC;
import static Part1.Config.SC_CREATED;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.concurrent.CircuitBreaker;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;

public class TestThread extends Thread{

  private static final int NUM_THREADS = 1;
  private final int resortID;
  private final int seasonID;
  private final int dayID;
  private final int liftID;
  private final int startSkierID;
  private final int endSkierID;
  private final int startTime;
  private final int endTime;
  private final int numRequests;
  private final PhaseStatus status;
  private final CountDownLatch allPhasesLatch;
  List<String> data;
  SkiersApi apiInstance;
  ApiClient client;
  static long wallTime = 0;

  int skierID = -1;
  int responseCode = -1;
  long thStartTime = 0;
  long thEndTime = 0;
  int success = 0;
  int fail = 0;

  public TestThread(int resortID, int seasonID, int dayID, int liftID,
      int startSkierID, int endSkierID, int startTime, int endTime,
       int numRequests, PhaseStatus status,
      CountDownLatch allPhasesLatch) {
    this.resortID = resortID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.liftID = liftID;
    this.startSkierID = startSkierID;
    this.endSkierID = endSkierID;
    this.startTime = startTime;
    this.endTime = endTime;
    this.numRequests = numRequests;
    this.status = status;
    this.allPhasesLatch = allPhasesLatch;
    data = new ArrayList<>();
    apiInstance = new SkiersApi();
    client = apiInstance.getApiClient();
    client.setBasePath(BASE_PATH);
  }
  public static void main(String[] args) throws InterruptedException {
    CountDownLatch testLatch = new CountDownLatch(NUM_THREADS);
    PhaseStatus testStatus = new PhaseStatus();
    long start = System.currentTimeMillis();
    for(int i=0; i<NUM_THREADS; i++){
        int startSkier = 1;
        int endSkier = 100;


//      CountDownLatch threadLatch = new CountDownLatch(0);
      TestThread thread = new TestThread(1, 1, 1, 1, startSkier,
          endSkier, 1, 420, 10000, testStatus,
          testLatch);
      thread.start();
    }
    testLatch.await();
    long end = System.currentTimeMillis();
    wallTime = end - start;
    printStats(testStatus);
  }
  @Override
  public void run() {
    success = 0;
    fail = 0;
    for (int i = 0; i < this.numRequests; i++) {
      responseCode = -1;
      thStartTime = System.currentTimeMillis();
      LiftRide liftRide = new LiftRide();
      liftRide = liftRide.time( this.startTime + ThreadLocalRandom.current().nextInt(this.endTime - this.startTime));
      liftRide = liftRide.liftID( 1 + ThreadLocalRandom.current().nextInt(this.liftID));
      skierID = this.startSkierID + ThreadLocalRandom.current().nextInt(this.endSkierID - this.startSkierID);
      thStartTime = System.currentTimeMillis();
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
      /**
       * a skierID from the range of ids passed to the thread
       * a lift number (liftID)
       * a time value from the range of minutes passed to each thread (between start and end time)
       * a wait time between 0 and 10
       * With our example, if numRuns=20, each thread will send 4x(1024/16) POST requests.
       *
       * The server will return an HTTP 201 response code for a successful POST operation. As soon as the 201 is received, the client should immediately send the next request until it has exhausted the number of requests to send.
       *
       * If the client receives a 5XX response code (Web server error), or a 4XX response code (from your servlet), it should retry the request up to 5 times before counting it as a failed request.
       */
    }

    this.status.addSuccess(success);
    this.status.addFail(fail);
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

  private static void printStats(PhaseStatus status) {
    /**
     * When all threads from all phases are complete, the programs should print out:
     *
     * number of successful requests sent
     * number of unsuccessful requests (ideally should be 0)
     * the total run time (wall time) for all phases to complete. Calculate this by taking a timestamp before commencing Phase 1 and another after all Phase 3 threads are complete.
     * the total throughput in requests per second (total number of requests/wall time)
     *
     */
    System.out.println("After all phases: ");
    System.out.println("Number of successful posts: " + status.getSuccess());
    System.out.println("Number of unsuccessful posts: " + status.getFail());
    System.out.println("Wall time: " + (wallTime / MILLS_TO_SEC) + "seconds");
    System.out.println("Throughput: " + status.getSuccess()/(wallTime / MILLS_TO_SEC) + " requests/sec");
  }

}
