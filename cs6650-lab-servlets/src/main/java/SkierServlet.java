import com.google.gson.Gson;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkierServlet extends HttpServlet {

//  private Gson gson  = new Gson();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/plain");
    String urlPath = request.getPathInfo();

    //check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
//    for(int i =0; i < urlParts.length; ++i) {
//      System.out.println(urlParts[i]);
//    }
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isUrlValid(urlParts)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      response.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      // TODO: process url params in `urlParts`
      response.getWriter().write("It works!");
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
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
    for(int i =0; i < urlParts.length; ++i) {
      System.out.println(urlParts[i]);
    }

    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isUrlValid(urlParts)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      try {
        StringBuilder sb = new StringBuilder();
        String s;
        while ((s = request.getReader().readLine()) != null) {
          sb.append(s);
        }
        //TODO: json dependency - SkierReqBody skierReqBody = gson.fromJson(sb.toString(), SkierReqBody.class);
//        SkierReqBody skierReqBody = gson.fromJson(sb.toString(), SkierReqBody.class);
        //TODO: process skierReqBody
        response.setStatus(HttpServletResponse.SC_CREATED);
      } catch (Exception ex) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      }
    }
  }

  private boolean isUrlValid(String[] urlParts) {
    // TODO: validate the request url path according to the API spec
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    Integer resortID;
    Integer seasons;
    Integer day;
    Integer skierID;
    try {
      resortID = Integer.parseInt(urlParts[1]);
      seasons = Integer.parseInt(urlParts[3]);
      day = Integer.parseInt(urlParts[5]);
      skierID = Integer.parseInt(urlParts[7]);
    } catch (NumberFormatException e) {
      return false;
    }
    if (resortID < 1 || resortID > 10) {
      return false;
    }
    if (seasons != 2022) {
      return false;
    }
    if (day != 1) {
      return false;
    }
    if (skierID < 1 || skierID > 100000) {
      return false;
    }
    return true;
  }
}
