package main.java.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import main.java.bean.FeatureRequestOL;
import main.java.core.DataParser;
import main.java.core.RequestAnalyzer;
import main.java.parse.Parser;

/**
 * Created by zzyo on 2017/3/16.
 */
@WebServlet(name = "indexServlet", urlPatterns = "/")
public class indexServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	ServletContext context = this.getServletContext();
    	String path = context.getRealPath("/");
    	String name = request.getParameter("name");
        String FRTitle = request.getParameter("FRTitle");
        String FRDes = request.getParameter("FRDes");
        
        Parser parser = new Parser();
        //parser.parseCode(FRDes);
        System.out.println(name + "\n\n\n\n\n" +
                FRTitle + "\n\n\n\n\n" +
                FRDes
        );
        
        
        FeatureRequestOL fr = parser.getFR(name, FRTitle, FRDes);
        String result = parser.printResult(fr);
        System.out.print(result);
        
        
		/*System.out.printf("source code located in : %s\n",System.getProperty("user.dir"));
		System.out.printf("servlet code located in : %s\n",path);
		DataParser dataParser = new DataParser(path);
        FeatureRequestOL loadedFR = dataParser.constructSFeatureRequestOL(fr);
        
        System.out.println("==============Start Loading================");
        System.out.println(loadedFR);
        System.out.println("===============END Loading=================");
        
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < loadedFR.getNumSentences(); i++){
        	int predict = RequestAnalyzer.predictTagIndex(fr, i);
        	String tag = RequestAnalyzer.tagNames[predict];
        	buffer.append("["+tag+"]\n"+loadedFR.getFullSentence(i)+"\n");
        }*/
        
        
        
        //request.setAttribute("parseResult", result);
        //RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/jsp/show.jsp");
        //rd.forward(request, response);
        PrintWriter printWriter = response.getWriter();
        printWriter.print(result/*+"\n====================================\n"+buffer*/);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/jsp/index.jsp");
        rd.forward(request, response);
    }
}
