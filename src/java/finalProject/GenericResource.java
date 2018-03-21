/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finalProject;
import com.sun.mail.smtp.SMTPTransport;
import java.security.Security;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import static javax.swing.UIManager.getString;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * REST Web Service
 *
 * @author 
 */
@Path("generic")
public class GenericResource {
    static final String JDBC_DRIVER = "oracle.jdbc.OracleDriver";
    static final String DB_URL = "jdbc:oracle:thin:@144.217.163.57:1521:XE";
    static final String USER = "mad303p1";
    static final String PASS = "mad303p1pw";
    JSONObject main =  new JSONObject();
    JSONArray mainArray =  new JSONArray();
    JSONObject singleObj = new JSONObject();
    long unixTimeStamp = System.currentTimeMillis() / 1000L;
    Connection conn = null;
    Statement stmt = null;
    PreparedStatement pstmt = null;
    String sql;
    int maxid;
    boolean empty = true;
    public Connection getCon() {
        Connection conn = null;
        try {
            //Register JDBC Driver
            Class.forName(JDBC_DRIVER);            
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

  
    @Context
    private UriInfo context;

    
    public GenericResource() {
        
    }
  
    @GET
    @Path("signup&{user_name}&{email}&{password}&{dob}&{active_user}")
    @Produces("application/json")
    public String signup(@PathParam("user_name") String name,
            @PathParam("email") String email,
            @PathParam("password") String password,
            @PathParam("dob") String dob,
            @PathParam("active_user") int activeuser) throws ParseException {    
        java.text.SimpleDateFormat sm = new SimpleDateFormat("dd/MM/YYYY");
        java.util.Date d = sm.parse(dob);
        java.sql.Date d1= new java.sql.Date(d.getTime());
        
        conn = getCon();
        int count = 0;
        int mid = 0;
        try {
            sql = "select count(*) from users where email='" + email + "'";
            if(stmt == null)
               stmt =  conn.createStatement();
            
            ResultSet rs=stmt.executeQuery(sql);
            rs.next();
            count = rs.getInt(1);            
            if(count != 0) {       
            singleObj.accumulate("Status", "warning");
            singleObj.accumulate("Timestamp",unixTimeStamp );
            singleObj.accumulate("Message", "You are already registered");
            } else{
                sql = "INSERT into users values (users_sequence.nextval,?,?,?,?,?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, name);
                pstmt.setString(2,email);
                pstmt.setString(3, password);
                pstmt.setDate (4, d1);
                pstmt.setInt(5,activeuser);
                    if (pstmt.executeUpdate() == 1) {
                        sql = "select max(userid) from users";
                   
                    while (rs.next()) {
                        mid = rs.getInt(1);
                    }
                     singleObj.accumulate("Status", "OK");
                     singleObj.accumulate("Timestamp",unixTimeStamp);
                     singleObj.accumulate("Message", "successfull Added");
                     singleObj.accumulate("Userid", mid+"");
                }
                pstmt.close();
              }
            conn.close();

        }    catch(Exception e)
        {
            e.printStackTrace();
            
                 singleObj.accumulate("Status", "ERROR");
                     singleObj.accumulate("Timestamp",unixTimeStamp);
                     singleObj.accumulate("Message", "Database Connection Failed");     
        }
        
          
        String result =  singleObj.toString();
    return result;
    }
    
     @GET
    @Path("fav&{userid}&{wordid}")
    @Produces("application/json")
    public String favourites(@PathParam("userid") int userid,@PathParam("wordid") int wordid) throws SQLException   {
           
        // System.out.println(userid+" dvdv "+wordid);
       try 
        {
             conn = getCon();
            String sql;
            sql = "select f.user_id,f.word_id,w.word_name from words w,favourites f where f.word_id=w.word_id and f.user_id = ? and f.word_id=?";
            PreparedStatement pstmt  =  conn.prepareStatement(sql);
           
            pstmt.setInt(1, userid);
            pstmt.setInt(2, wordid);
           ResultSet rs = pstmt.executeQuery();
            
      while(rs.next())
            {
                int uid=rs.getInt("user_id");
                int wid=rs.getInt("word_id");
                String wname=rs.getString("word_name");
                singleObj.accumulate("Status","OK");
                singleObj.accumulate("Timestamp",unixTimeStamp);
                singleObj.accumulate("user_id",uid);
                singleObj.accumulate("word_id", wid);
                singleObj.accumulate("word_name", wname); 
                empty=false;
            }
            
       
        if(empty){
                      singleObj.accumulate("Status", "warning");
                     singleObj.accumulate("Timestamp",unixTimeStamp);
                     singleObj.accumulate("Message", "invlaid userid and wordid");
            }
        
            rs.close();
            pstmt.close();
            conn.close();
        }
            catch(Exception e)
        {
            e.printStackTrace();
            
                 singleObj.accumulate("Status", "ERROR");
                     singleObj.accumulate("Timestamp",unixTimeStamp);
                     singleObj.accumulate("Message", "Database Connection Failed");     
        }
        
        String result =  singleObj.toString();

        return result;
        
    }
  
    
    
    
    @GET
    @Path("singleuser&{id}")
    @Produces("application/json")
    public String getSingleUser(@PathParam("id") int id) {
        int user_id=id;
        try 
        {
            conn = getCon();
            stmt=conn.createStatement();
            sql="select * from users where user_id="+ user_id;
         
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int uid = rs.getInt("user_id");
                String name=rs.getString("user_name");
                String email=rs.getString("email");
                String password=rs.getString("password");
                String dob=rs.getString("dob");
                 singleObj.accumulate("Status", "OK");
                 singleObj.accumulate("Timestamp",  unixTimeStamp);
                 singleObj.accumulate("user_id",uid);
                  singleObj.accumulate("user_name",name);
                   singleObj.accumulate("email",email);
                    singleObj.accumulate("password",password);
                     singleObj.accumulate("dob",dob);
                   
                 
                
                 empty = false;
             }
        
            if (empty) {
                 singleObj.accumulate("Status", "WARNING");
                singleObj.accumulate("Timestamp",  unixTimeStamp);
                 singleObj.accumulate("user_id", user_id);
                 singleObj.accumulate("MESSAGE", "No information available");
            }
            
            rs.close();
            stmt.close();
            conn.close();
       
            
        } catch (SQLException se) {
                singleObj.accumulate("Status", "ERROR");
                 singleObj.accumulate("Timestamp", unixTimeStamp);
                  singleObj.accumulate("user_id", user_id);
                 
                 singleObj.accumulate("MESSAGE", "database connection failed");
            
        } catch (Exception e){
            e.printStackTrace();
        } 
           
            String result = singleObj.toString();
           
           
           return result;
       }
     
    @GET
    @Path("languagelist")
    @Produces("application/json")
    public String languagelist() throws SQLException, ClassNotFoundException {
       
        
          try
          {
            conn = getCon(); 
            sql = "select * from languages";  
            
             stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             
              while(rs.next())
            {
                int lid = rs.getInt("language_id");
                String name=rs.getString("language_name");
                String image=rs.getString("image_url");
               singleObj.accumulate("language_id", lid);
               singleObj.accumulate("language_name", name);
               singleObj.accumulate("image_url", image);
                mainArray.add(singleObj);
               singleObj.clear();
            }
              
             if(mainArray.isEmpty())
             {
               main.accumulate("Status","WARNING");
              main.accumulate("Timestamp",unixTimeStamp);
              main.accumulate("languagelist", mainArray.toString());
              main.accumulate("Message","No information available");
             }
              
              main.accumulate("Status","OK");
              main.accumulate("Timestamp",unixTimeStamp);
                main.accumulate("languagelist", mainArray);
              
              
              
          }catch(SQLException e)
        {
            e.printStackTrace();
            
              main.accumulate("Status","ERROR");
              main.accumulate("Timestamp",unixTimeStamp);
              main.accumulate("Message","database connection failed");
        }
          
        return main.toString();
    }
    @GET
    @Path("courselist&{languageid}")
    @Produces("application/json")
    public String courselist(@PathParam("languageid") int lid)  throws SQLException, ClassNotFoundException {
       
        int language_id=lid;
          try
          {
              conn = getCon(); 
            sql = "select * from courses where LANGUAGE_ID="+language_id;  
            
             stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             
              while(rs.next())
            {
                int cid = rs.getInt("course_id");
                String name=rs.getString("course_name");
                 String description=rs.getString("description");
                 String image=rs.getString("image");
                
               singleObj.accumulate("course_id", cid);
               singleObj.accumulate("course_name", name);
               singleObj.accumulate("description", description);
               singleObj.accumulate("image",image);
              mainArray.add(singleObj);
               singleObj.clear();
            }
              
             if(mainArray.isEmpty())
             {
               main.accumulate("Status","WARNING");
              main.accumulate("Timestamp",unixTimeStamp);
              main.accumulate("courselist", mainArray.toString());
              main.accumulate("Message","No information available");
             }
              
              main.accumulate("Status","OK");
              main.accumulate("Timestamp",unixTimeStamp);
                main.accumulate("courselist", mainArray);
              
              
              
          }catch(SQLException e)
        {
            e.printStackTrace();
            
              main.accumulate("Status","ERROR");
              main.accumulate("Timestamp",unixTimeStamp);
              main.accumulate("Message","database connection failed");
        }  
        return main.toString();
    }
    
 
    
    @GET
    @Path("singlecourse&{id}")
    @Produces("application/json")
    public String getSingleCourse(@PathParam("id") int id) {
        int course_id=id;
        try 
        {
            conn = getCon();
            stmt=conn.createStatement();
            sql="select * from courses where course_id="+ course_id;
         
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int scid = rs.getInt("course_id");
                String name=rs.getString("course_name");
                String description=rs.getString("description");
                 singleObj.accumulate("Status", "OK");
                 singleObj.accumulate("Timestamp",  unixTimeStamp);
                 singleObj.accumulate("course_id",scid);
                  singleObj.accumulate("course_name",name);
                   singleObj.accumulate("description",description);
                   
                 empty = false;
             }
        
            if (empty) {
                 singleObj.accumulate("Status", "WARNING");
                singleObj.accumulate("Timestamp",  unixTimeStamp);
                 singleObj.accumulate("course_id", course_id);
                 singleObj.accumulate("MESSAGE", "No information available");
            }
            
            rs.close();
            stmt.close();
            conn.close();
       
            
        } catch (SQLException se) {
                singleObj.accumulate("Status", "ERROR");
                 singleObj.accumulate("Timestamp", unixTimeStamp);
                  singleObj.accumulate("course_id", course_id);
                 
                 singleObj.accumulate("MESSAGE", "database connection failed");
            
        } catch (Exception e){
            e.printStackTrace();
        } 
           
            String result = singleObj.toString();
           
           
           return result;
       }
    
