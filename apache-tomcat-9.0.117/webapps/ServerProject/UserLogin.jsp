<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%@ page import="org.json.JSONObject" %>
<%
    request.setCharacterEncoding("UTF-8");
    String userID = request.getParameter("userID");
    String userPassword = request.getParameter("userPassword");

    String dbURL = "jdbc:mysql://localhost:3306/DogAppDB?serverTimezone=UTC";
    String dbID = "doglog";
    String dbPW = "qwer1234";

    JSONObject json = new JSONObject();

    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(dbURL, dbID, dbPW);
        String sql = "SELECT userType FROM USER WHERE userID = ? AND userPassword = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userID);
        pstmt.setString(2, userPassword);

        ResultSet rs = pstmt.executeQuery();
        if(rs.next()) {
            json.put("success", true);
            json.put("userType", rs.getString("userType"));
        } else {
            json.put("success", false);
        }
    } catch(Exception e) {
        e.printStackTrace();
        json.put("success", false);
    }
    out.print(json.toString());
%>