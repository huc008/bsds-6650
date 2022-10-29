package model;

public class LiftRide {
  private final Integer time;
  private final Integer liftID;

  public LiftRide(Integer time, Integer liftID) {
    this.time = time;
    this.liftID = liftID;
  }

  public Integer getTime() {
    return time;
  }

  public Integer getLiftID() {
    return liftID;
  }

}