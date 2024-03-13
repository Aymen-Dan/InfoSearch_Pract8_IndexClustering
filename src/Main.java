import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {



       /* BSBI_Index idx = new BSBI_Index(directoryPath, blockSize);

        idx.printStatistics();
        idx.openTXT("src/results/Stats.txt");*/


    public static void main(String[] args) throws IOException {

        String resourcePath = "src/resource";
        String resultsPath = "src/results/Index.txt";

        Scanner in = new Scanner(System.in);

        int blockSize = 150; //Block size
        int k = 3; //Number of clusters

        BSBI_Index idx = new BSBI_Index(resourcePath, blockSize);

       // System.out.println("Doc collection path: " + resourcePath + ";\nIndex.txt & Stats.txt path: " + resultsPath + ";\nIndex block size: " + blockSize + ";\nk (num of clusters): " + k);

        IndexedCluster indexedCluster = new IndexedCluster(idx, resultsPath, k);
        Map<Integer, List<String>> clusters = indexedCluster.performKMeansClustering(k);

        // Print the clusters
        for (Map.Entry<Integer, List<String>> entry : clusters.entrySet()) {
            System.out.println("Cluster " + entry.getKey() + ": " + entry.getValue());
        }
    }
}