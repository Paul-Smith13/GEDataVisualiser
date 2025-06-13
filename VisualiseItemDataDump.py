import pandas as pd
import os
import altair as alt
from pathlib import Path
import re
import datetime

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
        maximum_length = 0
        if self.dates:
            maximum_length = max(maximum_length, len(self.dates))
        if self.dailyAvgPrices:
            maximum_length = max(maximum_length, len(self.dailyAvgPrices))
        if self.trendPrices:
            maximum_length = max(maximum_length, len(self.trendPrices))
        if self.volumes:
            maximum_length = max(maximum_length, len(self.volumes))
        
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
        df.info()
        df.head()
        try:
            df['Date'] = pd.to_datetime(df['Date'])
        except Exception as e:
            print(f"ERROR: {e}")
            return None
        if series_name not in df.columns:
            print(f"ERROR: in {self.file_name} couldn't find {series_name}")
            return None
        
        df[series_name] = pd.to_numeric(df[series_name], errors='coerce')
        print(f"\n--- DEBUG for '{self.file_name}' and series '{series_name}' ---")
        print(f"  Data type of 'Date' column: {df['Date'].dtype}")
        print(f"  Count of NaT values in 'Date': {df['Date'].isna().sum()}")
        print(f"  Data type of '{series_name}' column after pd.to_numeric: {df[series_name].dtype}")
        print(f"  Count of NaN values in '{series_name}': {df[series_name].isna().sum()}")
        print(f"  First 5 values of '{series_name}' column: {df[series_name].head(5).tolist()}")
        print(f"  Total rows in DataFrame (before dropping NaNs): {len(df)}")
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

#Loop in current directory to check if we have ItemDataDump created from earlier process
def all_Dirs_in_ItemDataDump():
    cwd = os.getcwd()
    all_cwd_entries = os.listdir(cwd)

    directories_found = []
    for entry in all_cwd_entries:
        entry_path = os.path.join(cwd, entry)
        if os.path.isdir(entry_path):
            print(f"{entry} is a directory")
            directories_found.append(entry)

    for dir in directories_found:
        if os.path.join(cwd, dir).endswith("ItemDataDump"):
            ItemDataDump = os.path.join(cwd, dir)    
            print(f"{dir} found")
            return True

#Loops over all files in ItemDataDump directory, nonCSVs just for curiousity/debugging
def get_all_ItemDataDumpCSVs():
    cwd = os.getcwd()
    ItemDataDumpDir = os.path.join(cwd, "ItemDataDump")
    #all_DD_Files = os.listdir(ItemDataDumpDir)
    ItemDataDumpFiles = os.listdir(ItemDataDumpDir)

    #num_files_to_process = 100
    #ItemDataDumpFiles = all_DD_Files[:num_files_to_process]

    CSVs_Found = []
    nonCSVs_Found = []
    for file in ItemDataDumpFiles:
        #print(f"Checking {file} (repr: {repr(file)})")
        print(f"Original repr: {repr(file)}")
        cleaned_name = ""
        try:
            normalised_name_bytes = file.encode('utf-8', errors='ignore').decode('utf-8', errors='ignore')
            cleaned_name = normalised_name_bytes.strip()
            print(f"Cleaned representation (after encode/decode/stripping): {repr(cleaned_name)}")
            path_for_file = Path(cleaned_name)
            suffix_lower = path_for_file.suffix.lower()
            print(f"Pathlib suffix: {repr(path_for_file.suffix)}")
            print(f"Lowercase suffix: {repr(suffix_lower)}")
            if path_for_file.suffix.lower() == ".csv":
                CSVs_Found.append(file)
                #print(f"CSV file {file} was found.")
            else:
                nonCSVs_Found.append(file)
                print(f"Non-CSV file {file} detected.")
                for i, char in enumerate(cleaned_name):
                    print(f" Char {i}: {repr(char)} (ord: {ord(char)})")
        except Exception as e:
            print(f"ERROR: {file}: {e}")
            nonCSVs_Found.append(file)

    #print(f"CSVs_Found length: {len(CSVs_Found)}")
    #for csv_File in CSVs_Found:
    #    print(f"Found the following {csv_File}")

    # In case any non-CSV files somehow make their way into this directory:
    print(f"Non-CSV Found length: {len(nonCSVs_Found)}. Found the following non-CSV files: ")
    for file in nonCSVs_Found:
        print(f"{file}")
    print("End of non-CSVs.")
    return CSVs_Found

