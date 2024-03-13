import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IndexedCluster {

    private BSBI_Index bsbiIndex;

    public String resourceDirectoryPath;
    public String resultDirectoryPath;

    int numOfClusters;


    public IndexedCluster(BSBI_Index idx, String resultsPath, int k) throws IOException {
        this.bsbiIndex = idx;
        this.resultDirectoryPath = resultsPath;
        this.numOfClusters = k;

        System.out.println("Index.txt & Stats.txt path: " + resultsPath + ";\nk (num of clusters): " + k);


        //performKMeansClustering(numOfClusters);
    }

    public String getResourceDirectoryPath(){
        return resourceDirectoryPath;
    }

    public String getResultDirectoryPath(){
        return resultDirectoryPath;
    }

    public int getNumOfClusters(){
        return numOfClusters;
    }

    // Perform K-means clustering on the indexed documents
    public Map<Integer, List<String>> performKMeansClustering(int k) throws IOException {
        // Retrieve the index from BSBI_Index
        Map<String, List<String>> index = bsbiIndex.getIndex();

        // Extract the text content of documents
        List<String> documents = index.values().stream()
                .flatMap(List::stream)
                .distinct().toList();

        // Perform K-means clustering
        KMeans kMeans = new KMeans(k, this.resultDirectoryPath);

        return kMeans.cluster();
    }



}

