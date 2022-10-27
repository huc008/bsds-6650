import static java.lang.Math.round;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import model.SkiersFactory;

public class SkiersClient {
  private static final Integer NUM_REQUESTS = 100;

  public static void main(String[] args) throws InterruptedException {
    long start = System.currentTimeMillis();
    BlockingQueue<SkiersFactory> dataBuffer = new LinkedBlockingQueue<>();

    //There is one thread producer keep generating the NUM_POSTS of skiers data
    //and put the generated skiers data into dataBuffer waiting for processes by consumers
    Thread producerSingleThread = new Thread(new Producer(dataBuffer, NUM_REQUESTS));
    producerSingleThread.start();

    //There are multi-thread consumer working concurrently to process the data in the dataBuffer
    int numThreads = 64;

    MultiThreadWrapper phase1 = new MultiThreadWrapper("Phase 1", numThreads, NUM_REQUESTS,
        dataBuffer);
    phase1.startPhase();
    producerSingleThread.join();
    phase1.finishPhase();
    phase1.phaseOutputs();
    long end = System.currentTimeMillis();
    double runtime = (end - start)*0.001;
    System.out.println("============ PART 1 OUTPUT =============");
    System.out.println("Total run time to send 200k requests: " + round(runtime) + "/s");
    System.out.println("Total throughput to send 200k requests: " + round(NUM_REQUESTS /runtime) + "/s");
  }
}
