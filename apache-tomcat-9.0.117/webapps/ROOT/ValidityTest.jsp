<%@page language="java" contentType="text/html; charset=EUC-KR" pageEncoding="EUC-KR"%>
<%@page import="org.json.simple.*"%>
<%
    String id = request.getParameter("id");
    
    if(id != null && id.equals("mjc")) {
        JSONObject jObject = new JSONObject();
        jObject.put("password", "1234");
        jObject.put("id", id);
        
        out.print(jObject.toJSONString());
    }
%>