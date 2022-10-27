import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import java.util.Random;
import model.SkiersFactory;

public class SingleThreadPost {

  public static void main(String[] args) throws ApiException {
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath("http://localhost:8080/cs6650_lab_servlets_war_exploded");
    SkiersApi skiersApi = new SkiersApi(apiClient);

    SkiersFactory skier = new SkiersFactory();
    ApiResponse<Void> response = skiersApi.writeNewLiftRideWithHttpInfo(skier.getLiftRide(),
        skier.getResortID(), skier.getSeasonID(), skier.getDayID(), skier.getSkierID());
    System.out.println("response code: " + response.getStatusCode());
  }
}
