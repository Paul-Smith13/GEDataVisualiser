import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemAvailabilityChecker {
	private String url = "";
	private String storeLocation = "";
	private boolean tradeableGE;
	private Map<String, Integer> itemNameIDMap;
	private String itemName = "";
	private int itemID;
	
	public ItemAvailabilityChecker() {
		
	}
	
	//1. Read from file
	private Map<String, Integer> readItemNameIDMapping(String filename) {
		Map<String, Integer> fileNameIDMap = new HashMap<>();
		//Pattern used to handle few cases where we have special cases that don't follow standard pattern
		Pattern linePattern = Pattern.compile("(.+?)(?:,\\s*)?(\\d+)$");
		
		try (BufferedReader reader = new BufferedReader (new FileReader(filename))){
			String line;
			System.out.println("");
			reader.readLine(); //Skip first line of file
			
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				Matcher lineMatcher = linePattern.matcher(line);
				if (lineMatcher.find()) {
					String itemName = lineMatcher.group(1).trim();
					String itemIDString = lineMatcher.group(2).trim();
					try {
						int itemID = Integer.parseInt(itemIDString);
						fileNameIDMap.put(itemName, itemID);
					} catch (NumberFormatException nfe) {
						System.err.println("Error: " + nfe.getMessage());
					}
				} else {
					System.err.println("ERROR: Skipped line: " + line);
				}
			}					
				
		} catch (IOException e) {
			System.err.println("ERROR: " + e.getMessage());
		}
		fileNameIDMap.forEach((itemName, itemID) -> {
			System.out.println("Item Name: " + itemName + ", \t\t\t\t Item ID: " +  itemID);
		});
		return fileNameIDMap;
	}
	
	//3c & 3d
	public static ItemAvailabilityChecker[] canAccessURL(ItemAvailabilityChecker[] itemArray) {
		int i = 0;
		for (ItemAvailabilityChecker item: itemArray) {
			i++;
			try {
				URL checkURL = new URL(item.getURL());
				HttpURLConnection connection = (HttpURLConnection) checkURL.openConnection();
				connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36\"");
				connection.setRequestMethod("GET");
				connection.setConnectTimeout(5000);
				connection.setReadTimeout(10000);
				
				int responseCode = connection.getResponseCode();
				String responseMessage = connection.getResponseMessage();
				//First check if we get an okay response code
				if (responseCode >= 200 && responseCode < 400) {
					System.out.println(i + " ✓ URL is accessible for " + item.getItemName());
					//Second check if we're actually getting a page containing item info, or just too many reqs from IP
					InputStream inputStream = connection.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					StringBuilder content = new StringBuilder();
					String line;
					
					while ((line = reader.readLine()) != null) {
						content.append(line).append("\n");
					}
					String allHtmlContent = content.toString();
					if (allHtmlContent.contains(item.getItemName())) {
						System.out.println(i + " ✓ HTML content contains " + item.getItemName());
						item.setTradeableGE(true);	
					} else {
						System.out.println(i + " ✗ HTML content doesn't contain "+ item.getItemName() + ": " + responseCode);
						item.setTradeableGE(false);
					}
				} else {
					System.out.println(i + "✗ URL returned error code for item "+ item.getItemName() + ": " + responseCode);
					item.setTradeableGE(false);
				}
				connection.disconnect();
			} catch (IOException e) {
				System.err.println(" Accessing URL generated error: " + e.getMessage());
			}		
		}
		return itemArray;
	}
	
	private static void writeToFile(ItemAvailabilityChecker[] itemsOnGE) {	
		try (	
			Writer writeToFile = new BufferedWriter(new FileWriter("ItemsGEAvailability.txt"));	
			){
			StringBuilder dataAsString = new StringBuilder();
			dataAsString.append("Item name, item ID, item URL, item found on GE\n");
			for (ItemAvailabilityChecker item: itemsOnGE) {
				dataAsString.append(item.getItemName() + "," 
						+ item.getItemID() + "," 
						+ item.getURL() + "," 
						+ item.getTradeableGE()
						+ "\n"
						);
			}
			String writeObj = dataAsString.toString();
			writeToFile.write(writeObj);
			System.out.printf("✓ SUCCESS: written all item data to file.");
			System.getProperty("user.dir");			
		} catch (IOException e) {
			System.err.println("✗ ERROR: wasn't able to write to file" + e.getMessage());
		}
	}
	

	//3a & 3b
	public static ItemAvailabilityChecker[] isValidURL(Map<String, Integer> mapFromFile) {
		String itemURLStart = "https://secure.runescape.com/m=itemdb_oldschool/";
		int records = mapFromFile.size();
		ItemAvailabilityChecker[] allItemArray = new ItemAvailabilityChecker[records];
		
		AtomicInteger currentIndex = new AtomicInteger(0);
		mapFromFile.forEach((itemName, itemID) -> {
            int i = currentIndex.getAndIncrement();
			//https://secure.runescape.com/m=itemdb_oldschool/Mind+tiara/viewitem?obj=5529
			StringBuilder URLBuilder = new StringBuilder(itemURLStart);
			allItemArray[i] = new ItemAvailabilityChecker();
			allItemArray[i].setItemName(itemName);
			allItemArray[i].setItemID(itemID);
			//Build the itemURL
			//Bunch of special characters need to be repalced for URL
			if (itemName.contains(" ")) { //Spaces to +
				URLBuilder.append(itemName.replace(" ", "+"));
			}
			if (itemName.contains("'")) {//apostraphe's to %27
				URLBuilder.append(itemName.replace("'", "%27"));
			}
			if (itemName.contains("(")) {// ( to %28
				URLBuilder.append(itemName.replace("(", "%28"));
			} 
			if (itemName.contains(")")) {// ) to %29
				URLBuilder.append(itemName.replace(")", "%29"));
			}
			if (itemName.contains("#")) {
				URLBuilder.append(itemName.replace("#", "%2B"));
			}
			URLBuilder.append("/viewitem?obj=" + itemID);			
			allItemArray[i].setURL(URLBuilder.toString());
			System.out.println(allItemArray[i].getURL());
			i++;
		});
		return allItemArray;
	}
		
	//Getters & Setters
	public String getURL() { return this.url;}
	public String getStoreLocation() {return this.storeLocation;}
	public boolean getTradeableGE() { return this.tradeableGE;} 
	public Map<String, Integer> getItemNameIDMap(){return this.itemNameIDMap;}
	public String getItemName() {return this.itemName;}
	public int getItemID() {return this.itemID;}
		
	public void setNameIDMap(Map<String, Integer> itemNameIDMap){this.itemNameIDMap = itemNameIDMap;}
	public void setStoreLocation(String location) {this.storeLocation = location;}
	public void setTradeableGE(boolean bool) {this.tradeableGE = bool;}
	public void setURL(String url) {this.url = url;} 
	public void setItemName(String name) {this.itemName = name;}
	public void setItemID(int ID) {this.itemID = ID;}
		
	public static void main(String[] args) {
		//Workflow follows this pattern:
		//1. Read data from file
		ItemAvailabilityChecker testCheck = new ItemAvailabilityChecker();
		System.out.println("--- Checks whether items are available on GE ---");
				
		//2. Use data to initialise our object
		Map<String, Integer> mapFromFile = 	testCheck.readItemNameIDMapping("ItemNamesIDs.txt");
		
		//3. Check in bulk what items have a page on the GE
		ItemAvailabilityChecker[] itemsOnGE = new ItemAvailabilityChecker[mapFromFile.size()]; 	
		//3a. isValidURL = true - we can proceed to 3c & 3d
		//3b. isValidURL = false - we can't proceed to 3c & 3d (something gone wrong - CHECK)
		itemsOnGE = isValidURL(mapFromFile);
		
		//3c. Success: tradeableGE = true
		//3d. Failure: tradeableGE = false
		itemsOnGE = canAccessURL(itemsOnGE);
		
		//4. Write results to file
		//4a. Record of what does & doesn't have a GE Page
		writeToFile(itemsOnGE);
		
		//5. Write only true results to separate file
		//5a. Record of what will be later scraped en masse on GE for data
		
		
	}
}
