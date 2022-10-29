import com.google.gson.Gson;
import java.util.concurrent.TimeoutException;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import model.LiftRide;
import model.Skier;
import rabbitMQ.Publisher;

@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkierServlet extends HttpServlet {
  private final Gson gson  = new Gson();
  private static Publisher publisher;

  public void init() {
    try {
      publisher = new Publisher();
    } catch (IOException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("text/plain");
    String urlPath = request.getPathInfo();

    //check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing parameters");
      return;
    }
    String[] urlParts = urlPath.split("/");
    if (!isUrlValid(urlParts)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("Bad Request, please checking URL...");
    } else {
      response.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      // TODO: process url params in `urlParts`
      response.getWriter().write("It works!");
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    String urlPath = request.getPathInfo();

    //check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing parameters");
      return;
    }
    String[] urlParts = urlPath.split("/");
    if (!isUrlValid(urlParts)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getOutputStream().println("URL is not valid, return 404...");
    } else {
      try {
        StringBuilder sb = new StringBuilder();
        String s;
        while ((s = request.getReader().readLine()) != null) {
          sb.append(s);
        }
        LiftRide liftRide = gson.fromJson(sb.toString(), LiftRide.class);
        Integer skierId = Integer.parseInt(urlParts[7]);
        if (isBodyValid(liftRide)) {
          System.out.println("received a valid request!");
//          System.out.println("body is valid!");//TODO：why after comment still get this message
          response.setStatus(HttpServletResponse.SC_CREATED);
          response.getWriter().write(gson.toJson(liftRide));
          response.getWriter().write("It posts!");
          //HW2 - format the incoming data and send it as a payload to a remote queue and then return success to the client
          publisher.sendRMQ(new Skier(skierId, liftRide));
        } else {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write("body is not valid");
        }
      }
      catch (Exception ex) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("exception break");
      }
    }
  }

  private boolean isBodyValid (LiftRide liftRide) {
    int liftID = liftRide.getLiftID();
    int time = liftRide.getTime();
    return liftID >=1 && liftID <=40
        && time >=1 && time <=360;
  }

  private boolean isUrlValid(String[] urlParts) {
    // TODO: validate the request url path according to the API spec
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    int resortID;
    int seasons;
    int day;
    int skierID;
    try {
      resortID = Integer.parseInt(urlParts[1]);
      seasons = Integer.parseInt(urlParts[3]);
      day = Integer.parseInt(urlParts[5]);
      skierID = Integer.parseInt(urlParts[7]);
    } catch (NumberFormatException e) {
      return false;
    }
    return resortID >= 1 && resortID <= 10
        && seasons == 2022 && day == 1
        && skierID >= 1 && skierID <= 100000;
  }
}
