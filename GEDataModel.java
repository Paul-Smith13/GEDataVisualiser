//Handles data
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GEDataModel {
	private List<GEItem> items;
	private String data;
	private String dataLocation;
	
	//Default blank constuctor
	public GEDataModel() {
		this.data = "";
		this.items = new ArrayList<>();
		this.dataLocation = "";
	}
	//Constructor with values
	public GEDataModel(String data, String fileLocation) {
		this.data = data;
		this.dataLocation = fileLocation;
		this.items = new ArrayList<>();
	}
	//Constructor with all values
	public GEDataModel(String data, String fileLocation, ArrayList<GEItem> geItems) {
		this.data = data;
		this.dataLocation = fileLocation;
		this.items = geItems;
	}
	
	public void loadData(String filePath) throws IOException {
		try (BufferedReader bufferedRead = new BufferedReader(new FileReader(filePath))){
			String fileLine = "";
			GEItem currentItem = null;
			Pattern itemPattern = Pattern.compile("^Item: (.+?),\\s*$");
			Pattern urlPattern = Pattern.compile("^URL: (.+)$");
			Pattern dataPattern = Pattern.compile("^Date: (\\d{4}/\\d{2}/\\d{2}) trendPoint: (\\d+) dailyAverage: (\\d+) dailyVolume: (\\d+),?\\s*$");
			
			//While loop to read the lines in the file & get what we need
			while ((fileLine = bufferedRead.readLine()) != null){
				String lineTrimmed = fileLine.trim();
				if (lineTrimmed.startsWith("|")) {
					lineTrimmed = lineTrimmed.substring(1).trim();
				}
				
				if (lineTrimmed.isBlank() || lineTrimmed.equals("Data: ")) {
					continue;
				}
				
				//Check for item in line - probably a better way to separate this to only check at start
				Matcher itemMatcher = itemPattern.matcher(lineTrimmed);
				if (itemMatcher.matches()) {
					if (currentItem != null) {
						items.add(currentItem);
					}
					currentItem = new GEItem();
					currentItem.setItemName(itemMatcher.group(1).trim());
					continue;
				}
				//Check for URL in line - probably a better way to separate this to only check at start
				Matcher urlMatcher = urlPattern.matcher(lineTrimmed);
				if (urlMatcher.matches()) {
					if (urlMatcher.matches() && currentItem != null) {
						currentItem.setURL(urlMatcher.group(1).trim());
						continue;
					}
				}
				
				//Check for Data in each line
				Matcher dataMatcher = dataPattern.matcher(lineTrimmed);
				if (dataMatcher.matches() && currentItem != null) {
					//Specific ordering dependent upon how they appear in above variable dataPattern (i.e. the file) and how I've set up the constructor
					GEItemDailyData dailyData = new GEItemDailyData(
							dataMatcher.group(1),
							currentItem.getItemName(),
							dataMatcher.group(3),
							dataMatcher.group(2),
							dataMatcher.group(4)
							);
					currentItem.addData(dailyData);
					continue;
				}
				System.out.println("Warning: didn't recognise this line's format: " + fileLine);
				
			}
			if (currentItem != null) {
				items.add(currentItem);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	//Used for seeing what files in the current working directory
	public static void checkCurrentWorkingDirectory() {
		String cwDir = System.getProperty("user.dir");
		File currentDirectory = new File(cwDir);

		if (currentDirectory.exists() && currentDirectory.isDirectory()) {
			File[] directoryFiles = currentDirectory.listFiles();
			if (directoryFiles != null) {
				System.out.println("\t--- Files & Directories in this directory ---");
				int fileAndFolders = 0;
				for (File file: directoryFiles) {
					if (file.isFile()) {
						fileAndFolders++;
						System.out.println("Found file: " + file.getName());
					} else if(file.isDirectory()) {
						fileAndFolders++;
						System.out.println("Found directory: " + file.getName());
					}	
				}
				if (fileAndFolders == 0) {
					System.out.println("No files or directories were found.");
				}
			}
		} else {
			System.err.println("Error: either didn't find this exact directory.");
		}
		
	}
	
	public List<GEItem> getItems() {
	    return items;
	}
	
	public boolean checkForFile(String file) {
		File checkFile = new File(file);
		if (checkFile.exists()) {
			return true;
		}
		return false;
	}
	
	/*
	public static void main(String[] args) {
		String cwDir = System.getProperty("user.dir");
		System.out.println(cwDir);
		checkCurrentWorkingDirectory();
		GEDataModel geDM = new GEDataModel();
		try {geDM.loadData("outputAether rune.txt");} catch (IOException e) {System.err.println(e);}

	}
	*/
	
}
