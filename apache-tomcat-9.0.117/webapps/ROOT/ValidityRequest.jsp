<%@page import="java.sql.*"%>
<%@page import="org.json.simple.*"%>
<%@page language="java" contentType="text/html; charset=EUC-KR" pageEncoding="EUC-KR"%>
<%
    String pr_id = request.getParameter("userID");
    String userID = null;
    Connection conn = null;
    PreparedStatement prepared_stat = null;
    ResultSet rs = null;

    // 도커로 띄운 MySQL 서버 접속 정보 (로컬)
    String url = "jdbc:mysql://localhost:3306/db_android";
    String user = "user123";
    String password = "qwer1234";

    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = DriverManager.getConnection(url, user, password);

        String sql = "SELECT userID FROM MEMBER WHERE userID = ?";
        prepared_stat = conn.prepareStatement(sql);
        prepared_stat.setString(1, pr_id);

        rs = prepared_stat.executeQuery();

        while(rs.next()) {
            userID = rs.getString("userID");
        }

        JSONObject json = new JSONObject();
        json.put("userID", userID);

        // 아이디가 없으면(null) 가입 가능한 새 아이디이므로 true 반환
        if (userID == null) {
            json.put("newID", true);
        } else {
            json.put("newID", false);
        }

        out.println(json.toString());

    } catch (SQLException ex) {
        out.println("SQLException:" + ex.getMessage());
    } finally {
        if (rs != null) rs.close();
        if (prepared_stat != null) prepared_stat.close();
        if (conn != null) conn.close();
    }
%>