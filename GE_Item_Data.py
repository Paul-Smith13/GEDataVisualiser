import pandas as pd
import altair as alt

#File primary use: create itemCSV objects
#itemCSV objects purpose: take in CSV data for visualisation and data analysis
class itemCSV:
    def __init__(self, name, size, updated, dateSeries, priceSeries, trendSeries, volumeSeries):
        self.file_name = name
        self.file_size = size
        self.last_updated = updated
        self.dates = dateSeries
        self.dailyAvgPrices = priceSeries
        self.trendPrices = trendSeries
        self.volumes = volumeSeries

    # Thinking about a function to return largest changes over period
    def get_series_highest(self, data_series):
        return max(data_series)

    def get_series_lowest(self, data_series):
        return min(data_series)

    def get_Daily_Price_Series_From_File(self):
        return self.dailyAvgPrices

    def get_Daily_Trend_Series_From_File(self):
        return self.trendPrices

    def get_Daily_Volume_Series_From_File(self):
        return self.volumes

    def generate_linegraph(self, series_name: str):
        all_series = [self.dates, self.dailyAvgPrices, self.trendPrices, self.volumes]
        data_series_lengths = [len(s) for s in all_series if s is not None and len(s)>0]

        maximum_length = max(data_series_lengths) if data_series_lengths else 0
        if maximum_length == 0:
            print(f"ERROR: no data detected for {self.file_name}")
            return None
                        
        padded_dates = list(self.dates) + [None] * (maximum_length - len(self.dates)) if self.dates is not None else [None] * maximum_length     
        padded_dailyAvgPrices = list(self.dailyAvgPrices) + [None] * (maximum_length - len(self.dailyAvgPrices)) if self.dailyAvgPrices is not None else [None] * maximum_length     
        padded_trendPrices = list(self.trendPrices) + [None] * (maximum_length - len(self.trendPrices)) if self.trendPrices is not None else [None] * maximum_length     
        padded_volumes = list(self.volumes) + [None] * (maximum_length - len(self.volumes)) if self.volumes is not None else [None] * maximum_length     

        data = {
            'Date': padded_dates,
            'Daily Average Price': padded_dailyAvgPrices,
            'Trend Points': padded_trendPrices,
            'Daily Volume': padded_volumes
        }
        df = pd.DataFrame(data)

        try:
            df['Date'] = pd.to_datetime(df['Date'])
        except Exception as e:
            print(f"ERROR: {e}")
            return None
        if series_name not in df.columns:
            print(f"ERROR: in {self.file_name} couldn't find {series_name}")
            return None
        
        df[series_name] = pd.to_numeric(df[series_name], errors='coerce')

        line_chart = alt.Chart(df).mark_line().encode(
            x = alt.X('Date:T', title = 'Date'),
            y = alt.Y(series_name + ':Q', title=series_name),
            tooltip = [
                alt.Tooltip('Date:T', format='%Y-%m-%d'),
                alt.Tooltip(series_name + ':Q', format = '.2f')
            ]
        ).properties(
            title=f"{series_name} for {self.file_name.replace('.csv', '')}"
        ).interactive()

        return line_chart