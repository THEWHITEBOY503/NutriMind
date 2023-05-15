package com.example;

import com.jcraft.jsch.*;
import java.awt.AWTException;
import java.io.*;
import java.util.Scanner;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class App {
    // Change REMOTE_HOST and ENDPOINT_URL if your classifier server is running externally
    private static final String REMOTE_HOST = "localhost";
    private static final String USERNAME = "YOUR_USERNAME";
    private static final String PASSWORD = "YOUR_PASSWORD";
    // Set your SSH port on the classifier machine for SFTP-ing the file
    private static final int PORT = 22;
    // Change this to the location of your classerv.py file.
    private static final String UPLOAD_DIR = "/home/" + USERNAME + "/Desktop/keras/";
    private static final String ENDPOINT_URL = "http://localhost:8080";
    // Do not change this unless you're changing the name of the camera script-- it will break!
    private static final String PYTHON_SCRIPT = "cam.py";

    public static void main(String[] args) throws IOException, AWTException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            // Clear the screen
            System.out.print("\033[H\033[2J");  
            System.out.flush();  
            System.out.println(" _   _       _        _ __  __ _           _\n" +
                           "| \\ | |_   _| |_ _ __(_)  \\/  (_)_ __   __| |\n" +
                           "|  \\| | | | | __| '__| | |\\/| | | '_ \\ / _` |\n" +
                           "| |\\  | |_| | |_| |  | | |  | | | | | | (_| |\n" +
                           "|_| \\_|\\__,_|\\__|_|  |_|_|  |_|_|_| |_|\\__,_|\n");
            System.out.println("NutriMind Lightweight - V0.1 Alpha 2");
            System.out.println("Welcome to NutriMind! Please select an option below:");
            System.out.println("1) Scan food item");
            System.out.println("2) Manually log food");
            System.out.println("3) View log");
            System.out.println("4) About");
            System.out.println("5) Exit");
            System.out.print("> ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    // Execute the Python script
                    try {
                        System.out.println("Press 's' to capture, press 'q' to cancel.");
                        ProcessBuilder processBuilder = new ProcessBuilder("python3", PYTHON_SCRIPT);
                        Process process = processBuilder.start();
                        int exitCode = process.waitFor();
                        if (exitCode == 0) {
                            // Yippie! 
                        } else {
                            InputStream errorStream = process.getErrorStream();
                            String errorString = IOUtils.toString(errorStream, StandardCharsets.UTF_8);
                            System.out.println("Whoops, that's an error executing cam.py! Here's some information to help you (and the devs) debug: \n" + errorString);
                            waitForEnter();
                            break;
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    String filePath = "saved_img.jpg"; // Provide the actual path to your file here
                    // Upload the captured image to the remote server using SFTP
                    JSch jsch = new JSch();
                    Session session = null;
                    try {
                        session = jsch.getSession(USERNAME, REMOTE_HOST, PORT);
                        session.setConfig("StrictHostKeyChecking", "no");
                        session.setPassword(PASSWORD);
                        session.connect();
                        Channel channel = session.openChannel("sftp");
                        channel.connect();
                        ChannelSftp sftpChannel = (ChannelSftp) channel;
                        // Save the captured image as a PNG file
                        File file = new File(filePath);
                        sftpChannel.put(new FileInputStream(file), UPLOAD_DIR + file.getName());
                        sftpChannel.exit();
                        session.disconnect();
                    } catch (JSchException | SftpException e) {
                        e.printStackTrace();
                        scanner.close();
                        return;
                    }

                    // Make a POST request to the endpoint with the uploaded file name as an argument
                    URL url = new URL(ENDPOINT_URL);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setDoOutput(true);
                    String arg1 = filePath;
                    String jsonInputString = "{\"arg1\": \"" + arg1 + "\"}";

                    try (OutputStream os = con.getOutputStream()) {
                        byte[] input = jsonInputString.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        String webJson = (response.toString());
                        JSONObject jsonObject = new JSONObject(webJson);
                        String name = jsonObject.getString("name");
                        JSONObject nutritionInfo = jsonObject.getJSONObject("nutrition_info");
                        double calories = nutritionInfo.getDouble("Calories");
                        double servingSize = nutritionInfo.getDouble("Serving size");
                        double saturatedFat = nutritionInfo.getDouble("Saturated Fat");
                        double totalFat = nutritionInfo.getDouble("Total Fat");
                        double protein = nutritionInfo.getDouble("Protein");
                        int sodium = nutritionInfo.getInt("Sodium");
                        int potassium = nutritionInfo.getInt("Potassium");
                        int cholesterol = nutritionInfo.getInt("Cholesterol");
                        double totalCarbohydrates = nutritionInfo.getDouble("Total Carbohydrates");
                        double fiber = nutritionInfo.getDouble("Fiber");
                        double sugar = nutritionInfo.getDouble("Sugar");
                        System.out.println("Name: " + name);
                        System.out.println("Calories: " + calories);
                        System.out.println("Serving Size: " + servingSize + "g");
                        System.out.println("Total fat: " + totalFat + "g");
                        System.out.println("Saturated fat: " + saturatedFat + "g");
                        System.out.println("Protein: " + protein + "g");
                        System.out.println("Sodium: " + sodium + "mg");
                        System.out.println("Potassium: " + potassium + "mg");
                        System.out.println("Cholesterol: " + cholesterol + "mg");
                        System.out.println("Carbs Total: " + totalCarbohydrates + "g");
                        System.out.println("Fiber: " + fiber + "g");
                        System.out.println("Sugar: " + sugar + "g");
                    } catch (IOException e) {
                        e.printStackTrace();
                        scanner.close();
                        return;
                    }  
                    waitForEnter();
                    break;
                case 2:
                    System.out.println("Logging food manually...");
                    Scanner manFood = new Scanner(System.in);
                    System.out.println("Please enter the name of your food.");
                    String foodName = manFood.nextLine();
                    System.out.print("Do you want to look up the nutrition information online? (Y/N): ");

                    String response = manFood.nextLine().trim().toLowerCase();

                    if (response.equals("y") || response.equals("yes")) {
                        try {
                            String apiKey = "YOUR-API-KEY-HERE"; // Replace with your actual API key
            
                            // Construct the URL with the query parameter
                            String urlStr = "https://api.api-ninjas.com/v1/nutrition?query=" + foodName;
                            URL url2 = new URL(urlStr);
            
                            // Open a connection to the URL
                            HttpURLConnection connection = (HttpURLConnection) url2.openConnection();
                            // Set the request method to GET
                            connection.setRequestMethod("GET");
                            // Set the custom header
                            connection.setRequestProperty("X-Api-Key", apiKey);
                            // Read the response from the server
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder response2 = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response2.append(line);
                            }
                            reader.close();
                            connection.disconnect();
                            // Print the response
                            String manJson = (response2.toString());
                            JSONArray jsonArray = new JSONArray(manJson);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String name = jsonObject.getString("name");
                                double calories = jsonObject.getDouble("calories");
                                double servingSize = jsonObject.getDouble("serving_size_g");
                                double fatTotal = jsonObject.getDouble("fat_total_g");
                                double fatSaturated = jsonObject.getDouble("fat_saturated_g");
                                double protein = jsonObject.getDouble("protein_g");
                                int sodium = jsonObject.getInt("sodium_mg");
                                int potassium = jsonObject.getInt("potassium_mg");
                                int cholesterol = jsonObject.getInt("cholesterol_mg");
                                double carbs = jsonObject.getDouble("carbohydrates_total_g");
                                double fiber = jsonObject.getDouble("fiber_g");
                                double sugar = jsonObject.getDouble("sugar_g");
                                System.out.println("Name: " + name);
                                System.out.println("Calories: " + calories);
                                System.out.println("Serving Size: " + servingSize + "g");
                                System.out.println("Total fat: " + fatTotal + "g");
                                System.out.println("Saturated fat: " + fatSaturated + "g");
                                System.out.println("Protein: " + protein + "g");
                                System.out.println("Sodium: " + sodium + "mg");
                                System.out.println("Potassium: " + potassium + "mg");
                                System.out.println("Cholesterol: " + cholesterol + "mg");
                                System.out.println("Carbs Total: " + carbs + "g");
                                System.out.println("Fiber: " + fiber + "g");
                                System.out.println("Sugar: " + sugar + "g");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (response.equals("n") || response.equals("no")) {
                        System.out.println("Serving size (grams): ");
                        double servingSize = scanner.nextDouble();
                        System.out.println("Calories (cal): ");
                        double calories = scanner.nextDouble();
                        System.out.println("Total fat (g): ");
                        double fatTotal = scanner.nextDouble();
                        System.out.println("Saturated Fat (g): ");
                        double fatSaturated = scanner.nextDouble();
                        System.out.println("Protein (g): ");
                        double protein = scanner.nextDouble();
                        System.out.println("Sodium (mg): ");
                        int sodium = scanner.nextInt();
                        System.out.println("Potassium (mg): ");
                        int potassium = scanner.nextInt();
                        System.out.println("Cholesterol (mg): ");
                        int cholesterol = scanner.nextInt();
                        System.out.println("Carbs (g): ");
                        double carbs = scanner.nextDouble();
                        System.out.println("Fiber (g): ");
                        double fiber = scanner.nextDouble();
                        System.out.println("Sugar (g): ");
                        double sugar = scanner.nextDouble();
                        System.out.println("Name: " + foodName);
                        System.out.println("Calories: " + calories);
                        System.out.println("Serving Size: " + servingSize + "g");
                        System.out.println("Total fat: " + fatTotal + "g");
                        System.out.println("Saturated fat: " + fatSaturated + "g");
                        System.out.println("Protein: " + protein + "g");
                        System.out.println("Sodium: " + sodium + "mg");
                        System.out.println("Potassium: " + potassium + "mg");
                        System.out.println("Cholesterol: " + cholesterol + "mg");
                        System.out.println("Carbs Total: " + carbs + "g");
                        System.out.println("Fiber: " + fiber + "g");
                        System.out.println("Sugar: " + sugar + "g");
                    } else {
                        System.out.println("Invalid response. Please enter Y or N.");
                    }
                    waitForEnter();
                    break;
                case 3:
                    System.out.println("Sorry, but the nutrition log is still in progress! Thank you for your patience.");
                waitForEnter();
                break;
                case 4:
                System.out.println(" _   _       _        _ __  __ _           _\n" +
                           "| \\ | |_   _| |_ _ __(_)  \\/  (_)_ __   __| |\n" +
                           "|  \\| | | | | __| '__| | |\\/| | | '_ \\ / _` |\n" +
                           "| |\\  | |_| | |_| |  | | |  | | | | | | (_| |\n" +
                           "|_| \\_|\\__,_|\\__|_|  |_|_|  |_|_|_| |_|\\__,_|\n");
                System.out.println("NutriMind\n" +
                "Lightweight edition\n" +
                "Version 0.1 -- Closed Alpha 2\n" +
                "\n" +
                "NutriMind is a nutrition logging app that lets you take a picture of a food item and log it to a nutrition book using the power of AI image classification\n" +
                "\n" +
                "Attributions\n" +
                "NutriMind team: \n" +  
                "Conner Smith & Sharice Walton \n" +
                "\n" +
                "A special thank you to all the hardworking men and women who build all the libraries this program was written and developed on top / along side. This couldn't have been done without you.\n" +
                "\n" +
                "Teachable Machine - Google - http://teachablemachine.withgoogle.com/\n" +
                "ChatGPT - OpenAI - https://chat.openai.com\n" +
                "\n" +
                "The code for NutriMind is avaiable publiclly at https://github.com/THEWHITEBOY503/NutriMind/\n" +
                "The code for TM-ImageClassifier, the framework built for classifying food items for NM, is available at https://github.com/THEWHITEBOY503/TM-ImageClassifier\n" +
                "\n" +
                "Special Thanks:\n" +
                "Tina Cone\n" +
                "Kim Gunnels\n" +
                "Marissa Pinder\n" +
                "Shannon Donaghue\n" +
                "Stephanie Burnham\n" +
                "Catherine Gaschen\n" +
                "Academy High School Class of 2023\n" + 
                "...and you, the end user. Thank you. <3\n" +
                "\n" +
                "Written with <3 by Conner Smith. \n" +
                "\n" +
                "NutriMind 0.1 Alpha 1 - Built 5/11/23"
                );
                waitForEnter();
                break;
                case 5:
                    System.out.println("Exiting NutriMind. Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    waitForEnter();
                    break;
            }
        }
    }
    private static void waitForEnter() {
        try {
            System.out.println("Press Enter to continue...");
            System.in.read();
            while (System.in.available() > 0) {
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
    