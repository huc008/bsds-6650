package Part1;

import static Part1.Config.*;


import io.swagger.client.ApiClient;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.concurrent.CircuitBreaker;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;

public class ClientApp {
  // 128 20000 40 20
  //35.89.62.137 8080

  private static int numThreads;
  private static int numSkiers;
  private static int numLifts;
  private static int numRuns;
  private static long before;
  private static long end;
  private static long wallTime;
  private static int phase1Threads;
  private static int phase2Threads;
  private static int phase3Threads;
  private static int totalThreads;

  private static final SecureRandom random = new SecureRandom();

  /**
   * Once you have your client calling the API, it’s time to build the multithreaded client fun time!!
   *
   * Your client should accept a set of parameters from the command line (or a parameter file) at startup. These are:
   *
   * maximum number of threads to run (numThreads - max 1024)
   * number of skier to generate lift rides for (numSkiers - max 100000), This is effectively the skier’s ID (skierID)
   * number of ski lifts (numLifts - range 5-60, default 40)
   * mean numbers of ski lifts each skier rides each day (numRuns - default 10, max 20)
   * IP/port address of the server
   *
   * Based on these values, your client will start up and execute 3 phases, with each phase sending a large number of lift ride events to the server API.
   *
   * Phase 1, the startup phase, will launch numThreads/4 threads, and each thread will be passed:
   *
   * a start and end range for skierIDs, so that each thread has an identical number of skierIDs, caluculated as numSkiers/(numThreads/4). Pass each thread a disjoint range of skierIDs so that the whole range of IDs is covered by the threads, ie, thread 0 has skierIDs from 1 to (numSkiers/(numThreads/4)), thread 1 has skierIDs from (1x(numSkiers/(numThreads/4)+1) to (numSkiers/(numThreads/4))x2
   * a start and end time, for this phase this is the first 90 minutes of the ski day (1-90)
   * For example if numThreads=64 and numSkiers=1024, we will launch 16 threads, with thread 0 passed skierID range 1 to 64, thread 1 range 65 to 128, and so on.
   *
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    parseCommandline();
    startThreads();
  }

  /**
   * Client (Part 1) - run your client with 32, 64, 128 and 256 threads,
   * with numSkiers=20000, and numLifts=40. Include the outputs of each run in your submission (showing the wall time)
   * and plot a simple chart showing the wall time by the number of threads.
   * This should be a screen shot of your output window.
   *
   */

  private static void startThreads() throws InterruptedException {
    phase1Threads = (int) Math.round((double)numThreads / 4);
    phase2Threads = numThreads;
    phase3Threads = (int) Math.round((double)numThreads / 10);
    int phase1Requests =
        (int) Math.round((numRuns * PHASE1_RUNS_RATE) * (numSkiers / (double)(phase1Threads)));
    int phase2Requests =
        (int) Math.round((numRuns * PHASE2_RUNS_RATE) * (numSkiers / (double)(numThreads)));
    int phase3Requests =
        (int) Math.round(numRuns * PHASE3_RUNS_RATE);
    totalThreads = numThreads + phase3Threads + phase1Threads;

    CountDownLatch allPhasesLatch = new CountDownLatch(totalThreads);
    PhaseStatus status = new PhaseStatus();

    Phase phase1 =
        new Phase(phase1Threads, phase1Requests, 0.2,
            numSkiers,numLifts, RESORT_ID, SEASON_ID, DAY_ID, PHASE1_STARTTIME, PHASE1_ENDTIME,status,allPhasesLatch);

    Phase phase2 =
        new Phase(phase2Threads, phase2Requests, 0.2,
            numSkiers,numLifts, RESORT_ID, SEASON_ID, DAY_ID, PHASE2_STARTTIME, PHASE2_ENDTIME,status,allPhasesLatch);

    Phase phase3 = new Phase(phase3Threads, phase3Requests, 0,
        numSkiers,numLifts, RESORT_ID, SEASON_ID, DAY_ID, PHASE3_STARTTIME, PHASE3_ENDTIME,status,allPhasesLatch);

      runAll (phase1, phase2, phase3, allPhasesLatch);
      printStats(status);

      System.out.println("Terminating program...");
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
    System.out.println("numThreads: "+numThreads+", numSkiers: "+numSkiers+", numLifts: "+numLifts+", numRuns: " +numRuns);
    System.out.println("After all phases: ");
    System.out.println("Number of successful posts: " + status.getSuccess());
    System.out.println("Number of unsuccessful posts: " + status.getFail());
    System.out.println("Wall time: " + (wallTime / MILLS_TO_SEC) + "seconds");
    System.out.println("Throughput: " + status.getSuccess()/(wallTime / MILLS_TO_SEC) + " requests/sec");
  }
  private static void runAll(Phase phase1, Phase phase2, Phase phase3, CountDownLatch allPhasesLatch)
      throws InterruptedException {
    System.out.println("Total threads: "+totalThreads);

    before = System.currentTimeMillis();
    System.out.println("======================================");
    long beforephase = System.currentTimeMillis();
    System.out.println("starting phase1...");
    System.out.println("There are "+phase1Threads+" concurrent threads...");
    phase1.startPhase();
    long afterphase = System.currentTimeMillis();
    System.out.println("Time passed in this phase: " + (afterphase-beforephase) +" ms");
    System.out.println("======================================");

    System.out.println("======================================");
    beforephase = System.currentTimeMillis();
    System.out.println("starting phase2...");
    int phase1wait = (int)Math.ceil(0.2*phase1Threads);
    System.out.println("There are "+(phase1Threads-phase1wait+phase2Threads)+" concurrent threads (including those from phase1) at start...");
    phase2.startPhase();
    afterphase = System.currentTimeMillis();
    System.out.println("Time passed in this phase: " + (afterphase-beforephase) +" ms");
    System.out.println("======================================");

    System.out.println("======================================");
    beforephase = System.currentTimeMillis();
    System.out.println("starting phase3...");
    System.out.println("There are "+allPhasesLatch.getCount()+" remaining threads...");
    phase3.startPhase();
    afterphase = System.currentTimeMillis();
    System.out.println("Time passed in this phase: " + (afterphase-beforephase) +" ms");
    System.out.println("======================================");

    allPhasesLatch.await();
    end = System.currentTimeMillis();
    wallTime = end - before;
  }



  private static void parseCommandline() throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    System.out.println("Please Enter numThreads, numSkiers, numLifts, numRuns, seperated by a white space");
    String input = br.readLine().trim();
    List<Integer> res = processInput(input);
    numThreads = res.get(0);
    numSkiers = res.get(1);
    numLifts = res.get(2);
    numRuns = res.get(3);
    System.out.println("Please Enter IP and port, seperated by a white space");
    input = br.readLine().trim();
    String[] info = input.split("\\s+");
    String ip = info[0];
    int port = Integer.parseInt(info[1]);
    BASE_PATH = "http://"+ip+":"+port+PROJ;
    System.out.println("numThreads | numSkiers | numLifts | numRuns:");
    System.out.println(res);
  }

  private static List<Integer> processInput(String input) {
    String[] arr1 = input.split("\\s+");
    List<Integer> res = new ArrayList<>();
    for(String s: arr1){
      res.add(Integer.parseInt(s));
    }
    return res;
  }
}