import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import model.SkiersFactory;

public class Consumer extends Thread {
  private final ApiClient apiClient;
  private final SkiersApi skierApi;
  private static final String BASE_PATH = "http://localhost:8080/cs6650_lab_servlets_war_exploded";
//  private static final String BASE_PATH = "http://52.43.243.190:8080/cs6650_lab_servlets_war_exploded";
  private final BlockingQueue<SkiersFactory> dataBuffer;
  private final Integer numRequests;
  private final AtomicInteger successful;
  private final AtomicInteger unsuccessful;
  private final CountDownLatch phaseFinish;

  public Consumer (BlockingQueue<SkiersFactory> dataBuffer, Integer numRequests, AtomicInteger successful,
      AtomicInteger unsuccessful, CountDownLatch phaseFinish) {
    this.dataBuffer = dataBuffer;
    this.numRequests = numRequests;
    this.successful = successful;
    this.unsuccessful = unsuccessful;
    this.phaseFinish = phaseFinish;
    this.apiClient = new ApiClient();
    apiClient.setBasePath(BASE_PATH);
    this.skierApi = new SkiersApi(apiClient);
  }

  @Override
  public void run() {
    for (int i = 0; i < this.numRequests; ++i) {
      try {
        SkiersFactory skier = this.dataBuffer.take();
        this.post(skier);
      } catch (InterruptedException | ApiException e) {
        throw new RuntimeException(e);
      }
      phaseFinish.countDown();
    }
  }

  private void post(SkiersFactory skier) throws ApiException {
    int retryTime = 0;
    while (retryTime < 5) {
//      System.out.println("skier.getLiftRide is posting...\n" + skier.getLiftRide());//looks like only one
      try {
        ApiResponse<Void> response = skierApi.writeNewLiftRideWithHttpInfo(skier.getLiftRide(),
          skier.getResortID(), skier.getSeasonID(), skier.getDayID(), skier.getSkierID());

        if (response.getStatusCode() == 201) {
          this.successful.getAndIncrement();
          break;
        } else {
          retryTime++;
        }

      } catch (ApiException e){
        e.printStackTrace();
      }


    }
    if (retryTime == 5) {
      this.unsuccessful.getAndIncrement();
    }
  }
}
