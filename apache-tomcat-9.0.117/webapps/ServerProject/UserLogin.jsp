<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%!
    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
%>
<%
    request.setCharacterEncoding("UTF-8");
    String userID = request.getParameter("userID");
    String userPassword = request.getParameter("userPassword");

    String dbURL = "jdbc:mysql://localhost:3306/doglog?serverTimezone=Asia/Seoul";
    String dbID = "doglog";
    String dbPW = "qwer1234";

    String responseJson = "{\"success\":false}";

    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(dbURL, dbID, dbPW);
        String sql = "SELECT userType FROM USER WHERE userID = ? AND userPassword = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userID);
        pstmt.setString(2, userPassword);

        ResultSet rs = pstmt.executeQuery();
        if(rs.next()) {
            responseJson = "{\"success\":true,\"userType\":\"" + jsonEscape(rs.getString("userType")) + "\"}";
        } else {
            responseJson = "{\"success\":false}";
        }
    } catch(Exception e) {
        e.printStackTrace();
        responseJson = "{\"success\":false}";
    }
    out.print(responseJson);
%>
