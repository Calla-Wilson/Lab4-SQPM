package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.*;

/**
 * Evaluate Multi-Class Classification Metrics
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        String filePath = "model.csv";
        FileReader filereader;
        List<String[]> allData;
        try {
            filereader = new FileReader(filePath); 
            // Skipping the header line if it exists
            CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build(); 
            allData = csvReader.readAll();
        }
        catch(Exception e) {
            System.out.println("Error reading the CSV file");
            return;
        }

        double totalCE = 0;
        int n = allData.size();
        int[][] confusionMatrix = new int[5][5]; // Rows: Predicted (y^), Cols: Actual (y)

        for (String[] row : allData) {
            int yTrue = Integer.parseInt(row[0]); // Actual class (1-5)
            float[] yPredictedProbs = new float[5];
            
            int bestClass = 0;
            float maxProb = -1.0f;

            for (int i = 0; i < 5; i++) {
                yPredictedProbs[i] = Float.parseFloat(row[i + 1]);
                
                // Identify the class with the highest probability (argmax)
                if (yPredictedProbs[i] > maxProb) {
                    maxProb = yPredictedProbs[i];
                    bestClass = i + 1; // Class is 1-indexed
                }
            }

            // 1. Accumulate Cross Entropy: -log(probability of the actual class)
            // Use Math.log for natural logarithm
            totalCE += Math.log(yPredictedProbs[yTrue - 1]);

            // 2. Update Confusion Matrix: matrix[predicted-1][actual-1]
            confusionMatrix[bestClass - 1][yTrue - 1]++;
        }

        // Final CE calculation (negative average)
        double finalCE = -totalCE / n;

        // Output Results
        System.out.printf("CE = %.7f%n", finalCE);
        System.out.println("Confusion matrix");
        System.out.println("\t\ty=1\ty=2\ty=3\ty=4\ty=5");

        for (int i = 0; i < 5; i++) {
            System.out.print("\ty^=" + (i + 1) + "\t");
            for (int j = 0; j < 5; j++) {
                System.out.print(confusionMatrix[i][j] + "\t");
            }
            System.out.println();
        }
    }
}
