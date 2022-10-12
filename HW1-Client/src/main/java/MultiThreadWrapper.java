import static java.lang.Math.round;

import io.swagger.client.api.SkiersApi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadWrapper {

  private final Integer numThreads;
  private final Integer numRequests;
  private final String phaseLabel;
  private final SkiersApi skierApi;
  private final BlockingQueue<SkiersFactory> dataBuffer;
  private final CountDownLatch phaseSignal;
  private final CountDownLatch phaseFinish;
  private long start;
  private long end;
  public static List<Double> latency = Collections.synchronizedList(new ArrayList<>());
  private final AtomicInteger SUCCESSFUL = new AtomicInteger(0);
  private final AtomicInteger UNSUCCESSFUL = new AtomicInteger(0);

  public MultiThreadWrapper(String phaseLabel, Integer numThreads, Integer numRequests, SkiersApi skierApi,
      CountDownLatch phaseSignal, BlockingQueue<SkiersFactory> dataBuffer) {
    this.phaseLabel = phaseLabel;
    this.numThreads = numThreads;
    this.numRequests = numRequests;
    this.phaseFinish = new CountDownLatch(this.numRequests);
    this.phaseSignal = phaseSignal;
    this.skierApi = skierApi;
    this.dataBuffer = dataBuffer;
  }

  public void startPhase() {
    this.start = System.currentTimeMillis();
    System.out.println("========== " + this.phaseLabel + " is starting ==========");
    for (int i = 0; i < this.numThreads; i++) {
      System.out.println("thread " + i + "starting initializing Consumer");
      new Consumer(this.dataBuffer, this.numRequests, SUCCESSFUL, UNSUCCESSFUL, this.phaseFinish).start();
      System.out.println("thread " + i + "finish initializing Consumer");
    }
  }

  public void finishPhase() throws InterruptedException {
    System.out.println("========== " + this.phaseLabel + " finishPhase ==========");
    this.phaseFinish.await();
  }

  public void phaseOutputs() {
    this.end = System.currentTimeMillis();
    double wallTime = round((this.end - this.start) * 0.001);
    System.out.println("=============== " + this.phaseLabel + " STATS ================");
    System.out.println("Number of successful POST requests: " + SUCCESSFUL);
    System.out.println("Number of unsuccessful POST requests: " + UNSUCCESSFUL);
    System.out.println("Wall Time: " + (wallTime) + " seconds");
    System.out.println(
        "Throughput = " + round(SUCCESSFUL.intValue() + UNSUCCESSFUL.intValue()
            / wallTime) + "/s for " +
            this.numThreads + " threads");
  }
}