def data_CSVs_Found(CSVs_Found):
    cwd = os.getcwd()
    ItemDataDumpDir = os.path.join(cwd, "ItemDataDump")
    num_CSVs = len(CSVs_Found)
    item_data_dump_path = Path(cwd) / "ItemDataDump"

    file_size_map = {}

    for CSV in CSVs_Found:
        full_file_path = item_data_dump_path / CSV
        if full_file_path.is_file():
            try:
                size_in_bytes = full_file_path.stat().st_size
                file_size_map[CSV] = size_in_bytes
            except OSError as e:
                print(f"ERROR: {e}")
            except Exception as e:
                print(f"ERROR: {e}")
        else:
            print(f"CHECK: CSV file {CSV} wasn't found at {full_file_path}")
    
    empty_CSVs = {}
    data_CSVs = {}
    for key, value in file_size_map.items():
        #If a file is empty, it will have size of 63 bytes
        if value <= 63:
            empty_CSVs[key] = value
        #Otherwise return mapping of filenames w/ their sizes    
        else:
            data_CSVs[key] = value
            #print(f"{key} : {size_in_bytes}")

    #print(f"Empty CSVs: {empty_CSVs.items()}")
    #print(f"Data CSVs: {data_CSVs.items()}")
    return data_CSVs

def visualise_CSV_data_scope(allCSVs):
    cwd = Path.cwd()
    path_ItemDataDump = cwd / "ItemDataDump"
    CSVs_processed = 0
    empty_CSVs = 0
    if not isinstance(path_ItemDataDump, Path):
        path_ItemDataDump = Path(path_ItemDataDump)
    processed_CSVs = []

    for CSV in allCSVs:
        csv_file_path = path_ItemDataDump / CSV
        if csv_file_path.is_file():
            try:
                file_size = csv_file_path.stat().st_size
                if file_size <= 63:
                    print(f"{CSV} was empty. Skipping")
                    empty_CSVs +=1
                    continue

                df = pd.read_csv(csv_file_path, encoding = 'utf-8')
                df.columns = df.columns.str.strip()
                print(f"\n--- DEBUG (FROM visualise_CSV_data_scope) for '{CSV}' ---")
                print("  DataFrame info *immediately after reading CSV*:")
                df.info(verbose=True, show_counts=True)
                print("  First 5 rows of DataFrame *immediately after reading CSV*:")
                print(df.head().to_string())
                print("--- END DEBUG PRINTS ---")

                #Data from file
                dates = df['Date'].tolist() if 'Date' in df.columns else []
                prices = df['Daily Average Price'].tolist() if 'Daily Average Price' in df.columns else []
                trends = df['Trend Points'].tolist() if 'Trend Points' in df.columns else []
                volumes = df['Daily Volume'].tolist() if 'Daily Volume' in df.columns else []
                #Get file data
                file_name = CSV
                last_modified = csv_file_path.stat().st_mtime
                last_updated_date = datetime.datetime.fromtimestamp(last_modified).strftime('%Y-%m-%d %H:%M:%S')
                #Instantiate CSV object
                item_object = itemCSV(
                    name=file_name,
                    size=file_size,
                    updated=last_updated_date,
                    dateSeries=dates,
                    priceSeries=prices,
                    trendSeries=trends,
                    volumeSeries=volumes
                )
                processed_CSVs.append(item_object)
                CSVs_processed += 1
            except pd.errors.EmptyDataError:
                print(f"ERROR: empty data error for {allCSVs}")
            except FileNotFoundError:
                #Unlikely to happen, given file already found
                print(f"ERROR: file note found for {allCSVs}")
            except Exception as e:
                print(f"Unexpected error: {e}")
                import traceback
                traceback.print_exc()
    all_CSVs_Count = CSVs_processed + empty_CSVs
    print(f"Was able to process {CSVs_processed}, missing {empty_CSVs}.")
    visualised_data = pd.DataFrame({
        'Category': ['CSVs with Data', 'Empty/Skipped CSVs'],
        'Count': [CSVs_processed, empty_CSVs]
    })
    # % for pie-chart
    visualised_data['Percentage'] = (visualised_data['Count'] / all_CSVs_Count * 100).round(2)
    visualised_data['Percentage Label'] = visualised_data['Percentage'].astype(str) + '%'
    bar_chart = alt.Chart(visualised_data).mark_bar().encode(
        x = alt.X('Category:N', axis = alt.Axis(title = None)),
        y = 'Count:Q',
        color = alt.Color('Category:N', legend=alt.Legend(title="CSV Data Scope", orient="bottom")),
        tooltip = ['Category', 'Count']
    ).properties(
        title = 'Total CSVs: Data vs. Empty'
    )

    text_bar = bar_chart.mark_text(
        align = 'center',
        baseline = 'bottom',
        dy =-5
    ).encode(
        text = 'Count:Q',
        color = alt.value('black')
    )
    combined_bar_chart = (bar_chart + text_bar)

    #PieChart
    pie_chart = alt.Chart(visualised_data).mark_arc(outerRadius=120).encode(
        theta = alt.Theta(field = "Count", type = "quantitative"),
        color = alt.Color("Category:N", legend = alt.Legend(title = "CSV Status", orient= "bottom")),
        order = alt.Order("Percentage", sort="ascending"),
        tooltip= ["Category", "Count", alt.Tooltip("Percentage", format = ".1f", title = "Percentage")]
    )
    text_pie = pie_chart.mark_text(radius=140).encode(
        text='Percentage Label',
        order = alt.Order("Percentage", sort = "descending"),
        color = alt.value('black')
    )

    combined_pie_chart = (pie_chart + text_pie)
    final_chart = alt.hconcat(combined_bar_chart, combined_pie_chart).resolve_legend(
        color = "independent"
    ).properties(
        title = f"CSV Data Summary for ItemDataDump (Total Files: {all_CSVs_Count})"
    )
    try:
        output_name = "CSV_Data_Scope.html"
        final_chart.save(output_name)
        print(f"Saved output to {output_name}")
    except Exception as e:
        print(f"ERROR: {e}") 

    return processed_CSVs

