package servlets;
import db.DBConnection; import javax.servlet.*; import javax.servlet.annotation.WebServlet; import javax.servlet.http.*; import java.io.*; import java.sql.*; import java.util.*;
@WebServlet("/profile") public class ProfileServlet extends HttpServlet {
 protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
  HttpSession session=req.getSession(false); Integer uid=session==null?null:(Integer)session.getAttribute("userId"); if(uid==null){resp.sendRedirect("login.jsp");return;}
  try(Connection c=DBConnection.getConnection()){
   try(PreparedStatement p=c.prepareStatement("SELECT u.First_Name,u.Last_Name,u.Email,s.Major,s.Grad_Year,s.Bio FROM Users u JOIN Students s ON u.User_ID=s.User_ID WHERE u.User_ID=?")){p.setInt(1,uid);try(ResultSet r=p.executeQuery()){if(r.next()){for(String k:new String[]{"First_Name","Last_Name","Email","Major","Grad_Year","Bio"})req.setAttribute(k,r.getObject(k));}}}
   List<Map<String,Object>> skills=new ArrayList<>(); try(PreparedStatement p=c.prepareStatement("SELECT sk.Skill_ID,sk.Name, hs.User_ID IS NOT NULL selected FROM Skills sk LEFT JOIN HasSkill hs ON hs.Skill_ID=sk.Skill_ID AND hs.User_ID=? ORDER BY sk.Name")){p.setInt(1,uid);try(ResultSet r=p.executeQuery()){while(r.next()){Map<String,Object>m=new HashMap<>();m.put("id",r.getInt(1));m.put("name",r.getString(2));m.put("selected",r.getBoolean(3));skills.add(m);}}} req.setAttribute("skills",skills);
  }catch(SQLException e){throw new ServletException(e);} req.getRequestDispatcher("profile.jsp").forward(req,resp);
 }
 protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException,ServletException{
  HttpSession s=req.getSession(false); if(s==null||s.getAttribute("userId")==null){resp.sendRedirect("login.jsp");return;} int uid=(Integer)s.getAttribute("userId");
  String first=trim(req.getParameter("firstName")),last=trim(req.getParameter("lastName")),major=trim(req.getParameter("major")),bio=trim(req.getParameter("bio")); int year=0; try{year=Integer.parseInt(req.getParameter("gradYear"));}catch(Exception ignored){}
  if(first.isEmpty()||last.isEmpty()||bio.length()>500||year<2025||year>2035){s.setAttribute("flashError","Please check your profile fields.");resp.sendRedirect("profile");return;}
  String[] skillIds=req.getParameterValues("skills"); try(Connection c=DBConnection.getConnection()){c.setAutoCommit(false);try{
   try(PreparedStatement p=c.prepareStatement("UPDATE Users SET First_Name=?,Last_Name=? WHERE User_ID=?")){p.setString(1,first);p.setString(2,last);p.setInt(3,uid);p.executeUpdate();}
   try(PreparedStatement p=c.prepareStatement("UPDATE Students SET Major=?,Grad_Year=?,Bio=? WHERE User_ID=?")){p.setString(1,major);p.setInt(2,year);p.setString(3,bio);p.setInt(4,uid);p.executeUpdate();}
   try(PreparedStatement p=c.prepareStatement("DELETE FROM HasSkill WHERE User_ID=?")){p.setInt(1,uid);p.executeUpdate();}
   if(skillIds!=null)try(PreparedStatement p=c.prepareStatement("INSERT INTO HasSkill(User_ID,Skill_ID) VALUES(?,?)")){for(String id:skillIds){p.setInt(1,uid);p.setInt(2,Integer.parseInt(id));p.addBatch();}p.executeBatch();}
   c.commit();s.setAttribute("firstName",first);s.setAttribute("flashSuccess","Profile updated successfully.");
  }catch(Exception e){c.rollback();throw e;}}catch(Exception e){throw new ServletException(e);}resp.sendRedirect("profile");
 }
 private String trim(String x){return x==null?"":x.trim();}
}
