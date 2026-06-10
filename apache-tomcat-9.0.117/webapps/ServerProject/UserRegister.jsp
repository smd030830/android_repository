<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%
    request.setCharacterEncoding("UTF-8");
    String userID = request.getParameter("userID");
    String userPassword = request.getParameter("userPassword");
    String userEmail = request.getParameter("userEmail");
    String userGender = request.getParameter("userGender");
    String userType = request.getParameter("userType");

    String dbURL = "jdbc:mysql://localhost:3306/doglog?serverTimezone=Asia/Seoul";
    String dbID = "doglog";
    String dbPW = "qwer1234";

    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(dbURL, dbID, dbPW);
        String sql = "INSERT INTO USER VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userID);
        pstmt.setString(2, userPassword);
        pstmt.setString(3, userEmail);
        pstmt.setString(4, userGender);
        pstmt.setString(5, userType);

        int result = pstmt.executeUpdate();
        if(result > 0) {
            out.print("success");
        } else {
            out.print("fail");
        }
    } catch(Exception e) {
        e.printStackTrace();
        out.print("error");
    }
%>
