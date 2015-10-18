

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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
import twitter4j.TwitterFactory;

/**
 * Servlet implementation class AddCollegeUser
 */
@WebServlet("/AddCollegeUser")
public class AddCollegeUser extends HttpServlet {
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
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddCollegeUser() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        // TODO Auto-generated method stub
        super.init(config);
        servletContext = config.getServletContext();
        rootPath = servletContext.getRealPath("/");
        liwcHandler = new LIWCHandler(rootPath);
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		// Working with database
	    // Register JDBC driver
        try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection(DB_URL,USER,PASS);
			
	         Statement stmt = conn.createStatement();
	         String sql;
	         sql = "SELECT username FROM CollegeUsers";
	         ResultSet rs = stmt.executeQuery(sql);
	         
	         // Check if user already exists
	         boolean found = false;
	         while(rs.next()) {
	        	 String username = rs.getString("username");
	        	 if (username.equals(request.getParameter("username"))) {
	        		 System.out.println("Same user already present.");
	        		 found = true;
	        		 break;
	        	 }
	         }
	         // Insert if user already does not exist
	         if (!found) {
	        	 Statement insert_statement = conn.createStatement();
	        	 String insert_query = "INSERT INTO CollegeUsers (college_name, username) VALUES ('" + request.getParameter("college_name") + "', '" + request.getParameter("username") + "');";
	        	 System.out.println(insert_query);
	        	 int status = insert_statement.executeUpdate(insert_query);
	        	 if (status > 0)
	        		 System.out.println("Inserted!");
	        	 else
	        		 System.out.println("Error!");
	         }
	         
	         // For all users, compute aggregate scores
	         String sql2 = "SELECT username FROM CollegeUsers WHERE college_name = '" + request.getParameter("college_name") + "';";
	         rs = stmt.executeQuery(sql2);
	         Twitter twitter = (Twitter) request.getSession().getAttribute("twitter");
	         String final_categories = "";
	         if (twitter != null) {
	        	 String finalData = "";
		         while(rs.next()) {
		        	 String username = rs.getString("username");
		        	List<Status> statuses = twitter.getUserTimeline();
		 			statuses.clear();
		 		    for (int i = 1;;i++) {
		 			    Paging page = new Paging (i, 200);
		 			    List<Status> temp = twitter.getUserTimeline(username, page);
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
		         }
		         
		         String sql3 = "DELETE FROM CollegeScores WHERE college_name = '" + request.getParameter("college_name") + "';";
		         int status1 = stmt.executeUpdate(sql3);
		         if (status1 > 0)
	        		 System.out.println("Deleted!");
	        	 else
	        		 System.out.println("Error!");

		         HashMap<String, Integer> hash = liwcHandler.tagString(finalData);
		         double[] correlations = liwcHandler.calculateCorrelations(hash);
		         
		         Statement insert_statement = conn.createStatement();
	        	 String insert_query = "INSERT INTO CollegeScores (college_name, extro, agree, consc, neuro, open) VALUES ('" + request.getParameter("college_name") + "', '" + correlations[0] + "', '" + correlations[1] + "', '" + correlations[2] + "', '" + correlations[3] + "', '" + correlations[4] + "');";
	        	 System.out.println(insert_query);
	        	 int status = insert_statement.executeUpdate(insert_query);
	        	 if (status > 0)
	        		 System.out.println("Inserted college score!");
	        	 else
	        		 System.out.println("Error!");
		         
	        	 final_categories = hash.toString();
	         } else {
	        	 System.out.println("Please login with twitter first.");
	         }
	         // Clean-up environment
	         rs.close();
	         stmt.close();
	         conn.close();
	         request.setAttribute("final_categories", final_categories);
			 request.getRequestDispatcher("ViewCollegePersonality.jsp").forward(request, response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}

}
