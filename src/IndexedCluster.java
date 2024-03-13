import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IndexedCluster {

    private BSBI_Index bsbiIndex;

    public String resDirectoryPath;


    public IndexedCluster(String directoryPath, int blockSize) {
        this.bsbiIndex = new BSBI_Index(directoryPath, blockSize);
        this.resDirectoryPath = directoryPath;
    }

    public String getResDirectoryPath(){
        return resDirectoryPath;
    }

    // Perform K-means clustering on the indexed documents
    public Map<Integer, List<String>> performKMeansClustering(int k) throws IOException {
        // Retrieve the index from BSBI_Index
        Map<String, List<String>> index = bsbiIndex.getIndex();

        // Extract the text content of documents
        List<String> documents = index.values().stream()
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        // Perform K-means clustering
        KMeans kMeans = new KMeans(k, this.resDirectoryPath);
        Map<Integer, List<String>> clusters = kMeans.cluster();

        return clusters;
    }

    public static void main(String[] args) throws IOException {
        String directoryPath = "your_directory_path"; //Specify the path to indexed documents directory
        int blockSize = 10; // Example block size
        int k = 3; // Specify the number of clusters

        IndexedCluster indexedCluster = new IndexedCluster(directoryPath, blockSize);
        Map<Integer, List<String>> clusters = indexedCluster.performKMeansClustering(k);

        // Print the clusters
        for (Map.Entry<Integer, List<String>> entry : clusters.entrySet()) {
            System.out.println("Cluster " + entry.getKey() + ": " + entry.getValue());
        }
    }
}

