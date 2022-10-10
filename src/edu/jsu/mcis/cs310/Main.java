package edu.jsu.mcis.cs310;

public class Main {

    public static void main(String[] args) {

        Database db = new Database("root", "fiji@", "localhost");

        if (db.isConnected()) {
            System.err.println("Connected Successfully!");
        }

    }

}
