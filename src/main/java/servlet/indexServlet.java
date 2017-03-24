package main.java.servlet;

import edu.stanford.nlp.io.EncodingPrintWriter;
import main.java.bean.FR;
import main.java.parse.Parser;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

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
        FR fr = parser.getFR(name, FRTitle, FRDes);
        String result = parser.printResult(fr);
        //request.setAttribute("parseResult", result);
        //RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/jsp/show.jsp");
        //rd.forward(request, response);
        PrintWriter printWriter = response.getWriter();
        printWriter.print(result);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/jsp/index.jsp");
        rd.forward(request, response);
    }
}
