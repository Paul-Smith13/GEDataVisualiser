import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GEItemDataGetterWriter {
	
	private List<Item> items;
	private static final String ITEM_DATA_DUMP_DIRECTORY_NAME = "ItemDataDump";
	private static final String ITEM_AVAILABILITY_FILENAME = "ItemsGEAvailability.txt";
	
	public GEItemDataGetterWriter(){
		this.items = new ArrayList<>();
	}
	
	//1.
	private static boolean checkIfItemDataDumpDirectoryExists(String path) { 
		Path itemDataDumpPath = Paths.get(path, ITEM_DATA_DUMP_DIRECTORY_NAME);
		if (!Files.exists(itemDataDumpPath)) {
			System.out.println("Didn't find " + ITEM_DATA_DUMP_DIRECTORY_NAME + " directory. Creating...");
			try {
				Files.createDirectories(itemDataDumpPath);
				System.out.println(itemDataDumpPath + " was created.");
				return true;
			} catch (IOException ioE) {
				System.err.println(ioE.getMessage());
				return false;
			}
		} else {
			System.out.println("Found directory: " + ITEM_DATA_DUMP_DIRECTORY_NAME);
			return true;
		}
	}

	//2. Read all item availability data from 'ItemAvailability.txt" file
	private void getAllItemsFromFile() {
		//String directory = "ItemDataDump";
		String file = ITEM_AVAILABILITY_FILENAME;
		List<Item> loadedItems = new ArrayList<>();
		
		try( BufferedReader br = new BufferedReader(new FileReader(file))) {
			String headerLine = br.readLine();
			if (headerLine == null) {
				System.out.println("ERROR:" + file + " is either empty or missing a header.");
				return;
			}
			System.out.println("Reading file...");
			//System.out.println(headerLine);
			String line;
			int itemsLoadedCounter = 0;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				String[] lineElements = line.split(",");
				if (lineElements.length == 4) {
					try {
						//if (Boolean.parseBoolean(lineElements[3].trim())) {
							Item lineItem = new Item(
									lineElements[0].trim(),
									lineElements[2].trim(),
									lineElements[1].trim(),
									lineElements[3].trim()
							);
							loadedItems.add(lineItem);
							itemsLoadedCounter++;
							System.out.println(itemsLoadedCounter);
						//}
					} catch (NumberFormatException nfe) {
						System.err.println(nfe.getMessage());
					} catch (IllegalArgumentException iae) {
						System.err.println(iae.getMessage());
					}
				} else {
					System.err.println("PROBLEM: line doesn't follow expected format " + file + ". Expected 4 elements, but found " + lineElements.length + " during line: " + line);
				}
			}	
			this.items = loadedItems;
			System.err.printf("Loaded %d items from %s", this.items.size(), file);
		} catch (FileNotFoundException e) {
			System.out.println("Didn't find " + file);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Problem with " + file);
			e.printStackTrace();
		}
	}
	
	
	//3. 
	private void accessGEWebsite() {
		//First handle if items is empty
		if (this.items.isEmpty()) {
			System.err.println("Problem: couldn't load any items from 'items' field to access GE Website. \nCheck: " + ITEM_AVAILABILITY_FILENAME);
			return;
		} else {
		 System.out.println("Attempting to access items on GE Website.");	
		}
		int itemsProcessed = 0;
		for (Item item: this.items) {
			itemsProcessed++;
			try {
				URL checkURL = new URL(item.getURL());
				HttpURLConnection connection = (HttpURLConnection) checkURL.openConnection();
				
				connection.setRequestProperty("User-Agent", "GEDataVisualiser (GitHub: https://github.com/Paul-Smith13/GEDataVisualiser)");
				connection.setRequestMethod("GET");
				connection.setConnectTimeout(5000);
				connection.setReadTimeout(10000);
				
				int responseCode = connection.getResponseCode();
				String responseMessage = connection.getResponseMessage();
				
				//Generate some output concerning the requests
				System.out.println("URL: " + item.getURL());
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
					item.setHTMLContent(content.toString());
					//System.out.println("Actual HTML content: " + this.getHTMLContent()); //Comment back in if we need to look at structure of htmlContent.
					item.searchWithinText(item.getHTMLContent());
					reader.close();			
				} else {
					System.out.println("✗ URL returned error code: " + responseCode);
				}
				connection.disconnect();
				writeItemDataToFile(item);
				
			} catch (IOException ioE) {
				System.err.println(" Accessing URL generated error: " + ioE.getMessage());
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			try {Thread.sleep(1000);} catch (InterruptedException ie) {System.err.println(ie);}
		}
		System.out.println("Finished accessing GE Website for item data.");
	}
		
	//4.
	private void writeItemDataToFile(Item item) {
		String individualItemName = item.getItemName().replaceAll("[^a-zA-Z0-9.\\-]", "_");
		Path outputItemFilePath = Paths.get(System.getProperty("user.dir"), ITEM_DATA_DUMP_DIRECTORY_NAME, individualItemName + "_DailyData.csv");
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputItemFilePath.toFile()))){
			bw.write("Date,Item Name, Daily Average Price, Trend Point, Daily Volume\n");
			if ( (item.getDailyData() != null) && (!item.getDailyData().isEmpty()) ) {
				for (GEItemDailyData dailyData : item.getDailyData()) {
					bw.write(dailyData.getDate() + ","
							+ dailyData.getItemName() + ","
							+ dailyData.getDailyAvgPrice() + ","
							+ dailyData.getTrendPoint() + ","
							+ dailyData.getDailyVolume() + "\n"
							);	
				}
				System.out.println("✅ Item: " + item.getItemName() + " successfully written to file " + item.getDailyData().size() + " data points.");
			} else {
				System.out.println("Item: " + item.getItemName() + " didn't have data available to be written.");
			}	
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
		}		
	}
	
	public static void main(String[] args) {
		/* 1. Check if itemDataDumpDirectory exists
		 * 1.1. if not create itemDataDumpDirectory 
		 * 2. Get all items from file
		 * 2.1. Check if items already have corresponding file in itemDataDumpDirectory
		 * 3. Access GE Website for each item
		 * 4. If GE page for item exists & has daily data (not IP blocked), get daily data
		 * 5. For items in 4. which have daily data, check if item exists in itemDataDumpDirectory 
		 * 5.1. If item in 5. doesn't exist, write all data to file 
		 * 5.2. If item in 5. does exist, only write data it is missing
		 */
		long startTime = System.currentTimeMillis();
		String cwd = System.getProperty("user.dir");
		//1.
		boolean directorySuccessfullySetUp = checkIfItemDataDumpDirectoryExists(cwd);
		if (!directorySuccessfullySetUp) {
			System.err.println("ERROR: wasn't able to set up ItemDataDump directory. Exiting.");
			return;
		}
		
		//2. 
		GEItemDataGetterWriter data = new GEItemDataGetterWriter();
		data.getAllItemsFromFile();
		
		//3.
		data.accessGEWebsite();
		
		//4. - Called from within accessGEWebsite
		
		
		//5. - haven't written this stage yet
		
		long endTime = System.currentTimeMillis();
		long timeTaken = endTime - startTime;
		System.out.println("System took " + timeTaken/1000 + " seconds, or " + ((timeTaken/1000)/60) + " minutes to run.");
		
	}

}
