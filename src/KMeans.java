import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class KMeans {

    private int numOfClusters; // Number of clusters
    private Map<String, List<String>> index; // Map representing the index of documents

    public KMeans(int numOfClusters, String indexPath) throws IOException {
        this.numOfClusters = numOfClusters;
        this.index = readIndex(indexPath);

        System.out.println("\nIndex.txt & Stats.txt path: " + indexPath + ";\nk (num of clusters): " + numOfClusters);

    }

    public int getNumOfClusters(){
        return numOfClusters;
    }

    public Map<String, List<String>> getIndex(){
        return index;
    }

    private List<String> getDocumentsAssignedToCentroid(String centroid) {
        List<String> assignedDocuments = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : index.entrySet()) {
            List<String> documents = entry.getValue();
            if (documents.contains(centroid)) {
                assignedDocuments.add(entry.getKey());
            }
        }
        return assignedDocuments;
    }



    // Method to read the index from the index file
    private Map<String, List<String>> readIndex(String indexPath) throws IOException {
        Map<String, List<String>> index = new HashMap<>();
        List<String> lines = Files.readAllLines(Paths.get(indexPath));
        for (String line : lines) {
            String[] parts = line.split(":");
            String term = parts[0];
            List<String> documents = Arrays.asList(parts[1].split(","));
            index.put(term, documents);
        }
        return index;
    }

    // Perform K-means clustering on the indexed documents
    public Map<Integer, List<String>> cluster() {
        // Step 1: Initialize centroids randomly
        List<String> centroids = initializeCentroids();

        // Step 2: Assign documents to the nearest centroid
        Map<Integer, List<String>> clusters = assignToClusters(centroids);

        // Step 3: Update centroids based on the mean of documents in each cluster
        while (true) {
            List<String> newCentroids = updateCentroids(clusters);
            if (centroids.equals(newCentroids)) {
                break; // Centroids converged
            }
            centroids = newCentroids;
            clusters = assignToClusters(centroids);
        }

        return clusters;
    }

    // Step 1: Initialize centroids randomly
    private List<String> initializeCentroids() {
        List<String> centroids = new ArrayList<>();
        Random random = new Random();
        List<String> allDocuments = new ArrayList<>(index.values().stream().flatMap(List::stream).toList());
        for (int i = 0; i < numOfClusters; i++) {
            int randomIndex = random.nextInt(allDocuments.size());
            centroids.add(allDocuments.get(randomIndex));
        }
        return centroids;
    }

    // Step 2: Assign documents to the nearest centroid
    private Map<Integer, List<String>> assignToClusters(List<String> centroids) {
        Map<Integer, List<String>> clusters = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : index.entrySet()) {
            String term = entry.getKey();
            List<String> documents = entry.getValue();
            int nearestCentroidIndex = findNearestCentroidIndex(documents, centroids);
            clusters.computeIfAbsent(nearestCentroidIndex, key -> new ArrayList<>()).add(term);
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
    private int findNearestCentroidIndex(List<String> documents, List<String> centroids) {
        // Initialize variables to keep track of the nearest centroid index and its similarity score
        int nearestCentroidIndex = -1;
        double maxSimilarity = Double.MIN_VALUE;

        // Calculate the TF-IDF vector for the given document
        Map<String, Double> documentVector = documentToVector(documents);

        // Calculate the cosine similarity between the document and each centroid
        for (int i = 0; i < centroids.size(); i++) {
            String centroid = centroids.get(i);
            // Calculate the TF-IDF vector for the centroid
            Map<String, Double> centroidVector = documentToVector(getDocumentsAssignedToCentroid(centroid));

            // Calculate dot product and magnitude for cosine similarity
            double dotProduct = 0.0;
            double docMagnitude = 0.0;
            double centroidMagnitude = 0.0;

            for (String term : documentVector.keySet()) {
                double docTfIdf = documentVector.get(term);
                double centroidTfIdf = centroidVector.getOrDefault(term, 0.0);

                dotProduct += docTfIdf * centroidTfIdf;
                docMagnitude += Math.pow(docTfIdf, 2);
                centroidMagnitude += Math.pow(centroidTfIdf, 2);
            }

            double tempDocMagnitude = Math.sqrt(docMagnitude);
            double tempCentroidMagnitude = Math.sqrt(centroidMagnitude);

            // Calculate cosine similarity
            //TODO: здесь происходит деление на 0 ибо docMagnitude == 0. Проверь его рассчеты,
            // соответственно ниже проверка иф == фолс, ведь similarity == NaN
            double similarity = dotProduct / (tempDocMagnitude * tempCentroidMagnitude);

            // Update nearest centroid index if the similarity is higher
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                nearestCentroidIndex = i;
            }
        }

        return nearestCentroidIndex;
    }


    // Convert a document (list of terms) to a TF-IDF vector
    private Map<String, Double> documentToVector(List<String> document) {
        // Calculate term frequency (TF)
        Map<String, Double> tf = new HashMap<>();
        for (String term : document) {
            tf.put(term, tf.getOrDefault(term, 0.0) + 1.0);
        }
        double totalTerms = document.size();
        for (String term : tf.keySet()) {
            tf.put(term, tf.get(term) / totalTerms);
        }

        // Calculate IDF and adjust TF to get TF-IDF
        Map<String, Double> tfidf = new HashMap<>();
        for (String term : tf.keySet()) {
            double idf = calculateIDF(term);
            tfidf.put(term, tf.get(term) * idf);
        }

        return tfidf;
    }

    // Calculate IDF for a given term
    private double calculateIDF(String term) {
        double docsWithTerm = index.containsKey(term) ? index.get(term).size() : 0;
        double totalDocs = index.size();
        return Math.log((totalDocs + 1) / (docsWithTerm + 1)) + 1; // Add smoothing to avoid division by zero
    }

    // Calculate the TF-IDF vector for a centroid
    private Map<String, Double> centroidToVector(List<List<String>> documents) {
        Map<String, Double> centroidVector = new HashMap<>();
        int numDocuments = documents.size();

        // Aggregate TF-IDF vectors of all documents assigned to the centroid
        for (List<String> document : documents) {
            Map<String, Double> documentVector = documentToVector(document);
            for (Map.Entry<String, Double> entry : documentVector.entrySet()) {
                String term = entry.getKey();
                double tfidf = entry.getValue();
                centroidVector.put(term, centroidVector.getOrDefault(term, 0.0) + tfidf);
            }
        }

        // Compute the average TF-IDF vector
        for (Map.Entry<String, Double> entry : centroidVector.entrySet()) {
            String term = entry.getKey();
            double tfidfSum = entry.getValue();
            double averageTfidf = tfidfSum / numDocuments;
            centroidVector.put(term, averageTfidf);
        }

        return centroidVector;
    }

    // Calculate the mean of documents in the cluster
    private String calculateMean(List<String> cluster) {
        // Aggregate TF-IDF vectors of all documents in the cluster
        Map<String, Double> aggregateVector = new HashMap<>();
        int numDocuments = cluster.size();

        for (String document : cluster) {
            Map<String, Double> documentVector = documentToVector(index.get(document));
            for (Map.Entry<String, Double> entry : documentVector.entrySet()) {
                String term = entry.getKey();
                double tfidf = entry.getValue();
                aggregateVector.put(term, aggregateVector.getOrDefault(term, 0.0) + tfidf);
            }
        }

        // Compute the average TF-IDF vector
        for (Map.Entry<String, Double> entry : aggregateVector.entrySet()) {
            String term = entry.getKey();
            double tfidfSum = entry.getValue();
            double averageTfidf = tfidfSum / numDocuments;
            aggregateVector.put(term, averageTfidf);
        }

        // Sort the terms by TF-IDF values in descending order
        List<Map.Entry<String, Double>> sortedTerms = new ArrayList<>(aggregateVector.entrySet());
        sortedTerms.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        // Construct the mean document using the top TF-IDF terms
        StringBuilder meanDocument = new StringBuilder();
        int termCount = 0;
        for (Map.Entry<String, Double> entry : sortedTerms) {
            String term = entry.getKey();
            double tfidf = entry.getValue();
            meanDocument.append(term).append(":").append(tfidf).append(" "); // Append term and its TF-IDF value
            termCount++;
            if (termCount >= 30) { // Adjust the number of terms to include in the mean document
                System.out.println("GOTCHA");
                break;
            }
        }

        return meanDocument.toString();
    }

}
