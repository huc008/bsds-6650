package Model;

import java.util.Objects;

public class LiftRide {

  private int resortId;
  private int seasonId;
  private int dayId;
  private int skierId;
  private int time;
  private int liftId;
  private int waitTime;

  public int getWaitTime() {
    return waitTime;
  }

  public void setWaitTime(int waitTime) {
    this.waitTime = waitTime;
  }

  public LiftRide(int skierId, int resortId, int seasonId, int dayId, int time, int liftId) {
    this.skierId = skierId;
    this.resortId = resortId;
    this.seasonId = seasonId;
    this.dayId = dayId;
    this.time = time;
    this.liftId = liftId;
    this.waitTime = waitTime;
  }

  public LiftRide(int skierId, int resortId, int seasonId, int dayId, int time, int liftId, int waitTime) {
    this.skierId = skierId;
    this.resortId = resortId;
    this.seasonId = seasonId;
    this.dayId = dayId;
    this.time = time;
    this.liftId = liftId;
    this.waitTime = waitTime;
  }

  @Override
  public String toString() {
    return "LiftRide{" +
        "skierId=" + skierId +
        ", resortId=" + resortId +
        ", seasonId=" + seasonId +
        ", dayId=" + dayId +
        ", time=" + time +
        ", liftId=" + liftId +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LiftRide)) {
      return false;
    }
    LiftRide liftRide = (LiftRide) o;
    return getSkierId() == liftRide.getSkierId() && getResortId() == liftRide.getResortId()
        && getSeasonId() == liftRide.getSeasonId() && getDayId() == liftRide.getDayId()
        && getTime() == liftRide.getTime() && getLiftId() == liftRide.getLiftId();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getSkierId(), getResortId(), getSeasonId(), getDayId(), getTime(),
        getLiftId());
  }

  public LiftRide() {
  }

  public int getSkierId() {
    return skierId;
  }

  public void setSkierId(int skierId) {
    this.skierId = skierId;
  }

  public int getResortId() {
    return resortId;
  }

  public void setResortId(int resortId) {
    this.resortId = resortId;
  }

  public int getSeasonId() {
    return seasonId;
  }

  public void setSeasonId(int seasonId) {
    this.seasonId = seasonId;
  }

  public int getDayId() {
    return dayId;
  }

  public void setDayId(int dayId) {
    this.dayId = dayId;
  }

  public int getTime() {
    return time;
  }

  public void setTime(int time) {
    this.time = time;
  }

  public int getLiftId() {
    return liftId;
  }

  public void setLiftId(int liftId) {
    this.liftId = liftId;
  }
}
