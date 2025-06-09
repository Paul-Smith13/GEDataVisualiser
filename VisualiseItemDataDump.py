import pandas as pd
import os
import altair as alt
from pathlib import Path
import re

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

    print(f"CSVs_Found length {len(CSVs_Found)}:")
    for csv_File in CSVs_Found:
        print(f"Found the following {csv_File}")

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

def visualise_CSV_data_scope(CSVs):
    


    return 

# Thinking about a function to return largest changes over period


def get_series_highest(CSV):
    return

def get_series_lowest(CSV):
    return

def get_Daily_Price_Series_From_File(CSV):
    return

def get_Daily_Trend_Series_From_File(CSV):
    return

def get_Daily_Volume_Series_From_File(CSV):
    return



# Equivalent of main
# Want to check if we need to run earlier stage
if all_Dirs_in_ItemDataDump() != True:
    print(f"ERROR: 'ItemDataDump' directory not found. Ensure GEItemDataGetterWriter.java has been ran.")
    quit()
else:
    print(f"Directory 'ItemDataDump' found. ")




#Debug print
print(f"Found CSVs: {get_all_ItemDataDumpCSVs()}")
#Get all CSV Files
dataCSVs = {data_CSVs_Found(get_all_ItemDataDumpCSVs())}
#Next want visualise scope of data coverage
visualise_CSV_data_scope(dataCSVs)