    @GET
    @Path("categorieslist")
    @Produces("application/json")
    public String categorieslist() throws SQLException, ClassNotFoundException {
       
        
          try
          {
              conn = getCon(); 
            sql = "select * from categories";  
            
             stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             
              while(rs.next())
            {
                int ctid = rs.getInt("category_id");
                String name=rs.getString("category_name");
                 String image=rs.getString("category_image");
                String language_id=rs.getString("language_id");
               singleObj.accumulate("category_id", ctid);
               singleObj.accumulate("category_name", name);
               singleObj.accumulate("image", image);
               singleObj.accumulate("language_id", language_id);
               mainArray.add(singleObj);
               singleObj.clear();
            }
              
             if(mainArray.isEmpty())
             {
               main.accumulate("Status","WARNING");
              main.accumulate("Timestamp",unixTimeStamp);
              main.accumulate("categorieslist", mainArray.toString());
              main.accumulate("Message","No information available");
             }
              
              main.accumulate("Status","OK");
              main.accumulate("Timestamp",unixTimeStamp);
                main.accumulate("categorieslist", mainArray);
              
              
              
          }catch(SQLException e)
        {
            e.printStackTrace();
            
              main.accumulate("Status","ERROR");
              main.accumulate("Timestamp",unixTimeStamp);
              main.accumulate("Message","database connection failed");
        }
          
        return main.toString();
    }
    
