import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class KMeans {

    private int k; // Number of clusters
    private List<String> documents; // List of documents in the corpus

    public KMeans(int k, String documentsFolderPath) throws IOException {
        this.k = k;
        this.documents = readDocumentsFromFolder(documentsFolderPath);
    }

    // Method to read all documents from a folder
    private List<String> readDocumentsFromFolder(String folderPath) throws IOException {
        List<String> documents = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(folderPath))) {
            for (Path path : directoryStream) {
                if (Files.isRegularFile(path)) {
                    String document = new String(Files.readAllBytes(path));
                    documents.add(document);
                }
            }
        }
        return documents;
    }

    // Perform K-means clustering on the given documents
    public Map<Integer, List<String>> cluster() throws IOException {
        // Step 1: Initialize centroids randomly
        List<String> centroids = initializeCentroids(documents);

        // Step 2: Assign documents to the nearest centroid
        Map<Integer, List<String>> clusters = assignToClusters(documents, centroids);

        // Step 3: Update centroids based on the mean of documents in each cluster
        while (true) {
            List<String> newCentroids = updateCentroids(clusters);
            if (centroids.equals(newCentroids)) {
                break; // Centroids converged
            }
            centroids = newCentroids;
            clusters = assignToClusters(documents, centroids);
        }

        return clusters;
    }

    // Step 1: Initialize centroids randomly
    private List<String> initializeCentroids(List<String> documents) {
        List<String> centroids = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < k; i++) {
            int randomIndex = random.nextInt(documents.size());
            centroids.add(documents.get(randomIndex));
        }
        return centroids;
    }

    // Step 2: Assign documents to the nearest centroid
    private Map<Integer, List<String>> assignToClusters(List<String> documents, List<String> centroids) throws IOException {
        Map<Integer, List<String>> clusters = new HashMap<>();
        for (String document : documents) {
            int nearestCentroidIndex = findNearestCentroidIndex(document, centroids);
            clusters.computeIfAbsent(nearestCentroidIndex, key -> new ArrayList<>()).add(document);
        }
        return clusters;
    }

    // Step 3: Update centroids based on the mean of documents in each cluster
    private List<String> updateCentroids(Map<Integer, List<String>> clusters) {
        List<String> newCentroids = new ArrayList<>();
        for (List<String> cluster : clusters.values()) {
            String centroid = calculateMean(cluster);
            newCentroids.add(centroid);
        }
        return newCentroids;
    }

    // Find the index of the nearest centroid for the given document
    private int findNearestCentroidIndex(String document, List<String> centroids) throws IOException {
        double minDistance = Double.MAX_VALUE;
        int nearestCentroidIndex = -1;
        for (int i = 0; i < centroids.size(); i++) {
            double distance = calculateDistance(document, centroids.get(i));
            if (distance < minDistance) {
                minDistance = distance;
                nearestCentroidIndex = i;
            }
        }
        return nearestCentroidIndex;
    }

    // Calculate the Euclidean distance between two documents (for simplicity)
    private double calculateDistance(String filePath1, String filePath2) throws IOException {
        // Read the contents of the files
        String document1 = new String(Files.readAllBytes(Paths.get(filePath1)));
        String document2 = new String(Files.readAllBytes(Paths.get(filePath2)));

        // Convert the documents to numerical feature vectors
        Map<String, Double> vector1 = documentToVector(document1, this.documents);
        Map<String, Double> vector2 = documentToVector(document2, this.documents);

        // Calculate the squared Euclidean distance between the feature vectors
        double squaredDistance = 0.0;
        for (String term : vector1.keySet()) {
            double diff = vector1.getOrDefault(term, 0.0) - vector2.getOrDefault(term, 0.0);
            squaredDistance += diff * diff;
        }

        // Return the square root of the squared distance (Euclidean distance)
        return Math.sqrt(squaredDistance);
    }

    /**Convert a document (file) to a numerical feature vector using TF-IDF*/
    private Map<String, Double> documentToVector(String document, List<String> corpus) {
        // Split the document into words
        String[] words = document.split("\\s+");

        // Calculate term frequency (TF)
        Map<String, Double> tf = new HashMap<>();
        for (String word : words) {
            tf.put(word, tf.getOrDefault(word, 0.0) + 1.0);
        }
        double totalWords = words.length;
        for (String word : tf.keySet()) {
            tf.put(word, tf.get(word) / totalWords);
        }

        // Calculate IDF and adjust TF to get TF-IDF
        Map<String, Double> tfidf = new HashMap<>();
        for (String word : tf.keySet()) {
            double idf = calculateIDF(word, corpus);
            tfidf.put(word, tf.get(word) * idf);
        }

        return tfidf;
    }

    // Calculate IDF for a given term
    private double calculateIDF(String term, List<String> corpus) {
        double docsWithTerm = 0;
        for (String document : corpus) {
            if (document.contains(term)) {
                docsWithTerm++;
            }
        }
        return Math.log((double) corpus.size() / (1.0 + docsWithTerm));
    }

    // Calculate the mean of documents in the cluster (for simplicity, just return the first document)
    private String calculateMean(List<String> cluster) {
        // This is a placeholder method; you'll need to implement a proper mean calculation
        // For simplicity, let's assume the mean of documents is the first document in the cluster
        return cluster.get(0);
    }
}

