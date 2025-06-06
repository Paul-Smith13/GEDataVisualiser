


/* This class will be used to bulk scrape item names & their Item IDs from:
 * https://oldschool.runescape.wiki/w/Item_IDs
 * As far as I know, the above link is community run, but generally a good source of info
 * 
 * Input: html from https://oldschool.runescape.wiki/w/Item_IDs
 * Output: txt file containing item names & item ids
 * 
 * NOTES:
 * > Does the job
 * > Needs refactoring
 * > Maybe use Singleton Design pattern: all we really need is one instance of the data being passed around (HTML content)
 * > Including threads is overkill & not clear they actually help
 * 
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BigScraper extends ScrapeHTML implements Runnable {
//Total number of items in list = 14606
//Want to use threads to divide and conquer
	private String url = "";
	private String storeLocation = "";
	private String htmlContent = "";
	private String itemName;
	private long[] dailyAverages;
	private long[] trendPoints;
	private String[] dates;
	private long[] dailyVolumes;	
	private static int rangeOfItems = 14606;
	private static volatile int itemRangeCounter = 0;
	private int[] itemRange;
	private String itemID = "";
	
	public BigScraper(String url, String storeToLocation) {
		super(url, storeToLocation);	
		incrementItemRangeCounter();
	}

	//Use to let thread know what range of items it is going over
	public synchronized void incrementItemRangeCounter() {
		int incrementBy = ((rangeOfItems - itemRangeCounter) != 2606) ? 3000 : 2606;
		int startOfRange = itemRangeCounter;
		itemRangeCounter += incrementBy;
		int endOfRange = itemRangeCounter;
		this.itemRange = new int[endOfRange - startOfRange];
		for (int i = 0; i < itemRange.length; i++) {
			itemRange[i] = i;
		}
	}
	
	@Override
	public String extractItemName(String text) {
		//Use regex to find the item name
		Pattern itemNamePattern = Pattern.compile("<a[^>]*?>(.*?)</a>");
		Matcher matcher = itemNamePattern.matcher(text);
		if (matcher.find()) {
			return matcher.group(1);
		} 
		return "Check - DNF";
	}
	
	public String extractItemID(String text) {
	//Use regex to find the item name
		Pattern itemNamePattern = Pattern.compile("amp;id=(\\d+)");
		Matcher matcher = itemNamePattern.matcher(text);
		if (matcher.find()) {
			return matcher.group(1);
		} 
		return "Check - DNF";
	}
	
	@Override
	public void searchWithinText(String text) {
		//start and end markers are as per structure of the table in the htmlContent variable.
		String startMarker = "<tbody>"; //if structure of html changes, we can simply edit these
		String endMarker = "</tbody></table>";
		int startingIndex = text.indexOf(startMarker);
		int endingIndex = text.indexOf(endMarker, startingIndex);
		
		/*//Use for Debugging & checking content of the html text
		String fileCheckName = "htmlRawScrape.txt";
		Path filePath = Paths.get(this.storeLocation, fileCheckName);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()));
			writer.write(text);	
			writer.close();
		} catch (IOException e) {System.err.println(e);}
		*/
		
		//Check if we can find both indices
		if (startingIndex == -1) {System.out.println("X Starting marker " + startMarker + " not found.");} 
		if (endingIndex == -1) {System.out.println("X Ending marker " + endMarker + " not found.");}
		
		String tableBodyContent = text.substring(startingIndex + startMarker.length(), endingIndex);
		//tr = table row, td = table data
		Pattern rowPattern = Pattern.compile("<tr>(.*?)</tr>", Pattern.DOTALL);
		Matcher rowMatcher = rowPattern.matcher(tableBodyContent);
		
		if (rowMatcher.find()) {
			//Do nothing & proceed
		} else {
			System.err.println("ERROR: no rows found in the table body.");
		}
		Map<String, Integer> tableData = new HashMap<>();
		while (rowMatcher.find()) {
			String rowHtml = rowMatcher.group(0);
			//td = Table Data, e.g. tdPattern = table data pattern
			Pattern tdPattern = Pattern.compile("<td>(.*?)</td>", Pattern.DOTALL);
			Matcher tdMatcher = tdPattern.matcher(rowHtml);
			String itemName = "Check - DNF";
			String itemID = "Check - DNF";
			if (tdMatcher.find()) {
				String tdContent = tdMatcher.group(1);
				itemName = extractItemName(tdContent);
			}
			if (tdMatcher.find()) {
				String tdContent = tdMatcher.group(1);
				itemID = extractItemID(tdContent);
			}
			//Show what is being found:
			System.out.println("Item name: " + itemName);
			System.out.println("Item ID: " + itemID);
			//Store Data in HashMap
			tableData.put(itemName, Integer.parseInt(itemID));
			
		}
		//Write Data to file:
		writeToFile(tableData);
		
	}
	
	public void writeToFile(Map<String, Integer> storeTableData) {
		try (
			Writer writeToFile = new BufferedWriter(new FileWriter("ItemNamesIDs.txt"))	
			){
			StringBuilder dataForOutput = new StringBuilder();
			dataForOutput.append("Structure: Item Name, Item ID\n");
			storeTableData.forEach((itemName, itemID) -> {
				dataForOutput.append(itemName + ", " + itemID + "\n");
			}); 
			writeToFile.write(dataForOutput.toString());
		} catch (IOException e) {
			System.err.println("✗ ERROR: wasn't able to write to file" + e.getMessage());
		}
		
		
	}
	
	@Override
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
	
	@Override
	public void run() {
		this.urlCanAccess();
		System.out.println("Finished.");
		
	}
	
	//Getters & Setters not inherited
	public void setItemID(String itemID) {this.itemID = itemID;}
	public String getItemID() {return this.itemID;}

	public static void main(String[] args) {
		//Going to create 5 threads to do this
		//Probably better with more
		String urlToScrape = "https://oldschool.runescape.wiki/w/Item_IDs";
		String currentWorkingDirectory = System.getProperty("user.dir");
		//Should create an array of BigScraper objects (like threads below
		BigScraper bs1 = new BigScraper(urlToScrape, currentWorkingDirectory);
		BigScraper bs2 = new BigScraper(urlToScrape, currentWorkingDirectory);
		BigScraper bs3 = new BigScraper(urlToScrape, currentWorkingDirectory);
		BigScraper bs4 = new BigScraper(urlToScrape, currentWorkingDirectory);
		BigScraper bs5 = new BigScraper(urlToScrape, currentWorkingDirectory);

		Thread[] threads = new Thread[5];

		threads[0] = new Thread(bs1, "FirstThread");
		//threads[0].start(); //For debugging
		
		threads[1] = new Thread(bs2, "SecondThread");
		threads[2] = new Thread(bs3, "ThirdThread");
		threads[3] = new Thread(bs4, "FourthThread");
		threads[4] = new Thread(bs5, "FifthThread");
		
		
		for (Thread thread : threads) {
			thread.start();
		}
		
		
	}
	
}
