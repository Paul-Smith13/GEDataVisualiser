import pandas as pd
import os 
import altair as alt
from pathlib import Path
import re


def all_Files_in_CWD():
    cwd = os.getcwd()
    print(cwd)

    all_entries = os.listdir(cwd)
     

    files_found = []
    for entry in all_entries:
        full_path = os.path.join(cwd, entry)
        if os.path.isfile(full_path):
            files_found.append(entry)
            print(f"-{entry}")

    if not files_found:
        print("No files found.")

def find_specific_file_CWD(find_File):
    #cwd = os.getcwd()
    cwd = Path.cwd()

    print(cwd)

    all_entries = os.listdir(cwd)
    target_file_path = cwd / find_File

    if target_file_path.is_file():
        print(f"Found {find_File}")
        return True
    elif target_file_path.exists() and target_file_path.is_dir:
        print(f"Problem {find_File} is a directory, not a file.")
        return False
    else:
        print(f"{find_File} was not found in {cwd}.")
        return False

def read_And_Analyse_File(found_File):
    ITEM_PATTERN = re.compile(r',(\d{1,5}),(https?://[^,]+),(true|false)\s*$', re.IGNORECASE)
    cwd = os.getcwd()
    file_path = os.path.join(cwd, found_File)

    print(f"Current working directory: {cwd}")

    if not os.path.isfile(found_File):
        print(f"Error: {found_File} not found at {file_path}.")
        return

    data = []

    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            next(f)    

            for line_number, line in enumerate(f, start=2):
                line = line.strip()
                if not line:
                    continue
                
                match = ITEM_PATTERN.search(line)

                if match:
                    item_id_str = match.group(1)
                    item_url = match.group(2)
                    found_on_ge_str = match.group(3)
                    item_name = line[:match.start(1)-1].strip()

                    try:
                        item_id = int(item_id_str)
                        found_on_ge = (found_on_ge_str).lower() == 'true'                    
                        data.append({
                            "item_name": item_name,
                            "item_id": item_id,
                            "item_url": item_url,
                            "found_on_ge": found_on_ge
                        })
                    except ValueError:
                        print(f"Problem: couldn't convert item ID or boolean on line {line_number}: '{line}' ")
                else:
                    print(f"ERROR: line {line_number} doesn't match our expected format: '{line}'")

        if not data:
            print("No valid data found while parsing.")
            return

        df = pd.DataFrame(data)
        
        print("\n First 5 of data:")
        print(df.head())
        print("Data types:")
        print(df.dtypes)

        summary_df = df['found_on_ge'].value_counts().reset_index(name='count')
        summary_df.columns = ['Found on GE', 'Count']
        total_count = summary_df['Count'].sum()
        summary_df['Percentage'] = (summary_df['Count'] / total_count) *100
        print("\n Summary data for Chart")
        print(summary_df)

        base = alt.Chart(summary_df).encode(
            x = alt.X('Found on GE:N', title = '% Items Successfully Found on GE Website'),
            y = alt.Y('Count:Q', title = 'No. of Items')
        )

        bars = base.mark_bar().encode(
            color = alt.Color(
                'Found on GE:N',
                scale = alt.Scale(domain=[False, True], range = ['red', 'green']),
                legend=alt.Legend(title="Item Found on GE Status")
            ),
            tooltip=['Found on GE', 'Count', alt.Tooltip('Percentage:Q', format='.1f')]
        )

        text_labels = base.mark_text(
            align = 'center',
            baseline = 'bottom',
            dy = -5,
            dx = 0
        ).encode(
            text=alt.Text('Percentage:Q', format = '.1f'),
            color = alt.value('black')
        )

        bar_chart_with_percentages = (bars + text_labels).properties(
            title = 'Item Availability on GE Website'
        ).interactive()

        pie_chart_base = alt.Chart(summary_df).encode(
            theta = alt.Theta("Percentage", stack=True),
            color = alt.Color("Found on GE:N",
                              scale = alt.Scale(domain=[False, True], range = ['red', 'green']),
                              legend = alt.Legend(title = "Item Found on GE Status")
                              )
        )

        pie_arcs = pie_chart_base.mark_arc(outerRadius= 120).encode(
            tooltip=['Found on GE', 'Count', alt.Tooltip('Percentage:Q', format = '.1f')]
        )

        pie_text = pie_chart_base.mark_text(radius=140).encode(
            text = alt.Text("Percentage:Q", format = ".1f"),
            order = alt.Order("Percentage", sort = "descending"),
            color = alt.value("black")
        )

        final_pie_chart = (pie_arcs + pie_text).properties(
            title = ("% of Items Found on GE Website")
        ).interactive()

        """
        chart = alt.Chart(summary_df).mark_bar().encode(
            x = alt.X('Found on GE:N', title = "Items successfully requested from Official GE Website"),
            y = alt.Y('Count:Q', title = 'No. of Items'),
            tooltip = ['Found on GE', 'Count']
        ).properties(
            title = 'Item Availability on Official GE Website'
        ).interactive()
        """
                
        combined_charts = bar_chart_with_percentages & final_pie_chart
        combined_output_file = "ItemAvailabilitySummaryCharts.html"
        combined_charts.save(combined_output_file)

        print(f"\nSaved altair chart to: {os.path.join(cwd, combined_output_file)}")
        print("Chart is HTML, view in browser.")

    except pd.errors.EmptyDataError:
        print(f"Empty data error: {found_File} either header only or empty.")
    except FileNotFoundError:
        print(f"File not found error: {found_File} wasn't found.")
        print(f"Double check because main call of methods should prevent this.")
    except Exception as e:
        print(f"Unexpected error {e}")
        import traceback
        traceback.print_exc()

#Equivalent of main
print("-" * 30)
print("")

find_File = "ItemsGEAvailability.txt"

all_Files_in_CWD()
if find_specific_file_CWD(find_File) == True:
    read_And_Analyse_File(find_File)
else: 
    print("File not found exiting.")