<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%
    // DB 연결 정보 설정
    String dbURL = "jdbc:mysql://localhost:3306/doglog?serverTimezone=Asia/Seoul";
    String dbID = "doglog";
    String dbPassword = "qwer1234";

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = DriverManager.getConnection(dbURL, dbID, dbPassword);

        // DOG 테이블에서 유기견 정보 조회
        String sql = "SELECT * FROM DOG ORDER BY id DESC";
        pstmt = conn.prepareStatement(sql);
        rs = pstmt.executeQuery();

        // JSON 배열 형태로 문자열 만들기 시작
        StringBuilder jsonResult = new StringBuilder();
        jsonResult.append("[");

        boolean isFirst = true;
        while (rs.next()) {
            if (!isFirst) {
                jsonResult.append(",");
            }
            jsonResult.append("{");
            jsonResult.append("\"name\":\"").append(rs.getString("name")).append("\",");
            jsonResult.append("\"breed\":\"").append(rs.getString("breed")).append("\",");
            jsonResult.append("\"age\":").append(rs.getInt("age")).append(",");
            jsonResult.append("\"status\":\"").append(rs.getString("status")).append("\"");
            jsonResult.append("}");
            isFirst = false;
        }
        jsonResult.append("]");

        // 안드로이드로 JSON 데이터 출력
        out.print(jsonResult.toString());

    } catch (Exception e) {
        e.printStackTrace();
        out.print("[]"); // 에러 시 빈 배열 반환
    } finally {
        if (rs != null) try { rs.close(); } catch(Exception e) {}
        if (pstmt != null) try { pstmt.close(); } catch(Exception e) {}
        if (conn != null) try { conn.close(); } catch(Exception e) {}
    }
%>