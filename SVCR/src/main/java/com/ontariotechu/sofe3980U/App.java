package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.*;
import java.lang.Math;

/**
 * Evaluate Single Variable Continuous Regression
 */
public class App
{
    // Small epsilon to avoid division by zero in MARE
    private static final double EPSILON = 1e-10;

    public static void main( String[] args )
    {
        String[] modelFiles = {"model_1.csv", "model_2.csv", "model_3.csv"};

        double[] mseValues  = new double[modelFiles.length];
        double[] maeValues  = new double[modelFiles.length];
        double[] mareValues = new double[modelFiles.length];

        for (int m = 0; m < modelFiles.length; m++) {
            String filePath = modelFiles[m];

            // Read all rows from the CSV (skip header line)
            List<String[]> allData;
            try {
                FileReader fileReader = new FileReader(filePath);
                CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build();
                allData = csvReader.readAll();
                csvReader.close();
            } catch (Exception e) {
                System.out.println("Error reading the CSV file: " + filePath);
                continue;
            }

            int n = allData.size();
            double sumSquaredError  = 0.0;
            double sumAbsError      = 0.0;
            double sumRelAbsError   = 0.0;

            for (String[] row : allData) {
                float y_true      = Float.parseFloat(row[0]);
                float y_predicted = Float.parseFloat(row[1]);

                double error    = y_true - y_predicted;
                double absError = Math.abs(error);

                sumSquaredError += error * error;                              // for MSE
                sumAbsError     += absError;                                   // for MAE
                sumRelAbsError  += absError / (Math.abs(y_true) + EPSILON);   // for MARE
            }

            double mse  = sumSquaredError / n;
            double mae  = sumAbsError     / n;
            double mare = sumRelAbsError  / n;   // expressed as a fraction (not %)

            mseValues[m]  = mse;
            maeValues[m]  = mae;
            mareValues[m] = mare;

            System.out.println("for " + filePath);
            System.out.printf("\tMSE =%f\n",  mse);
            System.out.printf("\tMAE =%f\n",  mae);
            System.out.printf("\tMARE =%f\n", mare);
        }

        // Recommend the best model for each metric (lowest value wins)
        System.out.println("According to MSE, The best model is "  + bestModel(mseValues,  modelFiles));
        System.out.println("According to MAE, The best model is "  + bestModel(maeValues,  modelFiles));
        System.out.println("According to MARE, The best model is " + bestModel(mareValues, modelFiles));
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

    // -----------------------------------------------------------------------
    // Kept for reference / unit testing — not used in main() above
    // -----------------------------------------------------------------------

    /** MSE for a single (y_true, y_predicted) pair */
    static double MSE(double y_true, double y_predicted) {
        double error = y_true - y_predicted;
        return error * error;
    }

    /** MAE for a single (y_true, y_predicted) pair */
    static double MAE(double y_true, double y_predicted) {
        return Math.abs(y_true - y_predicted);
    }

    /** MARE for a single (y_true, y_predicted) pair */
    static double MARE(double y_true, double y_predicted) {
        return Math.abs(y_true - y_predicted) / (Math.abs(y_true) + EPSILON);
    }
}
