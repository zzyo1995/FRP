package main.java.servlet;

import main.java.parse.Parser;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by zzyo on 2017/3/16.
 */
@WebServlet(name = "indexServlet", urlPatterns = "/")
public class indexServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        String FRTitle = request.getParameter("FRTitle");
        String FRDes = request.getParameter("FRDes");
        Parser parser = new Parser();
        //parser.parseCode(FRDes);
        System.out.println(name + "\n\n\n\n\n" +
                FRTitle + "\n\n\n\n\n" +
                FRDes
        );
        String raw = FRDes;
        String result = parser.parseExe(raw);
        System.out.println(result);
        request.setAttribute("parseResult", result);
        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/jsp/show.jsp");
        rd.forward(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/jsp/index.jsp");
        rd.forward(request, response);
    }
}
