package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.*;
import java.lang.Math;

/**
 * Evaluate Single Variable Binary Regression
 */
public class App
{
    private static final double THRESHOLD = 0.5;


    public static void main( String[] args )
    {
        String[] modelFiles = {"model_1.csv", "model_2.csv", "model_3.csv"};

        double[] bceValues       = new double[modelFiles.length];
        double[] accuracyValues  = new double[modelFiles.length];
        double[] precisionValues = new double[modelFiles.length];
        double[] recallValues    = new double[modelFiles.length];
        double[] f1Values        = new double[modelFiles.length];
        double[] aucValues       = new double[modelFiles.length];

        for (int m = 0; m < modelFiles.length; m++) {
            String filePath = modelFiles[m];

            //Read CSV 
            List<String[]> allData;
            try {
                FileReader fileReader = new FileReader(filePath);
                CSVReader csvReader = new CSVReaderBuilder(fileReader)
                        .withSkipLines(1).build();
                allData = csvReader.readAll();
                csvReader.close();
            } catch (Exception e) {
                System.out.println("Error reading: " + filePath);
                continue;
            }

            int n = allData.size();

            // Store parsed values — needed again for the ROC loop
            int[]    yTrue = new int[n];
            double[] yPred = new double[n];

            //  Pass 1: BCE + confusion matrix 
            double bceSum = 0.0;
            double epsilon = 1e-15; // To prevent log(0)
            int TP = 0, FP = 0, TN = 0, FN = 0;

            for (int i = 0; i < n; i++) {
                yTrue[i] = Integer.parseInt(allData.get(i)[0].trim());
                yPred[i] = Double.parseDouble(allData.get(i)[1].trim());
                // BCE Binary Cross Entropy = -1/n * Σ [y*log(y^) + (1-y)*log(1-y^)]
                bceSum += yTrue[i] * Math.log(yPred[i]) + (1 - yTrue[i]) * Math.log(1 - yPred[i]);

                // Confusion matrix at threshold = 0.5
                int yHat = (yPred[i] >= THRESHOLD) ? 1 : 0;
                if      (yTrue[i] == 1 && yHat == 1) TP++;
                else if (yTrue[i] == 0 && yHat == 1) FP++;
                else if (yTrue[i] == 0 && yHat == 0) TN++;
                else                                  FN++;
            }

            double bce       = -bceSum / n;
            double accuracy  = (double)(TP + TN) / (TP + TN + FP + FN);
            double precision = (TP + FP == 0) ? 0.0 : (double) TP / (TP + FP);
            double recall    = (TP + FN == 0) ? 0.0 : (double) TP / (TP + FN);
            double f1        = (precision + recall == 0) ? 0.0 : 2.0 * precision * recall / (precision + recall);

            bceValues[m]       = bce;
            accuracyValues[m]  = accuracy;
            precisionValues[m] = precision;
            recallValues[m]    = recall;
            f1Values[m]        = f1;

            //Pass 2: ROC curve + AUC 
            int nPositive = 0, nNegative = 0;
            for (int i = 0; i < n; i++) {
                if (yTrue[i] == 1) nPositive++;
                else               nNegative++;
            }

            // 101 points: threshold sweeps from 0.00 to 1.00 in steps of 0.01
            double[] rocX = new double[101]; // FPR at each threshold
            double[] rocY = new double[101]; // TPR at each threshold

            for (int t = 0; t <= 100; t++) {
                double th = t / 100.0;
                int tp = 0, fp = 0;
                for (int i = 0; i < n; i++) {
                    if (yTrue[i] == 1 && yPred[i] >= th) tp++;
                    if (yTrue[i] == 0 && yPred[i] >= th) fp++;
                }
                rocY[t] = (double) tp / nPositive; // TPR = TP / n_positive
                rocX[t] = (double) fp / nNegative; // FPR = FP / n_negative
            }

            // Trapezoidal rule: auc += (y[i-1] + y[i]) * |x[i-1] - x[i]| / 2
            double auc = 0.0;
            for (int t = 1; t <= 100; t++) {
                auc += (rocY[t - 1] + rocY[t]) * Math.abs(rocX[t - 1] - rocX[t]) / 2.0;
            }
            aucValues[m] = auc;

            //  Print results 
            System.out.println("for " + filePath);
            System.out.printf("\tBCE =%f\n", bce);
            System.out.println("\tConfusion matrix");
            System.out.println("\t\t\t\ty=1\t y=0");
            System.out.printf( "\t\t\ty^=1\t%d\t%d\n", TP, FP);
            System.out.printf( "\t\t\ty^=0\t%d\t%d\n", FN, TN);
            System.out.printf("\tAccuracy =%f\n",  accuracy);
            System.out.printf("\tPrecision =%f\n", precision);
            System.out.printf("\tRecall =%f\n",    recall);
            System.out.printf("\tf1 score =%f\n",  f1);
            System.out.printf("\tauc roc =%f\n",   auc);
        }

        //  Best model recommendations 
        // BCE: lower is better  →  findMin
        // All others: higher is better  →  findMax
        System.out.println("According to BCE, The best model is "       + findMin(bceValues,       modelFiles));
        System.out.println("According to Accuracy, The best model is "  + findMax(accuracyValues,  modelFiles));
        System.out.println("According to Precision, The best model is " + findMax(precisionValues, modelFiles));
        System.out.println("According to Recall, The best model is "    + findMax(recallValues,    modelFiles));
        System.out.println("According to F1 score, The best model is "  + findMax(f1Values,        modelFiles));
        System.out.println("According to AUC ROC, The best model is "   + findMax(aucValues,       modelFiles));
    }

    /** Returns the file name with the LOWEST value — used for BCE (lower = better). */
    static String findMin(double[] values, String[] names) {
        int best = 0;
        for (int i = 1; i < values.length; i++)
            if (values[i] < values[best]) best = i;
        return names[best];
    }

    /** Returns the file name with the HIGHEST value — used for Accuracy, Precision, etc. */
    static String findMax(double[] values, String[] names) {
        int best = 0;
        for (int i = 1; i < values.length; i++)
            if (values[i] > values[best]) best = i;
        return names[best];
    }
}