    @GET
    @Path("wordslist&{id}")
    @Produces("application/json")
        public String wordslist(@PathParam("id") int id) {
        int course_id=id;
       
          try
          {
              conn = getCon(); 
            sql = "select * from words where CATEGORIES_ID="+ course_id;  
            
             stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             
              while(rs.next())
            {
               int wid = rs.getInt("word_id");
               String firstword=rs.getString("firstword");
               String secoundword=rs.getString("secoundword");
               singleObj.accumulate("word_id", wid);
               singleObj.accumulate("firstword",firstword);
               singleObj.accumulate("secoundword",secoundword);
               mainArray.add(singleObj);
               singleObj.clear();
            }
              
             if(mainArray.isEmpty())
             {
               main.accumulate("Status","WARNING");
              main.accumulate("Timestamp",unixTimeStamp);
              main.accumulate("wordslist", mainArray.toString());
              main.accumulate("Message","No information available");
             }
              
              main.accumulate("Status","OK");
              main.accumulate("Timestamp",unixTimeStamp);
                main.accumulate("wordslist", mainArray);
              
                 
          }catch(SQLException e)
        {
            e.printStackTrace();
            
              main.accumulate("Status","ERROR");
              main.accumulate("Timestamp",unixTimeStamp);
              main.accumulate("Message","database connection failed");
        }
          
        return main.toString();
    }
        
