package Part1;

/**
 * Submit your work to Blackboard Assignment 1 as a pdf document. The document should contain:
 *
 * the URL for your git repo. Make sure that the code for the client part 1 and part 2 are in seperate folders in your repo
 * a 1-2 page description of your client design. Include major classes, packages, relationships, whatever you need to convey concisely how your client works.
 * Include Little’s Law throughput predictions.
 * Client (Part 1) - run your client with 32, 64, 128 and 256 threads, with numSkiers=20000, and numLifts=40.
 * Include the outputs of each run in your submission (showing the wall time) and plot a simple chart showing the wall time by the number of threads.
 * This should be a screen shot of your output window.
 * Client (Part 2) - run the client as per Part 1, showing the output window for each run.
 * Also generate a plot of throughput and mean response time against number of threads.
 * Again, this should be a screen shot of your output window.
 */

public class Config {




  public static String BASE_PATH = "http://54.149.21.71:8080/Server_war";
  public static String PROJ = "/Server_war";
  /*
   * ski day is of length 420 minutes (7 hours - 9am-4pm) from when the lifts open until they all close.
   */
  public final static int SKI_TIME_MIN = 420;
  /**
   * The constant to transfer minutes to milliseconds.
   */
  public final static int MILLS_TO_SEC= 1000;

  public final static double PHASE1_RUNS_RATE = 0.2;
  public final static double PHASE2_RUNS_RATE  = 0.4;
  public final static double PHASE3_RUNS_RATE  = 0.1;

  public static final int SEASON_ID = 2022;
  public static final int DAY_ID = 1;
  public static final int RESORT_ID = 1;

  public static final int PHASE1_STARTTIME = 1;
  public static final int PHASE1_ENDTIME = 90;
  public static final int PHASE2_STARTTIME = 91;
  public static final int PHASE2_ENDTIME = 360;
  public static final int PHASE3_STARTTIME = 361;
  public static final int PHASE3_ENDTIME = 420;

  public static final int SC_CREATED = 201;
  public static final int SC_OK = 200;

}