# Equivalent of main
# Want to check if we need to run earlier stage
if all_Dirs_in_ItemDataDump() != True:
    print(f"ERROR: 'ItemDataDump' directory not found. Ensure GEItemDataGetterWriter.java has been ran.")
    quit()
else:
    print(f"Directory 'ItemDataDump' found. ")

#Get all CSV Files
allCSVs = get_all_ItemDataDumpCSVs()
#Debug print
print(f"Found CSVs: {get_all_ItemDataDumpCSVs()}")
#Get just CSVs with data
#dataCSVs = {data_CSVs_Found(get_all_ItemDataDumpCSVs())}
#Next want visualise scope of data coverage
visualise_CSV_data_scope(allCSVs)

dataCSVs = visualise_CSV_data_scope(allCSVs)
if dataCSVs:
    test_CSV = dataCSVs[650]
    line_chart_for_CSV = test_CSV.generate_linegraph('Daily Average Price')
    if line_chart_for_CSV:
        test_output_filename = f"{test_CSV.file_name.replace('.csv', '')}_Price_LineGraph.html"
        line_chart_for_CSV.save(test_output_filename)
        print("Saved line graph for test as html file")
    else:
        print(f"ERROR: failed to generate linegraph")
else:
    print("No Data CSVs were successfully processed.")