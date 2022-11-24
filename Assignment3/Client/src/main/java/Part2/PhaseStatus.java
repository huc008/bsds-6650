package Part2;
import java.util.List;
import java.util.ArrayList;


public class PhaseStatus {
  private int success;
  private int fail;
  private  List<String> data;

  public PhaseStatus() {
    this.success = 0;
    this.fail = 0;
    this.data = new ArrayList<>();
  }

  public synchronized void addSuccess(int add) {
    this.success += add;
  }

  public synchronized void addFail(int add) {
    this.fail += add;
  }

  public synchronized void addData(List<String> res) {
    this.data.addAll(res);
  }

  public int getSuccess() {
    return this.success;
  }

  public int getFail() {
    return this.fail;
  }

  public List<String> getFileLines() { return this.data; }
}