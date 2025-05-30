
import javafx.application.Application; // Ensure this import is present
import java.io.IOException;
import java.util.List;
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
        // In a more complex application, you would set up event listeners here
        // (e.g., a button click in the view calling a method in the controller).
    }
    
    public void loadAndDisplayData(String filePath) {
        try {
            model.loadData(filePath); // Tell the model to load data
            List<GEItem> loadedItems = model.getItems(); // Get the loaded data from the model

            if (loadedItems.isEmpty()) {
                System.out.println("No data loaded from file: " + filePath + ". Displaying empty view.");
                // view.setItemsToDisplay(loadedItems); // REMOVE THIS LINE
            } else {
                System.out.println("Data loaded successfully. Number of items: " + loadedItems.size());
                // view.setItemsToDisplay(loadedItems); // REMOVE THIS LINE
                // The actual JavaFX application launch is handled in the main method after this call.
            }
        } catch (IOException e) {
            System.err.println("Failed to load data from file: " + filePath + ". Error: " + e.getMessage());
            // In a real UI, you'd show an error dialog here using the view, e.g., view.showError("...");
        }
    }
    
    public static void main(String[] args) {
        // Define your data file path
        String cwDir = System.getProperty("user.dir");
        String dataFileName = "outputAether rune.txt";
        String fullFilePath = cwDir + File.separator + dataFileName;

        System.out.println("--- Application Start ---");
        GEDataModel.checkCurrentWorkingDirectory(); // Useful for debugging file paths

        // --- Optional: Create a dummy data file if it doesn't exist for testing ---
        // This ensures the application has data to load if you run it without the file.
        try {
            File dummyFile = new File(fullFilePath);
            if (!dummyFile.exists()) {
                System.out.println("\nCreating dummy data file for testing: " + fullFilePath);
                Files.write(Paths.get(fullFilePath),
                        ("|Item: Zulrah's scales,\n" +
                         "URL: https://secure.runescape.com/m=itemdb_oldschool/Zulrah%27s+scales/viewitem?obj=12934\n" +
                         "Data: \n" +
                         "Date: 2025/04/26 trendPoint: 253 dailyAverage: 256 dailyVolume: 89461794,\n" +
                         "Date: 2025/04/27 trendPoint: 253 dailyAverage: 255 dailyVolume: 56014633,\n" +
                         "Date: 2025/04/28 trendPoint: 253 dailyAverage: 253 dailyVolume: 84895340,\n" +
                         "Date: 2025/04/29 trendPoint: 253 dailyAverage: 252 dailyVolume: 40576830,\n" +
                         "Date: 2025/04/30 trendPoint: 253 dailyAverage: 251 dailyVolume: 72220574,\n" +
                         "|Item: Abyssal whip,\n" +
                         "URL: https://secure.runescape.com/m=itemdb_oldschool/Abyssal+whip/viewitem?obj=4151\n" +
                         "Data: \n" +
                         "Date: 2025/04/26 trendPoint: 200 dailyAverage: 205 dailyVolume: 12345678,\n" +
                         "Date: 2025/04/27 trendPoint: 201 dailyAverage: 206 dailyVolume: 9876543,\n"
                        ).getBytes());
                System.out.println("Dummy data file created/overwritten.\n");
            } else {
                System.out.println("\nUsing existing data file: " + fullFilePath + "\n");
            }
        } catch (IOException e) {
            System.err.println("Could not create dummy file: " + e.getMessage());
        }
        // --- End of dummy file creation ---

        // 1. Instantiate the Model
        GEDataModel model = new GEDataModel();
        // 2. Instantiate the View (it will be populated later)
        GEDataView view = new GEDataView(); // Default constructor

        // 3. Instantiate the Controller, connecting Model and View
        GEDataController controller = new GEDataController(model, view);

        // 4. Controller orchestrates: Load data and pass it to the View
        controller.loadAndDisplayData(fullFilePath);

        // 5. Launch the JavaFX Application (this must be the last step in main)
        // We use a helper method in GEDataView to launch and pass the instance/data.
        GEDataView.launchApp(args, model.getItems()); // Pass the loaded data
    }
}


