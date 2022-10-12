public class SkierReqBody {
  private Integer time;
  private Integer liftID;

  public SkierReqBody(Integer time, Integer liftID, Integer waitTime) {
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