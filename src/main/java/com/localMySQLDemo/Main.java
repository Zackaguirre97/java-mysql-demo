package com.localMySQLDemo;

import java.sql.*;
import java.io.*;
import java.util.*;

public class Main {

    // Simple .env loader
    public static Map<String, String> loadEnv(String filePath) {
        Map<String, String> env = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) env.put(parts[0], parts[1]);
            }
        } catch (IOException e) {
            System.out.println("⚠️ Could not read .env file: " + e.getMessage());
        }
        return env;
    }

    public static void main(String[] args) {
        boolean isRailway = System.getenv("RAILWAY_ENVIRONMENT") != null;

        String host, port, db, user, password;

        if (isRailway) {
            System.out.println("🚉 Running in RAILWAY mode");
            host = System.getenv("MYSQLHOST");
            port = System.getenv("MYSQLPORT");
            db = System.getenv("MYSQLDATABASE");
            user = System.getenv("MYSQLUSER");
            password = System.getenv("MYSQLPASSWORD");
        } else {
            System.out.println("🧩 Running in LOCAL mode");
            Map<String, String> dotenv = loadEnv(".env");
            host = dotenv.get("LOCAL_MYSQLHOST");
            port = dotenv.get("LOCAL_MYSQLPORT");
            db = dotenv.get("LOCAL_MYSQLDATABASE");
            user = dotenv.get("LOCAL_MYSQLUSER");
            password = dotenv.get("LOCAL_MYSQLPASSWORD");
        }

        String url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=true&serverTimezone=UTC";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            System.out.println("✅ Connected to MySQL (" + (isRailway ? "Railway" : "Local") + ")");

            // Create table
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100),
                    email VARCHAR(100)
                )
            """;
            stmt.executeUpdate(createTableSQL);
            System.out.println("✅ Table ensured.");

            // Insert sample record
            stmt.executeUpdate("INSERT INTO users (name, email) VALUES ('Zack', 'zack@example.com')");
            System.out.println("✅ Sample record inserted.");

            // Query data
            ResultSet rs = stmt.executeQuery("SELECT * FROM users");
            System.out.println("✅ Data in 'users' table:");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + ": " + rs.getString("name") + " (" + rs.getString("email") + ")");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

