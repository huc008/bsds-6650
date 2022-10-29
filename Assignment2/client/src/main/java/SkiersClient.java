import static java.lang.Math.round;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import model.SkiersFactory;

public class SkiersClient {
  private static final Integer NUM_REQUESTS = 200000;
  private static final Integer NUM_THREADS = 200;

  public static void main(String[] args) throws InterruptedException {
    long start = System.currentTimeMillis();
    BlockingQueue<SkiersFactory> dataBuffer = new LinkedBlockingQueue<>();

    //There is one thread producer keep generating the NUM_POSTS of skiers data
    //and put the generated skiers data into dataBuffer waiting for processes by consumers
    Thread producerSingleThread = new Thread(new Producer(dataBuffer, NUM_REQUESTS));
    producerSingleThread.start();

    //There is multi-thread consumer working concurrently to process the data in the dataBuffer
    runPhase("Phase1", NUM_THREADS, NUM_REQUESTS, dataBuffer);
//    runPhase("Phase2", 84, 150000, dataBuffer);

    long end = System.currentTimeMillis();
    double runtime = (end - start)*0.001;
    System.out.println("============ PART 1 OUTPUT =============");
    System.out.println("Total run time to send 200k requests: " + round(runtime) + "/s");
    System.out.println("Total throughput to send 200k requests: " + round(NUM_REQUESTS /runtime) + "/s");
  }

  private static void runPhase (String phaseLabel, int numThread, int numRequest,
      BlockingQueue<SkiersFactory> dataBuffer) throws InterruptedException {
    MultiThreadWrapper phase = new MultiThreadWrapper(phaseLabel, numThread, numRequest, dataBuffer);
    phase.startPhase();
    phase.finishPhase();
    phase.phaseOutputs();
  }
}
