import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.Writer;

public class ScrapeHTML {
	private String url = "";
	private String storeLocation = "";
	private String htmlContent = "";
	private String itemName;
	private long[] dailyAverages;
	private long[] trendPoints;
	private String[] dates;
	private long[] dailyVolumes;
	
	//Constructor - only initialises url & storeLocation, other variables are initialised elsewhere.
	public ScrapeHTML(String url, String storeToLocation) {
		this.url = url;
		this.storeLocation = storeToLocation;
	}
	
	//Looks point within htmlContent then calls methods to start initialising dailyAverages, trendPoints, dates
	public void searchWithinText(String text) {
		//start and end markers are as per structure of the time-series in the htmlContent variable.
		String startMarker = "var average"; //if structure of html changes, we can simply edit these
		String endMarker = "</script>";
		int startingIndex = text.indexOf(startMarker);
		int endingIndex = text.indexOf(endMarker, startingIndex);
		
		//Check if we can find both indices
		if (startingIndex == -1) {
			System.out.println("X Starting marker " + startMarker + " not found.");	
		} 
		if (endingIndex == -1) {
			System.out.println("X Ending marker " + endMarker + " not found.");
		}
		String operableText = text.substring(startingIndex, endingIndex);
		//System.out.println("\t\t Extracted the following text: \n" + operableText);
		
		//Next want to initialise the dates, trendPoints, and dailyAverages variables.
		//1. Dates
		this.setDates(extractDates(operableText));
		// for (String date: this.getDates()) {System.out.println(date);} //Delete comment if want to check what dates we have
		
		//Get Data for trendPoints and dailyAverages so we only have to call the method once
		long[][] dailyAndTrend = extractNumbers(operableText); 
		this.dailyAverages = new long[dailyAndTrend.length];
		this.trendPoints = new long[dailyAndTrend.length];
		
		//2. & 3. Loop for dailyAverages and trend points
		for (int i = 0; i < dailyAndTrend.length; i++) {
			this.trendPoints[i] = dailyAndTrend[i][0];
			this.dailyAverages[i] = dailyAndTrend[i][1];
		}
		
		//4. Get item name
		this.setItemName(extractItemName(text));
		System.out.println(this.getItemName());
		
		//5. Get Daily Volumes (if not bond - bond volumes not published)
		if (!this.getItemName().equals("Old School bond")) {
			this.setDailyVolumes(extractDailyVolumes(operableText));
		} 
	}
	
	public String extractItemName(String text) {
		//Use regex to find the item name
		Pattern itemNamePattern = Pattern.compile("<title>(.*?) - Grand Exchange");
		Matcher matcher = itemNamePattern.matcher(text);
		
		if (matcher.find()) {
			return matcher.group(1);
		} 
		return "Check - DNF";
	}
	
	public long[] extractDailyVolumes(String text) {
		Pattern itemNamePattern = Pattern.compile("trade\\d+\\.push\\(\\[new Date\\('[^']+'\\),\\s*(\\d+)\\s*\\]\\)");
		Matcher matcher = itemNamePattern.matcher(text);
		List<Long> foundVolumes = new ArrayList<>();
		while (matcher.find()) {
			long dailyVol = Long.parseLong(matcher.group(1));
			foundVolumes.add(dailyVol);
		}
		long[] result = new long[foundVolumes.size()];
		for (int i = 0; i < foundVolumes.size(); i++) {
			result[i] = foundVolumes.get(i);
		}
		return result;
	}
	
	public String[] extractDates(String text) {
		//Uses regex to find pattern we want
		//NOTE: HTML contains 2 lines for same date - 1 for dailyAvg, 1 for dailyVolume
		//To-do: later incorporate daily volumes as attributes & exception handling if they don't have (e.g. bonds)
		Pattern datePattern = Pattern.compile("average\\d+\\.push\\(\\[new Date\\('(\\d{4}/\\d{2}/\\d{2})");
		Matcher matcher = datePattern.matcher(text);
		List<String> newDates = new ArrayList<>();		
		while (matcher.find()) {
			String date = matcher.group(1);
			newDates.add(date);
			//System.out.println("Found date: " + date);
		}
		//System.out.println("Found " + newDates.size() + " dates");
		return newDates.toArray(new String[0]);
	}
	
	//Inefficient gets me entirely new long array, probably going to store these to file
	public long[][] extractNumbers(String text) {
		Pattern numberPattern = Pattern.compile("\\),\\s*(\\d+),\\s*(\\d+)\\]");
		Matcher matcher = numberPattern.matcher(text);
		List<long[]> foundNumbers = new ArrayList<>();
		while (matcher.find()) {
			long number1 = Long.parseLong(matcher.group(1));
			long number2 = Long.parseLong(matcher.group(2));
			foundNumbers.add(new long[] {number1, number2});	
		}
		long[][] result = new long[foundNumbers.size()][2];
		for (int i = 0; i < foundNumbers.size(); i++) {
			result[i][0] = foundNumbers.get(i)[0];
			result[i][1] = foundNumbers.get(i)[1];
		}
		return result;
	}
		
