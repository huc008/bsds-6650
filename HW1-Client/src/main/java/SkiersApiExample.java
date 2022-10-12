import io.swagger.client.*;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.*;
import io.swagger.client.api.ResortsApi;
import java.util.Random;

public class SkiersApiExample {
  private static final String BASE_PATH = "http://localhost:8080/cs6650_lab_servlets_war_exploded";

  public static void main(String[] args) {
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(BASE_PATH);

    SkiersApi apiInstance = new SkiersApi(apiClient);
    Random random = new Random();
    Integer resortID = random.nextInt(10) + 1; // Range from 1 to 10 | ID of the resort of interest
    Integer skierID = random.nextInt(100000) + 1; // Range from 1 to 100,000 | ID of the skier of interest
    try {
      LiftRide body = new LiftRide();
      body.setLiftID(random.nextInt(40) + 1);
      body.setTime(random.nextInt(360) + 1);
      apiInstance.writeNewLiftRide(body, resortID, "2022", "1", skierID);
    } catch (ApiException e) {
      System.err.println("Exception when calling ResortsApi#addSeason");
      e.printStackTrace();
    }
  }
}