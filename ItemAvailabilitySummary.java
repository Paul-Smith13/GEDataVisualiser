import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

//Just a quick summary script so I know the scale of items I'm not finding

public class ItemAvailabilitySummary {

	public static void itemAvailabilitySummary(String file) {
		try( BufferedReader br = new BufferedReader(new FileReader(file))) {
			String firstLine = br.readLine();
			if (firstLine == null) {
				System.err.println("Potential error: first item null.");
			} else {
				System.out.println(firstLine);
			}
			String line;
			int itemCount = 0;
			int itemsAccessible = 0;
			while ((line = br.readLine()) != null) {
				String[] lineElements = line.split(",");
				if (lineElements[3].contains("true")) {
					itemsAccessible++;
				}
				itemCount++;
			}
			System.out.printf("File contains %d items, of which %d were found on the official GE Website.", itemCount, itemsAccessible);
			float accessiblePercent = ((float) itemsAccessible/itemCount)*100;
			System.out.println("This represents " + accessiblePercent + "%  of the total items.");
		} catch (IOException e) {
			e.getStackTrace();
		} 
	}
	
	public static void main(String[] args) {
		System.out.println("--- Item Availability Summary ---");
		String fileName = "ItemsGEAvailabilityTest.txt";
		
		itemAvailabilitySummary(fileName);
		
	}
	
}
