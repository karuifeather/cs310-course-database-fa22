package edu.jsu.mcis.cs310;


import org.json.simple.*;
import org.json.simple.parser.*;

public class Main {

    public static void main(String[] args) {

        Database db = new Database("root", "fiji@", "localhost");

        if (db.isConnected()) {
            System.err.println("Connected Successfully!");

            String test = db.getSectionsAsJSON(1, "CS", "230");
            
            System.out.println(test);
            
            JSONArray testar = (JSONArray) JSONValue.parse(test);
            
            System.out.println(testar.size());

        }

    }

}
