
import javafx.application.Application; // Ensure this import is present
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.File; // For file operations
import java.nio.file.Files; // For creating dummy file
import java.nio.file.Paths; // For creating dummy file

public class GEDataController {
	private GEDataModel model;
    private GEDataView view;
	
    // Constructor to connect the Model and View
    public GEDataController(GEDataModel model, GEDataView view) {
        this.model = model;
        this.view = view;
        //TO-DO: add event listeners (e.g. click button in view calling method in Controller)
        
    }
    
    public void loadAndDisplayData(String filePath) {
        try {
            model.loadData(filePath); // Instructs model to load data
            List<GEItem> loadedItems = model.getItems(); // Gets model's loaded data

            if (loadedItems.isEmpty()) {
                System.out.println("No data loaded from file: " + filePath + ". Displaying empty view.");
            } else {
                System.out.println("Data loaded successfully. Number of items: " + loadedItems.size());
            }
        } catch (IOException e) {
            System.err.println("Failed to load data from file: " + filePath + ". Error: " + e.getMessage());
            //TO-DO: work out how to show error dialog here using the view
            // In a real UI, you'd show an error dialog here using the view, e.g., view.showError("...");
        }
    }
    
    //Want to use a map to get the txt file the user chooses: int, String map
    public static Map<Integer, String> getTxtFilesinCWDir() {
    	File file = new File(".");
    	File[] files = file.listFiles();
    	int i = 0;
    	Map<Integer, String> fileNumberNameMap = new HashMap<>();
    	if (files != null) {
    		for (File eachFile: files) {
        		if (eachFile.isFile() && eachFile.getName().endsWith("txt")) {
        			i++;
        			fileNumberNameMap.put(i, eachFile.getName());
        			System.out.println(i + ". " + eachFile.getName());
        		}
        	}
    	} else {
    		System.out.println("No files found.");
    	}
    	return fileNumberNameMap;
    }
    
    public static void main(String[] args) {
        System.out.println("--- GE Data Visualiser ---");
    	// Define your data file path
        Scanner s = new Scanner(System.in);
    	Map<Integer, String> fileMappings = getTxtFilesinCWDir();
    	System.out.println("Enter a number to select a file");
        //At the moment no error handling if user puts in something wrong
    	int userChoice = s.nextInt();
        String fileFromChoice = fileMappings.get(userChoice);
        System.out.printf("\nUser selected file %d. %s", userChoice, fileFromChoice );
        s.close();
        String cwDir = System.getProperty("user.dir");
        String dataFileName = fileFromChoice;
        String fullFilePath = cwDir + File.separator + dataFileName;
        
        //GEDataModel.checkCurrentWorkingDirectory(); // Don't really need, very similar method in this class

        // 1. Create Model, View, Controller instances:
        GEDataModel model = new GEDataModel();
        // 2. Instantiate the View (it will be populated later)
        GEDataView view = new GEDataView();

        // 3. Instantiate the Controller, connecting Model and View
        GEDataController controller = new GEDataController(model, view);

        // 4. Controller orchestrates: Load data and pass it to the View
        controller.loadAndDisplayData(fullFilePath);

        // 5. Launch the JavaFX Application (this must be the last step in main)
        // We use a helper method in GEDataView to launch and pass the instance/data.
        GEDataView.launchApp(args, model.getItems()); // Pass the loaded data
    }
}


