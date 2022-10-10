package edu.jsu.mcis.cs310;

import java.sql.*;
import org.json.simple.*;
import org.json.simple.parser.*;
import java.util.*;

public class Database {

    private final Connection connection;

    private final int TERMID_SP22 = 1;

    /* CONSTRUCTOR */
    public Database(String username, String password, String address) {

        this.connection = openConnection(username, password, address);

    }

    /* PUBLIC METHODS */
    public String getSectionsAsJSON(int termid, String subjectid, String num) {
        String result = null;
        String query = "SELECT * FROM section WHERE termid=? AND subjectid=? AND num=?";

        PreparedStatement psm = null;
        ResultSet rset = null;

        JSONArray jsonArray = new JSONArray();
        JSONObject row = new JSONObject();

        try {
            psm = this.connection.prepareStatement(query);

            psm.setInt(1, termid);
            psm.setString(2, subjectid);
            psm.setString(3, num);

            boolean hasResults = psm.execute();

            if (hasResults) {
                rset = psm.getResultSet();
                
                result = getResultSetAsJSON(rset);

//                while (rset.next()) {
//                    ResultSetMetaData meta = rset.getMetaData();
//                    int cols = meta.getColumnCount();
//
//                    for (int k = 1; k <= cols; ++k) {
//                        String columnname = meta.getColumnName(k);
//                        row.put(columnname, rset.getObject(columnname));
//
//                    }
//
//                    jsonArray.add(row);
//                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rset != null) {
                try {
                    rset.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (psm != null) {
                try {
                    psm.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

        }

        return result;

    }

    public int register(int studentid, int termid, int crn) {

        int result = 0;

        String query = "INSERT INTO registration (studentid, termid, crn) VALUES (?,?,?)";

        PreparedStatement psm = null;

        try {
            psm = this.connection.prepareStatement(query);

            psm.setInt(1, studentid);
            psm.setInt(2, termid);
            psm.setInt(3, crn);

            result = psm.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (psm != null) {
                try {
                    psm.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

        }
        
        return result;

    }

    public int drop(int studentid, int termid, int crn) {

        int result = 0;

        String query = "DELETE FROM registration WHERE studentid=? AND termid=? AND crn=?";

        PreparedStatement psm = null;

        try {
            psm = this.connection.prepareStatement(query);

            psm.setInt(1, studentid);
            psm.setInt(2, termid);
            psm.setInt(3, crn);

            result = psm.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (psm != null) {
                try {
                    psm.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;

    }

    public int withdraw(int studentid, int termid) {

        int result = 0;

        String query = "DELETE FROM registration WHERE studentid=? AND termid=?";

        PreparedStatement psm = null;

        try {
            psm = this.connection.prepareStatement(query);

            psm.setInt(1, studentid);
            psm.setInt(2, termid);

            result = psm.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (psm != null) {
                try {
                    psm.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return result;

    }

    public String getScheduleAsJSON(int studentid, int termid) {

        String result = null;
        String query = "SELECT registration.studentid,section.* FROM registration INNER JOIN section ON registration.crn = section.crn WHERE registration.studentid=? AND registration.termid=?";

        PreparedStatement psm = null;
        ResultSet rset = null;

        JSONArray jsonArray = new JSONArray();

        try {
            psm = this.connection.prepareStatement(query);

            psm.setInt(1, studentid);
            psm.setInt(2, termid);

            boolean hasResults = psm.execute();

            if (hasResults) {
                rset = psm.getResultSet();

                while (rset.next()) {
                    ResultSetMetaData meta = rset.getMetaData();
                    int cols = meta.getColumnCount();
                    System.out.println("here" + rset.getObject("crn"));
                    
                    JSONObject row = new JSONObject();

                    
                    for (int k = 1; k <= cols; ++k) {
                        String columnname = meta.getColumnName(k);
                        
                        row.put(columnname, rset.getObject(columnname).toString());
                        
                    }
                    
                    jsonArray.add(row);
                }

            }
            
            result = JSONValue.toJSONString(jsonArray);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rset != null) {
                try {
                    rset.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (psm != null) {
                try {
                    psm.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

        }

        
        System.out.println(result);
        return result;

    }

    public int getStudentId(String username) {

        int id = 0;

        try {

            String query = "SELECT * FROM student WHERE username = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, username);

            boolean hasresults = pstmt.execute();

            if (hasresults) {

                ResultSet resultset = pstmt.getResultSet();

                if (resultset.next()) {
                    id = resultset.getInt("id");
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return id;

    }

    public boolean isConnected() {

        boolean result = false;

        try {

            if (!(connection == null)) {
                result = !(connection.isClosed());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }

    /* PRIVATE METHODS */
    private Connection openConnection(String u, String p, String a) {

        Connection c = null;

        if (a.equals("") || u.equals("") || p.equals("")) {
            System.err.println("*** ERROR: MUST SPECIFY ADDRESS/USERNAME/PASSWORD BEFORE OPENING DATABASE CONNECTION ***");
        } else {

            try {

                String url = "jdbc:mysql://" + a + "/jsu_sp22_v1?autoReconnect=true&useSSL=false&zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=America/Chicago";
                // System.err.println("Connecting to " + url + " ...");

                c = DriverManager.getConnection(url, u, p);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return c;

    }

    private String getResultSetAsJSON(ResultSet resultset) {

         
        String result;
        
        /* Create JSON Containers */
        
        JSONArray json = new JSONArray();
        JSONArray keys = new JSONArray();
        
        try {
            
            /* Get Metadata */
        
            ResultSetMetaData metadata = resultset.getMetaData();
            int columnCount = metadata.getColumnCount();
            
            
            while (resultset.next()) {
                JSONObject obj = new JSONObject();
                for (int i = 0; i < columnCount; i++) {
                    obj.put(metadata.getColumnLabel(i + 1).toLowerCase(), resultset.getObject(i + 1).toString());
                }
                json.add(obj);
            }
        }
        catch (Exception e) { e.printStackTrace(); }
        
        /* Encode JSON Data and Return */
        
        result = JSONValue.toJSONString(json);
        return result;

    }

}
