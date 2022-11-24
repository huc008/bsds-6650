package Part2;

import static Part2.Config.MILLS_TO_SEC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PerformanceAnalyzer {

    private final double mean;
    private final double median;
    private final int throughput;
    private final int maxResponse;
    private final int minResponse;
    private final int the99thPercentile;
    private final List<Integer> responseTimes;

  public PerformanceAnalyzer(PhaseStatus status, long wallTime) {
    List<String> res = status.getFileLines();
    responseTimes = new ArrayList<>();
    calcRes(res);
    this.mean = calcMean(responseTimes);
    this.median = calcMedian(responseTimes);
    this.throughput =  (int)(status.getSuccess() / (wallTime/MILLS_TO_SEC));
    this.minResponse = responseTimes.get(0);
    this.maxResponse = responseTimes.get(responseTimes.size() - 1);
    this.the99thPercentile = responseTimes.get((int) Math.ceil(responseTimes.size() * 0.99) - 1);
    }

  private void calcRes(List<String> res) {
    for (String record : res ) {
      String[] r = record.split(",");
      int time = Integer.parseInt(r[2]);
      responseTimes.add(time);
    }
  }

  private double calcMean(List<Integer> responseTimes) {
      double sum = 0;
      for (int response : responseTimes) {
        sum += response;
      }
      return sum / responseTimes.size();
    }

    private double calcMedian(List<Integer> res) {
      Collections.sort(res);
      double median;
      int size = res.size();
      if (size % 2 == 0) {
        int mid = res.get(size / 2) + res.get(size / 2 - 1);
        median = ((double) mid) / 2;
      } else {
        median = (double) res.get(size / 2);
      }
      return median;
    }

    public double getMean() {
      return mean;
    }

    public int getMaxResponse() {
      return maxResponse;
    }

    public int getMinResponse() {
      return minResponse;
    }

    public double getMedian() {
      return median;
    }

    public int getThroughput() {
      return throughput;
    }

    public int get99thPercentile() {
      return the99thPercentile;
    }
}