    @GET
    @Path("lecture&{id}")
    @Produces("application/json")
        public String lecture(@PathParam("id") int id) {
        int course_id=id;
       
          try
          {
              conn = getCon(); 
            sql = "select * from words where COURSE_ID="+ course_id;  
            
             stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             
              while(rs.next())
            {
               int wid = rs.getInt("word_id");
               String firstword=rs.getString("firstword");
               String secoundword=rs.getString("secoundword");
               singleObj.accumulate("word_id", wid);
               singleObj.accumulate("firstword",firstword);
               singleObj.accumulate("secoundword",secoundword);
               mainArray.add(singleObj);
               singleObj.clear();
            }
              
             if(mainArray.isEmpty())
             {
               main.accumulate("Status","WARNING");
              main.accumulate("Timestamp",unixTimeStamp);
              main.accumulate("wordslist", mainArray.toString());
              main.accumulate("Message","No information available");
             }
              
              main.accumulate("Status","OK");
              main.accumulate("Timestamp",unixTimeStamp);
                main.accumulate("wordslist", mainArray);
              
                 
          }catch(SQLException e)
        {
            e.printStackTrace();
            
              main.accumulate("Status","ERROR");
              main.accumulate("Timestamp",unixTimeStamp);
              main.accumulate("Message","database connection failed");
        }
          
        return main.toString();
    }
        
        
     
