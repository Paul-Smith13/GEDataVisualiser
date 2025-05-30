import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class GEItemDailyData {
	private String itemName;
	private int dailyAvgPrice;
	private int trendPoint;
	private long dailyVolume;
	private LocalDate date;
	
	//Constructors
	public GEItemDailyData() {
		
	}
	public GEItemDailyData(LocalDate date, String itemName, int dailyPrice, int dailyTrend, long dailyVolume) {
		this.date = date;
		this.itemName = itemName;
		this.dailyAvgPrice = dailyPrice;
		this.trendPoint = dailyTrend;
		this.dailyVolume = dailyVolume;
	}
	public GEItemDailyData(String date, String itemName, String dailyPrice, String dailyTrend, String dailyVolume) {
		try {
			this.date = LocalDate.parse(date.replace("/", "-"));
		} catch (DateTimeParseException e) {
			System.err.println("Couldn't parse date: " + date + ". Setting to 'null', please check if valid.");
			this.date = null;
		}
		try {
			this.itemName = itemName;
			this.dailyAvgPrice = Integer.parseInt(dailyPrice);
			this.trendPoint = Integer.parseInt(dailyTrend);
			this.dailyVolume = Long.parseLong(dailyVolume);
		} catch (NumberFormatException e) {
			System.err.println("Couldn't parse number data for " + this.itemName + " on date " + this.date );
			this.dailyAvgPrice = 0;
			this.trendPoint = 0;
			this.dailyVolume = 0L;
		}
		
	}
	
	@Override
	public String toString() {
		return "Date: " + date + ", Trend Point: " + this.trendPoint + ", Daily Average Price:" + this.dailyAvgPrice 
				+ ", Daily Volume: " + dailyVolume; 
	}
	
	//Getters
	public String getItemName() {
		return this.itemName;
	}
	public int getDailyAvgPrice() {
		return this.dailyAvgPrice;
	}
	public int getTrendPoint() {
		return this.trendPoint;
	}
	public long getDailyVolume() {
		return this.dailyVolume;
	}
	public LocalDate getDate() {
		return this.date;
	}
	
	//Setters
	public void setItemName(String name) {
		this.itemName = name;
	}
	public void setDailyAvgPrice(int dailyPrice) {
		this.dailyAvgPrice = dailyPrice;
	}
	public void setTrendPoint(int trendPoint) {
		this.trendPoint = trendPoint;
	}
	public void setDailyVolume(long volume) {
		this.dailyVolume = volume;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
	
	
}
