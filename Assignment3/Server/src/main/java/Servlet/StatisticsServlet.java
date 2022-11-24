package Servlet;

import Model.DummyObject;
import com.google.gson.Gson;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class StatisticsServlet extends HttpServlet {

  private Gson gson = new Gson();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    String urlPath = req.getPathInfo();

    // check we have a URL!
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
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("text/plain");
    res.sendError(HttpServletResponse.SC_BAD_REQUEST, "No post options");
    res.getWriter().write("No post options");
    return;
  }
}
