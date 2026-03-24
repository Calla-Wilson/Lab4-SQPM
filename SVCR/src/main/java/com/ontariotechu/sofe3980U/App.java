package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.*;

/**
 * Evaluate Single Variable Continuous Regression
 */
public class App
{
    // Small epsilon to avoid division by zero in MARE
    private static final double EPSILON = 1e-10;

    public static void main( String[] args )
    {
        String[] filePaths = {"model_1.csv", "model_2.csv", "model_3.csv"};

        double[] mseValues  = new double[filePaths.length];
        double[] maeValues  = new double[filePaths.length];
        double[] mareValues = new double[filePaths.length];

        for (int m = 0; m < filePaths.length; m++) {
            String filePath = filePaths[m];

            // Read all rows from the CSV (skip header line)
            List<String[]> allData;
            try {
                FileReader fileReader = new FileReader(filePath);
                CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build();
                allData = csvReader.readAll();
                csvReader.close();
            } catch (Exception e) {
                System.out.println("Error reading the CSV file: " + filePath);
                return;
            }

            int n = allData.size();
            double sumSquaredError  = 0.0;
            double sumAbsError      = 0.0;
            double sumRelAbsError   = 0.0;

            for (String[] row : allData) {
                float yTrue      = Float.parseFloat(row[0]);
                float yPredicted = Float.parseFloat(row[1]);

                double error    = yTrue - yPredicted;
                double absError = Math.abs(error);

                sumSquaredError += error * error;                              // for MSE
                sumAbsError     += absError;                                   // for MAE
                sumRelAbsError  += absError / (Math.abs(yTrue) + EPSILON);   // for MARE
            }

            double mse  = sumSquaredError / n;
            double mae  = sumAbsError     / n;
            double mare = sumRelAbsError  / n;   // expressed as a fraction (not %)

            mseValues[m]  = mse;
            maeValues[m]  = mae;
            mareValues[m] = mare;

            System.out.println("for " + filePath);
            System.out.printf("\tMSE = %f%n",  mse);
            System.out.printf("\tMAE = %f%n",  mae);
            System.out.printf("\tMARE = %f%n", mare);
        }

        // Recommend the best model for each metric (lowest value wins)
        System.out.println("According to MSE, The best model is "  + bestModel(mseValues,  filePaths));
        System.out.println("According to MAE, The best model is "  + bestModel(maeValues,  filePaths));
        System.out.println("According to MARE, The best model is " + bestModel(mareValues, filePaths));
    }

    /**
     * Returns the file name of the model with the lowest metric value.
     */
    static String bestModel(double[] values, String[] fileNames) {
        int bestIndex = 0;
        for (int i = 1; i < values.length; i++) {
            if (values[i] < values[bestIndex]) {
                bestIndex = i;
            }
        }
        return fileNames[bestIndex];
    }

}
