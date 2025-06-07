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
	
	private Item[] items;
	
	public GEItemDataGetterWriter(){
		
	}
	
	//1.
	private static boolean checkIfItemDataDumpDirectoryExists(String path) { 
		boolean hadItemDataDumpDirectory = false;
		File currentDirectory = new File(path);
		if (currentDirectory.exists() && currentDirectory.isDirectory()) {
			File[] directoryFiles = currentDirectory.listFiles();
			for (File file: directoryFiles) {
				if (file.getName().contains("ItemDataDump")) {
					System.out.println("Found directory: 'ItemDataDump'. ");
					hadItemDataDumpDirectory = true;
				} 
			}
			if (hadItemDataDumpDirectory) {
				//Do nothing
			} else {
				System.out.println("Didn't find 'ItemDataDump' directory. Creating directory: 'ItemDataDump'. \nThis will be used to store data we find.");
				Path itemDataDumpPath = Paths.get(path, "ItemDataDump");
				try {
					Files.createDirectories(itemDataDumpPath);	
				} catch (IOException e){
					System.err.println(e);
				}
			}
		}
		return hadItemDataDumpDirectory;
	}

	//2.
	private void getAllItemsFromFile() {
		String directory = "ItemDataDump";
		String file = "ItemAvailability.txt";
		Item[] itemsFromFile; 
		String fullDirPath = System.getProperty("user.dir") + "/" + directory;
		System.out.println();
		
		try( BufferedReader br = new BufferedReader(new FileReader(file))) {
			String firstLine = br.readLine();
			if (firstLine == null) {
				System.out.println("Problem: first line null.");
			} else {
				System.out.println(firstLine);
			}
			String line;
			int itemCount = 0;
			while ((line = br.readLine()) != null) {
				itemCount++;
			}
			System.out.printf("\nFound %d items", itemCount);
			itemsFromFile = new Item[itemCount];
			int i = 0;
			while ((line = br.readLine()) != null) {
				String[] lineElements = line.split(",");
				Item lineItem = new Item(lineElements[0], lineElements[2], lineElements[1], lineElements[3]);
				itemsFromFile[i] = lineItem;
			}
			
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
		for (Item item: this.items) {
			
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
				
			} catch (IOException e) {
				System.err.println(" Accessing URL generated error: " + e.getMessage());
			}
		}
	}
		
	//4.
	private void writeItemDataToFile(Item item) {
		try (
				Writer writeToFile = new BufferedWriter(new FileWriter(item.getItemName() +  "DailyData.txt"));	
				){
			StringBuilder itemData = new StringBuilder();
			itemData.append("Item Name, ItemID, ItemURL, ItemTradeableGE");
			itemData.append(item.getItemName() + "," 
					+ item.getItemID() + "," 
					+ item.getURL() + "," 
					+ item.isTradeableGE()
					+ "\n"
					);
			String dailyData = item.getDailyData().toString();
			itemData.append(dailyData);
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
		String cwd = System.getProperty("user.dir");
		//1.
		boolean step1 = checkIfItemDataDumpDirectoryExists(cwd);
		
		//2. 
		GEItemDataGetterWriter data = new GEItemDataGetterWriter();
		data.getAllItemsFromFile();
		
		//3.
		data.accessGEWebsite();
		
		//4. - Called from within accessGEWebsite
		
		
		//5. - haven't written this stage yet
		
	}

}