    @GET
    @Path("quelist&{id}")
    @Produces("application/json")
        public String quelist(@PathParam("id") int id) {
        int course_id=id;
       
          try
          {
              conn = getCon(); 
              sql = "select * from question where category_id="+ course_id;  
              stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             
              while(rs.next())
            {
                int wid = rs.getInt("QUESTION_ID");
                String QTN_TYPE=rs.getString("qtn_type");
                String QUESTION=rs.getString("question");
                String answer_one=rs.getString("answer_one");
                String answer_two=rs.getString("answer_two");
                String answer_three=rs.getString("answer_three");
                String answer_four=rs.getString("answer_four");
                String image_one=rs.getString("image_one");
                String image_two=rs.getString("image_two");
                String image_three=rs.getString("image_three");
                String image_four=rs.getString("image_four");
                String answer=rs.getString("answer");
             
               singleObj.accumulate("que_id", wid);
               singleObj.accumulate("question_type",QTN_TYPE);
               singleObj.accumulate("question",QUESTION);
               singleObj.accumulate("answer_one",answer_one);
               singleObj.accumulate("answer_two",answer_two);
               singleObj.accumulate("answer_three",answer_three);
               singleObj.accumulate("answer_four",answer_four);
               singleObj.accumulate("image_one",image_one);
               singleObj.accumulate("image_two",image_two);
               singleObj.accumulate("image_three",image_three);
               singleObj.accumulate("image_four",image_four);
               singleObj.accumulate("answer",answer);
               mainArray.add(singleObj);
               singleObj.clear();
            }
              
             if(mainArray.isEmpty())
             {
               main.accumulate("Status","WARNING");
              main.accumulate("Timestamp",unixTimeStamp);
              main.accumulate("wordslist", mainArray.toString());
              main.accumulate("Message","No information available");
             }
              else
             {
              main.accumulate("Status","OK");
              main.accumulate("Timestamp",unixTimeStamp);
              main.accumulate("quelist", mainArray);
             }
              
              
          }catch(SQLException e)
        {
            e.printStackTrace();
            
              main.accumulate("Status","ERROR");
              main.accumulate("Timestamp",unixTimeStamp);
              main.accumulate("Message","database connection failed");
        }
          
        return main.toString();
    }
    
 //SIGN IN   
    @GET
    @Path("signIn&{email}&{password}")
    @Produces("application/json")
    public String signIn(@PathParam("email") String email,@PathParam("password") String password) {
       
        
        conn = getCon();
        
        try {
            String sql;
            sql = "select * from USERS where email=? and password=?";
            pstmt  =  conn.prepareStatement(sql); 
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
           
            while (rs.next()) {
                int uid = rs.getInt("user_id");
                String name=rs.getString("user_name");
                String email1=rs.getString("email");
                String password1=rs.getString("password");
                String dob=rs.getString("dob");
                 singleObj.accumulate("Status", "OK");
                 singleObj.accumulate("Timestamp",  unixTimeStamp);
                 singleObj.accumulate("user_id",uid);
                  singleObj.accumulate("user_name",name);
                   singleObj.accumulate("email",email1);
                    singleObj.accumulate("password",password1);
                     singleObj.accumulate("dob",dob);
                   mainArray.add(singleObj);
               singleObj.clear();
             }
            
            if(mainArray.isEmpty())
             {
               main.accumulate("Status","WARNING");
              main.accumulate("Timestamp",unixTimeStamp);
              main.accumulate("login", mainArray.toString());
              main.accumulate("Message","No information available");
             }
              else
            {
              main.accumulate("Status","OK");
              main.accumulate("Timestamp",unixTimeStamp);
              main.accumulate("login", mainArray);
            }
                    // singleObj.accumulate("Status", "warning");
                   //  singleObj.accumulate("Timestamp",unixTimeStamp);
                   //  singleObj.accumulate("Message", "invlaid username or password");    
        }catch(Exception e)
        {
            e.printStackTrace();
            
              main.accumulate("Status","ERROR");
              main.accumulate("Timestamp",unixTimeStamp);
              main.accumulate("Message","database connection failed");   
        }
        return main.toString();
    }
    
    @GET
    @Path("resultlist&{userid}")
    @Produces("application/json")
    
