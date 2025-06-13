import pandas as pd
import os
import altair as alt
from pathlib import Path
import re
import datetime

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

#Equivalent of main method
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