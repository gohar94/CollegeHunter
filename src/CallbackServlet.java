

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.sql.*;

/**
 * Servlet implementation class CallbackServlet
 */
@WebServlet("/CallbackServlet")
public class CallbackServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	// JDBC driver name and database URL
    public static final String JDBC_DRIVER="com.mysql.jdbc.Driver";  
    public static final String DB_URL="jdbc:mysql://localhost/CollegeHunter";
    
    //  Database credentials
    public static final String USER = "root";
    public static final String PASS = "root";

 	private static ServletContext servletContext;
 	private static String rootPath;
 	
 	private static LIWCHandler liwcHandler;
 	
    @Override
    public void init(ServletConfig config) throws ServletException {
        // TODO Auto-generated method stub
        super.init(config);
        servletContext = config.getServletContext();
        rootPath = servletContext.getRealPath("/");
        liwcHandler = new LIWCHandler(rootPath);
    }

    /**
     * @see HttpServlet#HttpServlet()
     */
    public CallbackServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Twitter twitter = (Twitter) request.getSession().getAttribute("twitter");
		RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
		String verifier = request.getParameter("oauth_verifier");
		try {
			AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
			//persist to the accessToken for future reference.
		    storeAccessToken(twitter.verifyCredentials().getScreenName() , accessToken);
//		    request.getSession().removeAttribute("requestToken");
		    System.out.println("Here");
		    List<Status> statuses = twitter.getUserTimeline();
			statuses.clear();
		    String finalData = "";
		    for (int i = 1;;i++) {
			    Paging page = new Paging (i, 200);
			    List<Status> temp = twitter.getUserTimeline(twitter.getScreenName(), page);
			    System.out.println("temp size is " + temp.size());
			    System.out.println(i);
			    for (Status status : temp) {
			    	finalData+=status.getText();
			    }
			    if (temp.size() == 0)
			    	break;
			    else
			    	statuses.addAll(0, temp);
		    }
		    
		    HashMap<String,Integer> hash = liwcHandler.tagString(finalData);
		    String final_categories = hash.toString();
		    double[] correlations = liwcHandler.calculateCorrelations(hash);
		    
		    Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection(DB_URL,USER,PASS);
			
	         Statement stmt = conn.createStatement();
	         String sql;
	         sql = "SELECT * FROM CollegeScores";
	         ResultSet rs = stmt.executeQuery(sql);
	         
	         HashMap<String,Double> college_dist = new HashMap<String,Double>();
	         
	         while(rs.next()) {
	        	 String college_name = rs.getString("college_name");
	        	 double[] college_correlations = {0,0,0,0,0};
	        	 college_correlations[0] = rs.getDouble("extro");
	        	 college_correlations[1] = rs.getDouble("agree");
	        	 college_correlations[2] = rs.getDouble("consc");
	        	 college_correlations[3] = rs.getDouble("neuro");
	        	 college_correlations[4] = rs.getDouble("open");
	        	 double distance = calculateDistance(college_correlations, correlations);
	        	 college_dist.put(college_name, distance);
	         }
		    
		    request.setAttribute("correlations", correlations);
		    request.setAttribute("distances", college_dist.toString());
		    request.setAttribute("final_categories", final_categories);
		    request.setAttribute("user_tweets", statuses);
			request.getRequestDispatcher("index.jsp").forward(request, response);
		} catch (TwitterException e) {
		    throw new ServletException(e);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
	private static void storeAccessToken(String username, AccessToken accessToken) {
		// TODO: Store this in rdbms or some persistence layer
		String token = accessToken.getToken();
	    String secret = accessToken.getTokenSecret();
	}
	
	public static double calculateDistance(double[] array1, double[] array2) {
        double sum = 0.0;
        for(int i=0;i<array1.length;i++) {
           sum = sum + Math.pow((array1[i]-array2[i]),2.0);
        }
        return Math.sqrt(sum);
    }

}
