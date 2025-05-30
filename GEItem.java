import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GEItem {
	private String itemName;
	private String URL;
	private List<GEItemDailyData> dailyData;
	
	//Constructors
	//Default constructor
	public GEItem() {
		this.itemName = "CHECK - NO NAME";
		this.URL = "CHECK - NO URL";
		this.dailyData = new ArrayList<>();
	}
	//Constructor with for no daily data
	public GEItem(String name, String URL) {
		this.itemName = name;
		this.URL = URL;
		this.dailyData = new ArrayList<>();
	}
	//Constructor with daily data
	public GEItem(String name, String URL, ArrayList<GEItemDailyData> itemDailyData) {
		this.itemName = name;
		this.URL = URL;
		this.dailyData = itemDailyData;
	}
	
	//Methods
	public void addData(GEItemDailyData data) {
		this.dailyData.add(data);
	}
	public List<LocalDate> getDates(){
		List<LocalDate> dates = new ArrayList<>();
		for (GEItemDailyData dailyData: this.dailyData) {
			if (dailyData.getDate() != null) {
				dates.add(dailyData.getDate());
			}
		}
		return dates;
	}
	public List<Integer> getDailyAvgPrices() {
		List<Integer> prices = new ArrayList<>();
		for (GEItemDailyData data: this.dailyData) {
			prices.add(data.getDailyAvgPrice());
		}
		
		return prices;
	}
	public List<Integer> getTrendPoints() {
		List<Integer> trendPoints = new ArrayList<>();
		for (GEItemDailyData data: this.dailyData) {
			trendPoints.add(data.getTrendPoint());
		}
		return trendPoints;
	}
	
	public List<Long> getDailyVolumes() {
		List<Long> volumes = new ArrayList<>();
		for (GEItemDailyData data: this.dailyData) {
			volumes.add(data.getDailyVolume());
		}
		return volumes;
	}
	@Override
	public String toString() {
		String toString = "Item name: " + this.itemName + ", URL: " + this.URL + ", Number of Daily Data Points: " + this.dailyData.size();
		return toString;
	}
	
	//Basic Attribute Getters
	public String getItemName() {return this.itemName;}
	public String getURL() {return this.URL;}
	public List<GEItemDailyData> getDailyData(){return this.dailyData;}
	public Optional<GEItemDailyData> getDataOnDate(LocalDate date) {
		return this.dailyData.stream()
				.filter(data -> data.getDate() != null && data.getDate().equals(date))
				.findFirst();
	}
	
	//Basic Attribute Setters
	public void setItemName(String name) {this.itemName = name;}
	public void setURL(String url) {this.URL = url;}
	public void setDailyData(List<GEItemDailyData> itemDailyData) {this.dailyData = itemDailyData;}
	
}
