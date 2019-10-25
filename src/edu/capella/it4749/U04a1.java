package edu.capella.it4749;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class U04a1 {
//created the necessary private variables that will be used later in the program
private static String apiKey = "6c1cd62497ac4d3298976e0decb7d7d1";
private static String openWeatherURL = "https://api.openweathermap.org/data/2.5/weather?";
private static Logger log = Logger.getLogger(U04a1.class.getName());
    
    public static void main(String[] args) {
        //created the fileHandler and simpleformatter to create logs into the proper log file
        try {
            FileHandler fileHandler = new FileHandler("weather.log");
            SimpleFormatter simple = new SimpleFormatter();
            //set the simpleformatter to the filehandler
            fileHandler.setFormatter(simple);
            log.setLevel(Level.ALL);
            log.addHandler(fileHandler);
            log.getLogger("").getHandlers()[0].setLevel(Level.WARNING);
            //caught any exceptions that needed to be caught 
        } catch(IOException ex) {
            System.err.println("Error opening the Log File...");
            System.exit(1);
        }
        
        //named my asynchronous call result and set it to null for future use
        Future<Weather> result = null;
        try {
            //makes sure it is collecting the data from over the web
            log.log(Level.INFO, "Collecting the required weather information");
            
            //Made a call to my asynchronous method and put it in result
            result = getWeatherAsync();
        }
        catch(Exception ex) {
            //catching any exceptions that might be thrown
            log.log(Level.SEVERE, "Error in getting the call...");
            System.exit(1);
        }
    }
    
    public static Future<Weather> getWeatherAsync() {
        //Creating a new completable future for further use in this method
        CompletableFuture<Weather> weatherResult = new CompletableFuture<>();
        
        //Lambda expression
        new Thread( () -> {
            //everything must go inside the lambda expression that is necessary for the program to run
            //eveything must be initialized in the lambda expression
            URL weatherURL = null;
            Weather weather = null;
            
            //User inputs their zip code
            Scanner input = new Scanner(System.in);
            System.out.print("Enter a ZIP Code: ");
            String zip = input.nextLine();
            
            try {
                //program opens the openWeather app using the URL provided
                //with the correct URL and APIKey included
            weatherURL = new URL(openWeatherURL + "zip=" + zip + ",us&APPID=" + apiKey);
            log.log(Level.CONFIG, "URL: ", weatherURL.toString());
            
            } catch(Exception ex) {
                //catches any exception thrown
                log.log(Level.CONFIG, "Incorrect URL: ", ex.getMessage());
                ex.getMessage();
            } 
            
            //if the URL comes back and is not null then the code continues
            if(Objects.nonNull(weatherURL)) {
                try {
                    //it now tries to open the program through the internet
                    HttpURLConnection connection = (HttpURLConnection) weatherURL.openConnection();
                    int code = connection.getResponseCode();
                    //it gets the response code from the http and if that is ok then the code continues
                    if(code == HttpURLConnection.HTTP_OK) {
                        try(Scanner data = new Scanner(connection.getInputStream())) {
                            weather = WeatherJSONParser.parseJsonWeatherData(data);
                        }
                        //if the weather is in JSON format and can be parsed the code continues
                        if(Objects.nonNull(weather)) {
                            //setting up different formats for use by the program
                            DecimalFormat tempFormat = new DecimalFormat("###.0 °F");
                            DecimalFormat baroFormat = new DecimalFormat("##.00\"  hg");
                            DecimalFormat windFormat = new DecimalFormat("## mph");
                            
                            //Here the weather information is displayed with all of the correct formats in place
                            System.out.println("Temperature: " + tempFormat.format(weather.getCurrentTemp()));
                            System.out.println("Max_Temp: " + tempFormat.format(weather.getHighTemp()));
                            System.out.println("Min_Temp: " + tempFormat.format(weather.getLowTemp()));
                            System.out.println("Pressure: " + baroFormat.format(weather.getPressure()));
                            System.out.println("Humidity: " + Integer.toString(weather.getHumidity()));
                            System.out.println("Wind Speed: " + windFormat.format(weather.getWindSpeed()));
                            System.out.println("Wind Direction: " + Integer.toString(weather.getWindDirection()));
                        }
                    }
                    log.log(Level.INFO, "The required weather information is in the queue...");
                    weatherResult.complete(weather);
                }
                catch (IOException ex) {
                    //catches any exception thrown
                    log.log(Level.INFO, "Error connecting to the servers...");
                }
            }
            else {
                //This is from an earlier if statement line 83 that tells the program
                //the URL came back null and to exit
                log.log(Level.SEVERE, "URL Object was null...");
                System.exit(1);
            }
        }).start(); //Must have the start() at the end of a long lambda expression
        //Otherwise you run the risk of the program not starting
        
        return weatherResult;
    }
}
