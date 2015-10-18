<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="twitter4j.Status" %>
<%@ page import="java.util.List" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
	<head>
	    <meta content="text/html; charset=utf-8" http-equiv="Content-Type"/>
	    <title>CollegeHunter</title>
	</head>
	<body>
		<% if (request.getSession().getAttribute("twitter") != null) { %>
			<h1>Welcome ${twitter.screenName} (${twitter.id})</h1>
			<form action="./PostServlet" method="post">
			    <textarea cols="80" rows="1" name="text"></textarea>
			    <input type="submit" name="post" value="update"/>
			</form>
			<% List<Status> statuses = (List<Status>) request.getAttribute("user_tweets"); %>
			<% for (Status status : statuses) { %>
				<%= "@" + status.getUser().getScreenName() + " - " + status.getText() %>
				<br>
			<% } %>
			<br>
			<%= request.getAttribute("final_categories") %>
			<% double[] correlations = (double[]) request.getAttribute("correlations"); %>
			<% for (double d : correlations) { %>
				<%= d %>
			<% } %>
			<%= request.getAttribute("distances") %>
			<a href="./LogoutServlet">logout</a>
		<% } else { %>
			<a href="SigninServlet"><img src="./images/Sign-in-with-Twitter-darker.png"/></a>
		<% } %>

	</body>
</html>