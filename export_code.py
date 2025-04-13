import os

# Absolute paths
java_dir = r"src\main\java"
fxml_dir = r"src\main\resources"
output_file_path = r"C:\Users\prana\Downloads\Temple Misc\My Whole Code.txt"

with open(output_file_path, "w", encoding="utf-8") as output:

    # Add all Java files
    for root, dirs, files in os.walk(java_dir):
        for file in files:
            if file.endswith(".java"):
                full_path = os.path.join(root, file)
                with open(full_path, "r", encoding="utf-8") as f:
                    output.write("\n\n// FILE: " + full_path + "\n")
                    output.write(f.read())

    # Add all FXML files
    for root, dirs, files in os.walk(fxml_dir):
        for file in files:
            if file.endswith(".fxml"):
                full_path = os.path.join(root, file)
                with open(full_path, "r", encoding="utf-8") as f:
                    output.write("\n\n<!-- FILE: " + full_path + " -->\n")
                    output.write(f.read())

print(f"✅ All Java and FXML files written to {output_file_path}")