	//Checks we can access the ScrapeHTML's URL
	public void urlCanAccess() {
		try {
			URL checkURL = new URL(this.getURL());
			HttpURLConnection connection = (HttpURLConnection) checkURL.openConnection();
			
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36\"");
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(10000);
			
			int responseCode = connection.getResponseCode();
			String responseMessage = connection.getResponseMessage();
			
			//Generate some output concerning the requests
			System.out.println("URL: " + this.getURL());
			System.out.println("Response Code: " + responseCode);
			System.out.println("Response Message: " + responseMessage);
			System.out.println("Content Type: " + connection.getContentType());
			System.out.println("Content Length: " + connection.getContentLength());
			
			if (responseCode >= 200 && responseCode < 400) {
				System.out.println("✓ URL is accessible");
				//System.out.println("URL Content: " + connection.getContent()); //<-- if you want to see content of the HTML
				
				InputStream inputStream = connection.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				StringBuilder content = new StringBuilder();
				String line;
				
				while ((line = reader.readLine()) != null) {
					content.append(line).append("\n");
				}
				this.setHTMLContent(content.toString());
				//System.out.println("Actual HTML content: " + this.getHTMLContent()); //Comment back in if we need to look at structure of htmlContent.
				this.searchWithinText(this.getHTMLContent());
				reader.close();			
			} else {
				System.out.println("✗ URL returned error code: " + responseCode);
			}
			connection.disconnect();			
		} catch (IOException e) {
			System.err.println(" Accessing URL generated error: " + e.getMessage());
		}
		
	}
	
	public void writeToFile() {
		try (
			Writer writeToFile = new BufferedWriter(new FileWriter("output" + this.getItemName() + ".txt"));	
			){
			StringBuilder dataAsString = new StringBuilder();
			if (!this.getItemName().equals("Old school bond")) {
				for(int i = 0; i < this.getDates().length; i++) {
					dataAsString.append( "Date: " + this.getDates()[i] + " trendPoint: " +
					+ this.getDailyAvgerages()[i] + " dailyAverage: "
					+ this.getTrendPoints()[i] + " dailyVolume: "
					+ this.getDailyVolumes()[i] + ", \n"
					);
				}
			} else {
				for(int i = 0; i < this.getDates().length; i++) {
					dataAsString.append( "Date: " + this.getDates()[i] + " trendPoint: " +
					+ this.getDailyAvgerages()[i] + " dailyAverage: "
					+ this.getTrendPoints()[i] + " dailyVolume: "
					+ ", \n"
					);
				}
			}			
			String writeObj = "|Item: " + this.getItemName() + ", \nURL: "
					+ this.getURL() + "\nData: \n" 
					+ dataAsString;
			writeToFile.write(writeObj);
			System.out.printf("✓ SUCCESS: written %s data to file.", this.getItemName());
			
		} catch (IOException e) {
			System.err.println("✗ ERROR: wasn't able to write to file" + e.getMessage());
		}
	}
	
	public boolean isValidURL(String checkThisURL) {
		//First check start of URL (should begin with geURLStandard)
		String geURLStandard = "https://secure.runescape.com/m=itemdb_oldschool/";
		//String urlFirstCheck = checkThisURL.substring(0, 47);		
		if (checkThisURL.startsWith(geURLStandard)) {
			System.out.println("URL follows correct beginning format.");
			//Second check ending of URL (should end with /viewitem?obj=00)
			Pattern checkFinalPattern = Pattern.compile("viewitem\\?obj=\\d+");
			Matcher matchFinalPart = checkFinalPattern.matcher(checkThisURL);
			if (matchFinalPart.find()) {
				System.out.println("URL follows correct ending format.");
				return true;
			} else {
				System.err.println("URL doesn't follow correct ending format.");
			}
		} else {
			System.err.println("URL not recognised following correct beginning format.");
		}
		return false;
	}
	
	//Getters and setters
	public String getURL() {return this.url;}
	public void setURL(String url) {this.url = url;}
	public String getStoreLocation(){return this.storeLocation;}	
	public void setStoreLocation(String storeLocation) {this.storeLocation = storeLocation;}
	public String getHTMLContent() {return htmlContent;}
	public void setHTMLContent(String htmlContent) {this.htmlContent = htmlContent;}
	public String getItemName() {return this.itemName;}
	
	public void setItemName(String name) {this.itemName = name;}
	public long[] getDailyAvgerages() {return this.dailyAverages;}
	public void setDailyAverages(long[] dailyAvgs) {this.dailyAverages = dailyAvgs;}
	public long[] getTrendPoints() {return this.trendPoints;}
	public void setTrendPoints(long[] trendPoints) {this.trendPoints = trendPoints;}	
	public String[] getDates() {return this.dates;}
	public void setDates(String[] newDates) {this.dates = newDates;}
	public void setDailyVolumes(long[] dailyVolume) {this.dailyVolumes = dailyVolume;}
	public long[] getDailyVolumes() {return this.dailyVolumes;}
	
	public static void main(String[] args) {
		String cwDir = System.getProperty("user.dir");
		System.out.println(cwDir);
		Scanner s = new Scanner(System.in);
		System.out.println("--- GE PRICE, TREND, VOLUME DATA WEBSCRAPER ---");
		System.out.println("Enter an item's URL from the GE Website to obtain data for:");
		String testHTML = s.nextLine().trim();				
		ScrapeHTML s1 = new ScrapeHTML(testHTML, cwDir);
		//Need to handle if user puts in invalid HTML
		while (!s1.isValidURL(s1.getURL())) {
			System.err.println("Invalid, URL. Re-enter URL: ");
			s1.setURL(s.nextLine().trim());
		}
		s.close();	
		//System.out.println(s1.getURL() + " " + s1.getStoreLocation());

		s1.urlCanAccess();
		
		//Finally write to file:
		s1.writeToFile();
		
		
	}

}
