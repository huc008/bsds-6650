package model;

import io.swagger.client.model.LiftRide;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import lombok.Data;
import lombok.Getter;

@Data
public class SkiersFactory {
  private static final int LOWERBOUND = 1;
  private static final int MAX_SKIER_ID = 100000;
  private static final int MAX_RESORT_ID = 10;
  private static final int MAX_LIFT_ID = 40;
  private static final int MAX_LIFT_TIME = 360;
  private static final String SEASON_ID = "2022";
  @Getter public static LiftRide liftRide = new LiftRide();
  @Getter public static Integer resortID;
  @Getter public static String seasonID;
  @Getter public static String dayID;
  @Getter public static Integer skierID;
  private static int randomSeed;

  public SkiersFactory(int random) {
    this.randomSeed = random;
    liftRide.setLiftID(idGenerator(MAX_LIFT_ID));
    liftRide.setTime(idGenerator(MAX_LIFT_TIME));
    resortID = idGenerator(MAX_RESORT_ID);
    seasonID = SEASON_ID;
    dayID = String.valueOf(LOWERBOUND);
    skierID = idGenerator(MAX_SKIER_ID);
  }

  public static Integer idGenerator(int upperBound) {
    Random random = new Random(randomSeed);
    int res = random.nextInt(upperBound) + LOWERBOUND;
//    System.out.println(res);
    return random.nextInt(upperBound) + LOWERBOUND;
  }
}
