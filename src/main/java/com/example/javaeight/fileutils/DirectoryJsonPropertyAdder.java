package com.example.javaeight.fileutils;

import java.io.*;
import java.util.regex.*;

public class DirectoryJsonPropertyAdder {

    public static void addJsonPropertyAnnotationsToDirectory(String directoryPath) {
        File directory = new File(directoryPath);

        if (!directory.isDirectory()) {
            System.err.println("Provided path is not a directory: " + directoryPath);
            return;
        }

        File[] javaFiles = directory.listFiles((dir, name) -> name.endsWith(".java"));
        if (javaFiles == null || javaFiles.length == 0) {
            System.out.println("No Java files found in the directory: " + directoryPath);
            return;
        }

        for (File file : javaFiles) {
            try {
                addJsonPropertyAnnotations(file.getPath());
                System.out.println("Processed file: " + file.getName());
            } catch (IOException e) {
                System.err.println("Failed to process file: " + file.getName() + " - " + e.getMessage());
            }
        }
    }

    public static void addJsonPropertyAnnotations(String filePath) throws IOException {
        StringBuilder modifiedClass = new StringBuilder();
        boolean importAdded = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                // Add the JsonProperty import if not already present
//                if (!importAdded && line.trim().startsWith("public class")) {
//                    modifiedClass.append("import com.fasterxml.jackson.annotation.JsonProperty;\n\n");
//                    importAdded = true;
//                }

                // Check for field declarations (including List or other generic types)
                if (line.trim().matches("protected\\s+(\\w+|List<\\w+>)\\s+\\w+;")) {
                    String fieldName = extractFieldName(line);
                    String capitalizedFieldName = capitalizeFirstLetter(fieldName);
                    modifiedClass.append("    @JsonProperty(\"").append(capitalizedFieldName).append("\")\n");
                }

                modifiedClass.append(line).append("\n");
            }
        }

        // Write the modified class back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(modifiedClass.toString());
        }
    }

    private static String extractFieldName(String fieldLine) {
        // Extract the field name from the line (e.g., "private String name;" -> "name")
        String[] parts = fieldLine.trim().split("\\s+");
        String fieldDeclaration = parts[2]; // Get the part containing the field name
        return fieldDeclaration.replace(";", ""); // Remove trailing semicolon
    }

    private static String capitalizeFirstLetter(String input) {
        // Capitalize the first letter of the string
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static void main(String[] args) {
        // Replace with the actual directory path containing Java files
        String directoryPath = "/Users/gramaraju/Documents/new_workspace/java-eight/src/main/resources/representation"; // Update with the actual directory path

        addJsonPropertyAnnotationsToDirectory(directoryPath);
    }
}
