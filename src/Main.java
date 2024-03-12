import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        int blockSize = 100;

        Scanner in = new Scanner(System.in);

        String directoryPath = "src/res";
        BSBI_Index idx = new BSBI_Index(directoryPath, blockSize);

        idx.printStatistics();
        idx.openTXT("src/results/Stats.txt");
    }
}