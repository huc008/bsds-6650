package Part1;
import java.util.List;
import java.util.ArrayList;


public class PhaseStatus {
  private int success;
  private int fail;

  public PhaseStatus() {
    this.success = 0;
    this.fail = 0;
  }

  public synchronized void addSuccess(int add) {
    this.success += add;
  }

  public synchronized void addFail(int add) {
    this.fail += add;
  }


  public int getSuccess() {
    return this.success;
  }

  public int getFail() {
    return this.fail;
  }

}