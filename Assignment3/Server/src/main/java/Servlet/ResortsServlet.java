package Servlet;

import Model.DummyObject;
import com.google.gson.Gson;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class ResortsServlet extends HttpServlet {
  private Gson gson = new Gson();
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    String urlPath = req.getPathInfo();

    if (urlPath == null || urlPath.isEmpty()) {
//      res.setContentType("text/plain");
//      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
//      res.getWriter().write("Please provide url");
//      return;
      res.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      DummyObject dummyObject = new DummyObject(req.getRequestURI());
      String objJsonString = this.gson.toJson(dummyObject);

      PrintWriter out = res.getWriter();
      res.setContentType("application/json");
      res.setCharacterEncoding("UTF-8");
      out.print(objJsonString);
      out.flush();

    }

    String[] urlParts = urlPath.substring(1).split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (isUrlValidGet(urlParts) != 0) {
      res.setContentType("text/plain");
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid format: "+ urlPath + "Reason: "+ isUrlValidGet(urlParts));
      res.getWriter().write("Please provide follow the correct format");
      return;
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      DummyObject dummyObject = new DummyObject(req.getRequestURI());
      String objJsonString = this.gson.toJson(dummyObject);

      PrintWriter out = res.getWriter();
      res.setContentType("application/json");
      res.setCharacterEncoding("UTF-8");
      out.print(objJsonString);
      out.flush();

    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    String urlPath = req.getPathInfo();
    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setContentType("text/plain");
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
      res.getWriter().write("Please provide url");
      return;
    }
    String[] urlParts = urlPath.substring(1).split("/");
    if (isUrlValidPost(urlParts) != 0) {
      res.setContentType("text/plain");
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid format: "+ urlPath + "Reason: " + isUrlValidPost(urlParts) );
      res.getWriter().write("Please provide follow the correct format");
    }  else {
      res.setStatus(HttpServletResponse.SC_CREATED);
      // do any sophisticated processing with urlParts which contains all the url params
      DummyObject dummyObject = new DummyObject("Name");
      String objJsonString = this.gson.toJson(dummyObject);

      PrintWriter out = res.getWriter();
      res.setContentType("application/json");
      res.setCharacterEncoding("UTF-8");
      out.print(objJsonString);
      out.flush();
    }
  }

  private int isUrlValidPost(String[] urlPath) {
    /**
     * POST /resorts/{resortID}/seasons
     */
    try{
      if(urlPath.length == 2){
        Long resortID = Long.valueOf(urlPath[0]);
        if(urlPath[1].equals("seasons")){
          return 0;
        }
      }
    }
    catch (NumberFormatException e){
      System.out.println(e.getMessage());
      return -2;
    }
    return -1;
  }

  private int isUrlValidGet(String[] urlPath) {
    /**
     * GET /resorts
     * GET /resorts/{resortID}/seasons/{seasonID}/day/{dayID}/skiers
     * GET /resorts/{resortID}/seasons
     */
    // TODO: validate the request url path according to the API spec
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    try{
      if(urlPath.length == 6){
        Long resortID = Long.valueOf(urlPath[0]);
        Long seasonID = Long.valueOf(urlPath[2]);
        Long dayID = Long.valueOf(urlPath[4]);
        if(urlPath[1].equals("seasons") && urlPath[3].equals("day")&&urlPath[5].equals("skiers") ){
          return 0;
        }
      }
      else if(urlPath.length == 2){
        Long resortID = Long.valueOf(urlPath[0]);
        if(urlPath[1].equals("seasons")){
          return 0;
        }
      }
    }
    catch (NumberFormatException e){
      System.out.println(e.getMessage());
      return -2;
    }
    return -1;
  }
}
