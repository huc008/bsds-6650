package Part2;

public class Config {
  // 18.237.41.167 8080
  // 64 20000 40 20
  public static String BASE_PATH = "http://54.149.21.71:8080/Server_war";
  public static String PROJ = "/Server_war";
  /*
   * ski day is of length 420 minutes (7 hours - 9am-4pm) from when the lifts open until they all close.
   */
  public final static int SKI_TIME_MIN = 420;
  /**
   * The constant to transfer minutes to milliseconds.
   */
  public final static int MILLS_TO_SEC = 1000;

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
