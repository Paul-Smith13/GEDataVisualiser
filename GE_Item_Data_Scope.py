import pandas as pd
import os
import altair as alt
from pathlib import Path
import re
import datetime
from GE_Item_Data import itemCSV

#File purpose: produce & communicate scope of data covered

#Checks if we have ItemDataDump directory already created
def has_ItemDataDump():
    cwd = os.getcwd()
    all_cwd_entries = os.listdir(cwd)
    for entry in all_cwd_entries:
        entry_path = os.path.join(cwd, entry)
        if os.path.isdir(entry_path):
            if entry == "ItemDataDump":
                print(f"Confirmed we have ItemDataDump directory.")
                return True 
    print("No ItemDataDump directory detected")
    return False

#Gets all CSVs in ItemDataDump directory
def get_all_ItemDataDump_CSVs():
    cwd = Path(os.getcwd())
    item_data_dump_dir = cwd / "ItemDataDump"
    if not has_ItemDataDump():
        print(f"Error: no {item_data_dump_dir} found.")
        return []

    CSVs_Found = []
    nonCSVs_Found = []
    for file in item_data_dump_dir.iterdir(): #Assumption: no handling of subdirectories required
        if file.is_file():
            if file.suffix.lower() == ".csv":
                CSVs_Found.append(file.name)
        else:
            nonCSVs_Found.append(file.name)
    return CSVs_Found

#Gets all CSVs in ItemDataDump which have data
def get_ItemDataDump_dataCSVs():
    cwd = Path(os.getcwd())
    item_data_dump_dir = cwd / "ItemDataDump"
    CSVs_Found = get_all_ItemDataDump_CSVs()
    num_CSVs = len(CSVs_Found)
    file_size_map = {}
    empty_CSVs = {}
    data_CSVs = {}
    for CSV in CSVs_Found:
        full_file_path = item_data_dump_dir / CSV
        if full_file_path.is_file():
            try:
                size_in_bytes = full_file_path.stat().st_size
                file_size_map[CSV] = size_in_bytes
                if size_in_bytes <= 63:
                    empty_CSVs[CSV] = size_in_bytes
                else:
                    data_CSVs[CSV] = size_in_bytes
            except OSError as e:
                print(f"ERROR: {e}")
            except Exception as e:
                print(f"ERROR: {e}")
        else:
            print(f"CHECK: CSV file {CSV} wasn't found at {full_file_path}")
    return data_CSVs
    
#Returns how many CSVs in ItemDataDump
def how_many_ItemDataDump_CSVs():
    return len(get_all_ItemDataDump_CSVs())

#Returns how many CSVs in ItemData Dump have data 
def how_many_dataCSVs_ItemDataDump():
    return len(get_ItemDataDump_dataCSVs())

#Returns small summary of CSVs with data vs all other CSVs detected
def summarise_CSV_data_scope():
    count_ItemDataDump_CSVs = how_many_ItemDataDump_CSVs()
    count_dataCSVs_ItemDataDump = how_many_dataCSVs_ItemDataDump()
    count_emptyCSVs = (count_ItemDataDump_CSVs - count_dataCSVs_ItemDataDump)
    data_CSV_percentage = (count_dataCSVs_ItemDataDump/count_ItemDataDump_CSVs) * 100
    empty_CSV_percentage = ((count_ItemDataDump_CSVs - count_dataCSVs_ItemDataDump) / count_ItemDataDump_CSVs) * 100
    summary_message = f"""\t\t--- Summary ---
    \tCSVs in ItemDataDump: {count_ItemDataDump_CSVs}, 100%
    \tCSVs with data: {count_dataCSVs_ItemDataDump}, {data_CSV_percentage:.2f}% 
    \tCSVs missing data: {count_emptyCSVs}, {empty_CSV_percentage:.2f}%
    """
    return summary_message

def visualise_CSV_data_scope(CSVs: list[str]):

    path_ItemDataDump = Path.cwd() / "ItemDataDump"
    count_CSVs_processed = 0
    count_empty_CSVs = 0
    if not isinstance(path_ItemDataDump, Path):
        path_ItemDataDump = Path(path_ItemDataDump)
    processed_CSVs = []

    for CSV in CSVs:
        csv_file_path = path_ItemDataDump / CSV
        if csv_file_path.is_file():
            try:
                file_size = csv_file_path.stat().st_size
                if file_size <= 63:
                    #print(f"{CSV} was empty. Skipping")
                    count_empty_CSVs +=1
                    continue

                df = pd.read_csv(csv_file_path, encoding = 'utf-8')
                df.columns = df.columns.str.strip()

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
                count_CSVs_processed += 1
            except pd.errors.EmptyDataError:
                print(f"ERROR: empty data error for {CSVs}")
            except FileNotFoundError:
                #Unlikely to happen, given file already found
                print(f"ERROR: file note found for {CSVs}")
            except Exception as e:
                print(f"Unexpected error: {e}")
                import traceback
                traceback.print_exc()
    all_CSVs_Count = count_CSVs_processed + count_empty_CSVs
    print(f"Was able to process {count_CSVs_processed}, missing {count_empty_CSVs}.")
    visualised_data = pd.DataFrame({
        'Category': ['CSVs with Data', 'Empty/Skipped CSVs'],
        'Count': [count_CSVs_processed, count_empty_CSVs]
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
    combined_bar_chart = (bar_chart + text_bar).properties(
        width = 100
    )

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

    #Equivalent of main method
if __name__ == "__main__":
    #Test has_ItemDataDump()
    #print(has_ItemDataDump())

    #Test get_all_ItemDataDump_CSVs()
    #print(get_ItemDataDump_dataCSVs())

    #Test get_ItemDataDump_dataCSVs()
    #print(how_many_ItemDataDump_CSVs())

    #Test how_many_dataCSVs_ItemDataDump()
    #print(how_many_dataCSVs_ItemDataDump())

    #Test summarise_CSV_data_scope()
    print(summarise_CSV_data_scope())

    visualise_CSV_data_scope(get_all_ItemDataDump_CSVs())