    public String resultlist(@PathParam("userid") int userid) throws SQLException, ParseException   {
        try 
        {
             conn = getCon();
             String sql;
             sql = "select * from result where user_id=?";
             pstmt  =  conn.prepareStatement(sql);
             pstmt.setInt(1, userid);
             ResultSet rs = pstmt.executeQuery();
            
      while(rs.next())
            {
               int quizid=rs.getInt("quiz_id");
               int tscore=rs.getInt("total_score");
               String sqln;
               sqln = "select * from quiz where quiz_id=?";
               pstmt  =  conn.prepareStatement(sqln);
               pstmt.setInt(1, quizid);
               ResultSet rsn = pstmt.executeQuery();
               while (rsn.next()) {
               String quiz_name=rsn.getString("quiz_name");
                singleObj.accumulate("quiz_name",quiz_name);
               }
                singleObj.accumulate("quiz_id",quizid);
                singleObj.accumulate("total_score", tscore);
                mainArray.add(singleObj);
                singleObj.clear();
            }
       if(mainArray.isEmpty())
             {
               main.accumulate("Status","WARNING");
              main.accumulate("Timestamp",unixTimeStamp);
              main.accumulate("login", mainArray.toString());
              main.accumulate("Message","No information available");
             }
              else
            {
              main.accumulate("Status","OK");
              main.accumulate("Timestamp",unixTimeStamp);
              main.accumulate("data", mainArray);
            }
            
        }
            catch(Exception e)
        {
            e.printStackTrace();
            singleObj.accumulate("Status", "ERROR");
            singleObj.accumulate("Timestamp",unixTimeStamp);
            singleObj.accumulate("Message", "Database Connection Failed");     
        }
        
        String result =  main.toString();

        return result;
        
    }
    
   
   
    @GET
    @Path("addcomment&{comments}&{rate}&{quiz_id}&{user_id}")
    @Produces("application/json")
    public String addreview(@PathParam("comments") String comment,@PathParam("rate" ) int rating,@PathParam("quiz_id") int quizid,@PathParam("user_id") int userid) {
        conn = getCon();
        try {
            
            //DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY");
          //  java.util.Date date1 = new java.util.Date();
          //  java.sql.Date d = new java.sql.Date(date1.getTime());
            
            sql = "INSERT into rate_review values (rate_pk.nextval,?,?,?,?)";
            pstmt = conn.prepareStatement(sql);
           // pstmt.setDate(1, d);
            pstmt.setString(1, comment);
            pstmt.setInt(2, rating);
            pstmt.setInt(3, quizid);
            pstmt.setInt(4, userid);
            
            if (pstmt.executeUpdate() == 1) {
                singleObj.clear();
                singleObj.accumulate("Status", "OK");
                singleObj.accumulate("Timestamp", unixTimeStamp);
                singleObj.accumulate("Message", "Comment Added Successfully");
            }

            pstmt.close();
            conn.close();

        } catch (SQLException se) {
            se.printStackTrace();
            singleObj.accumulate("Status", "ERROR");
                     singleObj.accumulate("Timestamp",unixTimeStamp);
                     singleObj.accumulate("Message", "Database Connection Failed");     
        }
        String result = singleObj.toString();
        return result;
    }
    
    @GET
    @Path("share&{platform_id}&{result_id}&{user_id}")
    @Produces("application/json")
    public String share(@PathParam("platform_id") int platform, @PathParam("result_id") int result_id, @PathParam("user_id") int user_id) {
        conn = getCon();
        try {
            java.util.Date date1 = new java.util.Date();
            java.sql.Date d = new java.sql.Date(date1.getTime());
            sql = "INSERT into share1 values (?,?,?,?)";
            pstmt = conn.prepareStatement(sql);
           
            pstmt.setDate(1, d);
             pstmt.setInt(2,platform );
            pstmt.setInt(3, result_id);
            pstmt.setInt(4, user_id);
            if (pstmt.executeUpdate() == 1) {
                singleObj.clear();
                singleObj.accumulate("Status", "OK");
                singleObj.accumulate("Timestamp", unixTimeStamp);
                singleObj.accumulate("Message", "Successfull Added");
            }

            pstmt.close();
            conn.close();

        } catch (SQLException se) {
            se.printStackTrace();
             singleObj.accumulate("Status", "ERROR");
                     singleObj.accumulate("Timestamp",unixTimeStamp);
                     singleObj.accumulate("Message", "Database Connection Failed"); 
        }
        String result = singleObj.toString();
        return result;
    }
    
