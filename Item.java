import java.util.List;
//Used AI to write getters & setters


public class Item {
	private String itemName;
	private String URL;
	private List<GEItemDailyData> dailyData;
	private int itemID;
	private boolean tradeableGE;

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
	
	// --- Getters ---
	public String getItemName() {return itemName;}
	public String getURL() {return URL;}
	public List<GEItemDailyData> getDailyData() {return dailyData;}
	public int getItemID() {return itemID;}
	public boolean isTradeableGE() {return tradeableGE;}

	// --- Setters ---
	public void setItemName(String itemName) {this.itemName = itemName;}
	public void setURL(String URL) {this.URL = URL;}
	public void setDailyData(List<GEItemDailyData> dailyData) {this.dailyData = dailyData;}
	public void setItemID(int itemID) {this.itemID = itemID;}
	public void setTradeableGE(boolean tradeableGE) {this.tradeableGE = tradeableGE;}
}