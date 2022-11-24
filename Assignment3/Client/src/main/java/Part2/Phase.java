package Part2;

import static Part2.Config.MILLS_TO_SEC;

import java.security.SecureRandom;
import java.util.concurrent.CountDownLatch;

public class Phase {
  private int numThreads;
  private int numSkiers;
  private int resortID;
  private int seasonID;
  private int dayID;
  private int numLifts;
  private int startTime;
  private int endTime;
  private int numRequests;
  private int waitThreads;
  private PhaseStatus status;
  private CountDownLatch allPhasesLatch;
  private int waitTime = 0;
  private static final SecureRandom random = new SecureRandom();

  public Phase(int numThreads, int numRequests, double waitRate, int numSkiers, int numLifts,
      int resortID, int seasonID,
      int dayID, int startTime, int endTime,
      PhaseStatus status, CountDownLatch allPhasesLatch) {
    this.numRequests = numRequests;
    this.numThreads = numThreads;
    this.startTime = startTime;
    this.endTime = endTime;
    this.waitThreads = (int)Math.ceil(waitRate*numThreads);
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
    this.waitThreads = (int)Math.ceil(waitRate*numThreads);
    this.status = status;
    this.allPhasesLatch = allPhasesLatch;
    this.numSkiers = numSkiers;
    this.resortID = resortID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.numLifts = numLifts;
    this.waitTime = waitTime;
  }

  public void startPhase() throws InterruptedException {
    System.out.println("In this phase: "+numThreads+" threads, "+numRequests+" requests per thread, "+ waitThreads+" waitThreads before next phase could start");
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