    @GET
    @Path("forgot&{email}")
    @Produces("application/json")
    public String sendMail(@PathParam("email") String email) throws MessagingException {
        
        
        Send("shruthi1694419", "reddy419", email , "", "Forgot password request", "Hello from **");
        
        return "";
    }

    
    @GET
    @Path("result&{total}&{quizid}&{usrid}")
    @Produces("application/json")
    public String result(@PathParam("total") int total,
            @PathParam("quizid") int quizid,
            @PathParam("usrid") int usrid
           ) throws ParseException {    
     
        
        conn = getCon();
      
        try {
            // java.util.Date date1 = new java.util.Date();
            // java.sql.Date d = new java.sql.Date(date1.getTime());
                sql = "INSERT into result values (users_sequence.nextval,?,?,?)";
                pstmt = conn.prepareStatement(sql);
               // pstmt.setInt(1, 1);
              ///  pstmt.setDate(2, d);
                pstmt.setInt(1, total);
                pstmt.setInt(2,quizid);
                pstmt.setInt(3, usrid);
               
                  if (pstmt.executeUpdate() == 1) {
                singleObj.clear();
                singleObj.accumulate("Status", "OK");
                singleObj.accumulate("Timestamp", unixTimeStamp);
                singleObj.accumulate("Message", "Successfull Added");
            }
                   
                
                pstmt.close();
              
            conn.close();

        }    catch(Exception e)
        {
            e.printStackTrace();
            
                 singleObj.accumulate("Status", "ERROR");
                     singleObj.accumulate("Timestamp",unixTimeStamp);
                     singleObj.accumulate("Message", "Database Connection Failed");     
        }
        
          
        String result =  singleObj.toString();
    return result;
    }

public static void Send(final String username, final String password, String recipientEmail, String ccEmail, String title, String message) throws AddressException, MessagingException {
    Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
    final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

    // Get a Properties object
    Properties props = System.getProperties();
    props.setProperty("mail.smtps.host", "smtp.gmail.com");
    props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
    props.setProperty("mail.smtp.socketFactory.fallback", "false");
    props.setProperty("mail.smtp.port", "465");
    props.setProperty("mail.smtp.socketFactory.port", "465");
    props.setProperty("mail.smtps.auth", "true");

    /*
    If set to false, the QUIT command is sent and the connection is immediately closed. If set 
    to true (the default), causes the transport to wait for the response to the QUIT command.

    ref :   http://java.sun.com/products/javamail/javadocs/com/sun/mail/smtp/package-summary.html
            http://forum.java.sun.com/thread.jspa?threadID=5205249
            smtpsend.java - demo program from javamail
    */
    props.put("mail.smtps.quitwait", "false");

    Session session = Session.getInstance(props, null);

    // -- Create a new message --
    final MimeMessage msg = new MimeMessage(session);

    // -- Set the FROM and TO fields --
    msg.setFrom(new InternetAddress(username + "@gmail.com"));
    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail, false));

    if (ccEmail.length() > 0) {
        msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccEmail, false));
    }

    msg.setSubject(title);
    msg.setText(message, "utf-8");
    msg.setSentDate(new java.util.Date());

    SMTPTransport t = (SMTPTransport)session.getTransport("smtps");

    t.connect("smtp.gmail.com", username, password);
    t.sendMessage(msg, msg.getAllRecipients());      
    t.close();
}

     
}
