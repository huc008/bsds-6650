package Part1;
import java.security.SecureRandom;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;

public class Phase {

  private final int numThreads;
  private final int numSkiers;
  private final int resortID;
  private final int seasonID;
  private final int dayID;
  private final int numLifts;
  private final int startTime;
  private final int endTime;
  private final int numRequests;
  private final int waitThreads;
  private final PhaseStatus status;
  private final CountDownLatch allPhasesLatch;
  private int waitTime = 0;
  private final static SecureRandom random = new SecureRandom();

  public Phase(int numThreads, int numRequests, double waitRate, int numSkiers, int numLifts,
      int resortID, int seasonID,
      int dayID, int startTime, int endTime,
      PhaseStatus status, CountDownLatch allPhasesLatch) {
    this.numRequests = numRequests;
    this.numThreads = numThreads;
    this.startTime = startTime;
    this.endTime = endTime;
    this.waitThreads = (int) Math.ceil(waitRate * numThreads);
    this.status = status;
    this.allPhasesLatch = allPhasesLatch;
    this.numSkiers = numSkiers;
    this.resortID = resortID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.numLifts = numLifts;
  }

  public Phase(int numThreads, int numRequests, double waitRate, int numSkiers, int numLifts,
      int resortID, int seasonID,
      int dayID, int startTime, int endTime,
      PhaseStatus status, CountDownLatch allPhasesLatch, int waitTime) {
    this.numRequests = numRequests;
    this.numThreads = numThreads;
    this.startTime = startTime;
    this.endTime = endTime;
    this.waitThreads = (int) Math.ceil(waitRate * numThreads);
    this.status = status;
    this.allPhasesLatch = allPhasesLatch;
    this.numSkiers = numSkiers;
    this.resortID = resortID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.numLifts = numLifts;
    this.waitTime = waitTime;
  }

//  public void startPhase() throws InterruptedException {
//    System.out.println("In this phase: "+numThreads+" threads, "+numRequests+" requests per thread, "+ waitThreads+" waitThreads before next phase could start");
//    CountDownLatch latch = new CountDownLatch(waitThreads);
//    for (int i = 0; i < this.numThreads; i++) {
//      int startSkier = 1 + (i * (numSkiers / numThreads));
//      int endSkier = (i + 1) * (numSkiers / numThreads);
//      Thread.sleep((long) waitTime * random.nextInt(11));
//      PostThread thread = new PostThread(resortID, seasonID, dayID, numLifts, startSkier,
//          endSkier, startTime, endTime, latch, numRequests, status,
//          allPhasesLatch);
//      thread.start();
//    }
//    latch.await();
//  }

  public void startPhase() throws InterruptedException {

    System.out.println(
        "In this phase: " + numThreads + " threads, " + numRequests + " requests per thread, "
            + waitThreads + " waitThreads before next phase could start");

    CountDownLatch latch = new CountDownLatch(waitThreads);
    for (int i = 0; i < this.numThreads; i++) {
      int startSkier = 1 + (i * (numSkiers / numThreads));
      int endSkier = (i + 1) * (numSkiers / numThreads);
      Thread.sleep((long) waitTime * random.nextInt(11));
        PostThread thread = new PostThread(resortID, seasonID, dayID, numLifts, startSkier,
            endSkier, startTime, endTime, latch, numRequests, status,
            allPhasesLatch);
      thread.start();
    }
    latch.await();
  }
}