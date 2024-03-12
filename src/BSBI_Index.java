import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class BSBI_Index {

    private Map<String, List<String>> index;
    private long elapsedTime;
    private int numFilesIndexed;
    private double totalFileSize;
    private String statsFilePath;

    public BSBI_Index(String directoryPath, int blockSize) {
        long startTime = System.nanoTime();

        this.index = buildBSBIIndex(directoryPath, blockSize);

        long endTime = System.nanoTime();
        this.elapsedTime = endTime - startTime;

        // Save the index to a file and update statistics
        updateStatistics();
        writeStatisticsToFile();
    }

    // Getter for elapsed time
    public long getElapsedTime() {
        return elapsedTime;
    }

    // Getter for the number of files indexed
    public int getNumFilesIndexed() {
        return numFilesIndexed;
    }

    // Getter for the total file size indexed
    public double getTotalFileSize() {
        return totalFileSize;
    }



    /**Method to write statistics to Stats.txt in src/results/ */
    public void writeStatisticsToFile() {
        String filePath = statsFilePath;

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("Time taken to complete BSBI: " + this.getElapsedTime() + " ns,\nor " + this.getElapsedTime() / 1_000_000.0 + " ms,\nor " + this.getElapsedTime() / 1_000_000_000.0 + " s.");
            writer.println("\nNumber of files indexed: " + numFilesIndexed);
            writer.println("\nTotal file size indexed: " + totalFileSize + " KB");
            System.out.println("Statistics saved to: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Tokenize a document into terms
    private List<String> tokenize(String document) {
        // You may want to improve this based on your specific needs
        String[] terms = document.toLowerCase().split("\\s+");
        return Arrays.asList(terms);
    }

    // BSBI Index building method
    private Map<String, List<String>> buildBSBIIndex(String directoryPath, int blockSize) {
        Map<String, List<String>> termDict = new HashMap<>();

        File directory = new File(directoryPath);
        File[] files = directory.listFiles();

        if (files == null) {
            System.out.println("Error! The specified directory contains no files.");
            return termDict;
        }

        // Update the number of files
        numFilesIndexed = files.length;

        // Update the total file size
        totalFileSize = 0;
        for (File file : files) {
            totalFileSize += file.length();
        }
        totalFileSize /= 1024.0;  // Convert to KB

        // Step 1: Partitioning
        int numBlocks = (int) Math.ceil((double) numFilesIndexed / blockSize);

        for (int blockNum = 0; blockNum < numBlocks; blockNum++) {
            int startIdx = blockNum * blockSize;
            int endIdx = Math.min((blockNum + 1) * blockSize, numFilesIndexed);
            File[] blockFiles = Arrays.copyOfRange(files, startIdx, endIdx);

            List<Map.Entry<String, String>> blockTerms = new ArrayList<>();

            // Step 2: Tokenization and Sorting for each block
            for (File file : blockFiles) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    StringBuilder document = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        document.append(line).append(" ");
                    }

                    List<String> terms = tokenize(document.toString());
                    for (String term : terms) {
                        blockTerms.add(new AbstractMap.SimpleEntry<>(term, file.getName()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Sort terms within the block
            blockTerms.sort(Comparator.comparing(Map.Entry::getKey));

            // Step 3: Merging
            for (Map.Entry<String, String> entry : blockTerms) {
                String term = entry.getKey();
                String fileName = entry.getValue();

                termDict.computeIfAbsent(term, k -> new ArrayList<>()).add(fileName);
            }
        }

        // Step 4: Final Index Creation
        Map<String, List<String>> finalIndex = new HashMap<>();
        termDict.forEach((term, fileNames) -> finalIndex.put(term, new ArrayList<>(new HashSet<>(fileNames))));

        // Save the index to a file
        saveIndexToFile(finalIndex);

        return finalIndex;
    }

    // Method to save the index to a file named Index.txt in src/results/
    private void saveIndexToFile(Map<String, List<String>> idx) {
        String filePath = "src/results/Index.txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, List<String>> entry : idx.entrySet()) {
                writer.println(entry.getKey() + ": " + entry.getValue());
            }
            System.out.println("Index saved to: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Update statistics file path and create directories if necessary
    private void updateStatistics() {
        statsFilePath = "src/results/Stats.txt";
        File statsFile = new File(statsFilePath);

        // Create directories if they don't exist
        File resultsDir = statsFile.getParentFile();
        if (!resultsDir.exists()) {
            resultsDir.mkdirs();
        }
    }


    // Method to print the index to the console
    public void printIndex() {
        for (Map.Entry<String, List<String>> entry : index.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    // Method to print statistics to the console
    public void printStatistics() {
        System.out.println("Time taken to complete BSBI: " + this.getElapsedTime() + " ns," +
                "\nor " + this.getElapsedTime() / 1_000_000.0 + " ms," +
                "\nor " + this.getElapsedTime() / 1_000_000_000.0 +
                " s.\nNumber of files indexed: " + numFilesIndexed +
                "\nTotal file size indexed: " + totalFileSize + " KB");
    }


    /**OPEN A .TXT FILES*/
    public void openTXT(String filePath) throws IOException {
        File file = new File(filePath);

        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            System.out.println("\nPulling up the file...");

            if (file.exists()) {
                desktop.open(file);
            } else {
                System.out.println("File not found: " + filePath + "; Please restart the program.");
            }
        } else {
            System.out.println("Desktop is not supported.");
        }
    }
}

