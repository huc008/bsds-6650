import java.util.concurrent.BlockingQueue;
import model.SkiersFactory;

public class Producer extends Thread {
  private final BlockingQueue<SkiersFactory> dataBuffer;
  private final Integer numPosts;

  public Producer (BlockingQueue dataBuffer, Integer numPosts) {
    this.dataBuffer = dataBuffer;
    this.numPosts = numPosts;
  }

  private void generateSkiers() throws InterruptedException {
    SkiersFactory skiersGenerated = new SkiersFactory();
    dataBuffer.put(skiersGenerated);
  }

  @Override
  public void run() {
    try {
      for (int i = 0; i < this.numPosts; i++) {
        generateSkiers(); //assure enough skiers are generated
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }


}
