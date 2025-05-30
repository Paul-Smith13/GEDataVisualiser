import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox; // Used for vertical stacking of charts
import javafx.stage.Stage;
import javafx.scene.chart.BarChart; // NEW: Import for BarChart

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors; // NEW: For collecting data

public class GEDataView extends Application {

    private static List<GEItem> itemsToDisplay;

    public GEDataView() {
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Grand Exchange Item Data Visualization");

        if (itemsToDisplay == null || itemsToDisplay.isEmpty()) {
            primaryStage.setScene(new Scene(new Label("No data available to display."), 400, 200));
            primaryStage.show();
            return;
        }

        GEItem firstItem = itemsToDisplay.get(0);

        // Sort the historical data by date to ensure correct plotting order
        // This is crucial for line and bar charts
        List<GEItemDailyData> sortedHistoricalData = firstItem.getDailyData().stream()
                .sorted((d1, d2) -> d1.getDate().compareTo(d2.getDate()))
                .collect(Collectors.toList());

        // --- CHART 1: Price Trend (LineChart) ---
        final CategoryAxis xAxisPrice = new CategoryAxis();
        xAxisPrice.setLabel("Date");
        final NumberAxis yAxisPrice = new NumberAxis();
        yAxisPrice.setLabel("Price");

        final LineChart<String, Number> priceChart = new LineChart<>(xAxisPrice, yAxisPrice);
        priceChart.setTitle(firstItem.getItemName() + " Price Trends");
        priceChart.setCreateSymbols(false); // Hide symbols by default, can be overridden per series

        // Series 1: Daily Average Price
        XYChart.Series<String, Number> dailyAvgPriceSeries = new XYChart.Series<>();
        dailyAvgPriceSeries.setName("Daily Average Price");

        // Series 2: Trend Point
        XYChart.Series<String, Number> trendPointSeries = new XYChart.Series<>();
        trendPointSeries.setName("Trend Point");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        for (GEItemDailyData dailyData : sortedHistoricalData) {
            if (dailyData.getDate() != null) {
                String formattedDate = dailyData.getDate().format(dateFormatter);

                // Add data to Daily Average Price series
                dailyAvgPriceSeries.getData().add(new XYChart.Data<>(
                    formattedDate,
                    dailyData.getDailyAvgPrice()
                ));

                // Add data to Trend Point series
                trendPointSeries.getData().add(new XYChart.Data<>(
                    formattedDate,
                    dailyData.getTrendPoint()
                ));
            }
        }

        priceChart.getData().addAll(dailyAvgPriceSeries, trendPointSeries); // Add both series to the price chart


        // --- CHART 2: Daily Volume (BarChart) ---
        final CategoryAxis xAxisVolume = new CategoryAxis();
        xAxisVolume.setLabel("Date");
        final NumberAxis yAxisVolume = new NumberAxis();
        yAxisVolume.setLabel("Daily Volume");
        yAxisVolume.setForceZeroInRange(true); // Ensures the Y-axis starts at 0 for bar charts

        final BarChart<String, Number> volumeChart = new BarChart<>(xAxisVolume, yAxisVolume);
        volumeChart.setTitle(firstItem.getItemName() + " Daily Volume");
        volumeChart.setLegendVisible(false); // Typically no legend needed for single volume series
        volumeChart.setCategoryGap(0); // Make bars touch for continuous look
        volumeChart.setBarGap(0);

        XYChart.Series<String, Number> volumeSeries = new XYChart.Series<>();
        volumeSeries.setName("Daily Volume"); // Name for the series (not visible if legend is off)

        for (GEItemDailyData dailyData : sortedHistoricalData) {
            if (dailyData.getDate() != null) {
                volumeSeries.getData().add(new XYChart.Data<>(
                    dailyData.getDate().format(dateFormatter),
                    dailyData.getDailyVolume()
                ));
            }
        }
        volumeChart.getData().add(volumeSeries);
       

        
        // --- Layout the two charts vertically ---
        VBox rootVBox = new VBox(10); // 10px spacing between charts
        rootVBox.getChildren().addAll(priceChart, volumeChart);

        BorderPane root = new BorderPane();
        root.setCenter(rootVBox); // Place the VBox containing both charts in the center

        Scene scene = new Scene(root, 1200, 900); // Increased window size to accommodate two charts
        primaryStage.setScene(scene);
        primaryStage.show();
        dailyAvgPriceSeries.nodeProperty().get().setStyle("-fx-stroke: blue");
        trendPointSeries.nodeProperty().get().setStyle("-fx-stroke: orange");
    }

    public static void launchApp(String[] args, List<GEItem> items) {
        GEDataView.itemsToDisplay = items;
        Application.launch(GEDataView.class, args);
    }
}