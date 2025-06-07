import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
//Used AI to write getters & setters
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Item {
	private String itemName;
	private String URL;
	private List<GEItemDailyData> dailyData;
	private int itemID;
	private boolean tradeableGE;
	private String htmlContent;

	// Constructor (optional, but good practice if you want to initialize all fields)
	public Item(String itemName, String URL, List<GEItemDailyData> dailyData, int itemID, boolean tradeableGE) {
		this.itemName = itemName;
		this.URL = URL;
		this.dailyData = dailyData;
		this.itemID = itemID;
		this.tradeableGE = tradeableGE;
	}
	
	public Item(String itemName, String URL, String itemID, String tradeableGE) {
		this.itemName = itemName;
		this.URL = URL;
		this.itemID = Integer.parseInt(itemID);
		this.tradeableGE = Boolean.parseBoolean(tradeableGE);
	}

	// Default constructor (if you want to create an Item object without immediately setting all fields)
	public Item() {
		
	}
	
	/* Daily Data
	private String itemName;
	private int dailyAvgPrice;
	private int trendPoint;
	private long dailyVolume;
	private LocalDate date;
	 */
	
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
		if (this.dailyData == null) {
			this.dailyData = new ArrayList<>();
		} else {
			this.dailyData.clear();
		}
				
		//Next want to initialise the dates, trendPoints, and dailyAverages variables. This will be stored within the Item's dailyData field:
		//1. Dates
		String[] dailyDataDates = extractDates(operableText);
		//2. & 3. Loop for dailyAverages and trend points
		long[][] dailyAndTrend = extractNumbers(operableText); 
		long[] dailyAvgPrice = new long[dailyAndTrend.length];
		long[] dailyTrendPoints = new long[dailyAndTrend.length];
		for (int i = 0; i < dailyAndTrend.length; i++) {
			dailyTrendPoints[i] = dailyAndTrend[i][0];
			dailyAvgPrice[i] = dailyAndTrend[i][1];
		}		//4. Get item name
		// for (String date: this.getDates()) {System.out.println(date);} //Delete comment if want to check what dates we have
		//After we've got this info, we can then initialise GEItemDailyData object with it
		
		//5. Get Daily Volumes (if not bond - bond volumes not published)
		long[] dailyVolumes = null;
		if (!this.getItemName().equals("Old School bond")) {
			dailyVolumes = (extractDailyVolumes(operableText));
		} else {
			System.out.println("No volume data for OS Bonds on GE.");
		}
		int dataPoints = dailyDataDates.length;
		if (dataPoints != dailyAndTrend.length) {
			System.err.printf("\nPROBLEM: Daily Date length = %d but Daily Price & Trend length = %d", dataPoints, dailyAndTrend.length);
			return;
		}
		
		for (int i = 0; i < dataPoints; i++) {
			String dateString = dailyDataDates[i];
			LocalDate date = null;
			try {
				date = LocalDate.parse(dateString.replace("/", "-"));
			} catch (java.time.format.DateTimeParseException DTPE) {
				System.err.println("ERROR: couldn't parse date for "+ this.itemName + " " + DTPE.getMessage());
				continue;
			}
			int dailyAveragePrice = (int) dailyAndTrend[i][1];
			int trendPoint = (int) dailyAndTrend[i][0];
			long dailyVol = 0L;
			if ( (dailyVolumes != null) 
				&& (i < dailyVolumes.length) ) {
				dailyVol = dailyVolumes[i];
			} else if (this.getItemName().equals("OldSchool bond")) {
				dailyVol = 0L;
			} else if (dailyVolumes == null) {
				System.err.println("ERROR: failed to extract any daily volumes for " + this.getItemName());
			}
			GEItemDailyData geDD = new GEItemDailyData(date, this.getItemName(), dailyAveragePrice, trendPoint, dailyVol);
			this.dailyData.add(geDD);
			
		}	
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
	
	
	
	
	
	
	
	// --- Getters ---
	public String getItemName() {return itemName;}
	public String getURL() {return URL;}
	public List<GEItemDailyData> getDailyData() {return dailyData;}
	public int getItemID() {return itemID;}
	public boolean isTradeableGE() {return tradeableGE;}
	public String getHTMLContent() {return this.htmlContent;}
	
	// --- Setters ---
	public void setItemName(String itemName) {this.itemName = itemName;}
	public void setURL(String URL) {this.URL = URL;}
	public void setDailyData(List<GEItemDailyData> dailyData) {this.dailyData = dailyData;}
	public void setItemID(int itemID) {this.itemID = itemID;}
	public void setTradeableGE(boolean tradeableGE) {this.tradeableGE = tradeableGE;}
	public void setHTMLContent(String htmlContent) {this.htmlContent = htmlContent;}

	
}