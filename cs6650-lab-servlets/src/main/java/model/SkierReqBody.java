package model;

public class SkierReqBody {
  private Integer time;
  private Integer liftID;

  public SkierReqBody(Integer time, Integer liftID) {
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