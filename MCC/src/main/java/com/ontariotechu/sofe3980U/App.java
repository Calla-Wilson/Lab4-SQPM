package com.ontariotechu.sofe3980U;


import java.io.FileReader;
import java.util.List;
import com.opencsv.*;

/**
 * Evaluate Single Variable Continuous Regression
 *
 */
public class App
{
    public static void main( String[] args )
    {
		String filePath="model.csv";
		FileReader filereader;
		List<String[]> allData;
		try{
			filereader = new FileReader(filePath); 
			CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build(); 
			allData = csvReader.readAll();
		}
		catch(Exception e){
			System.out.println( "Error reading the CSV file" );
			return;
		}
		
		int count=0;
		float[] yPredicted=new float[5];
		for (String[] row : allData) {
			int yTrue=Integer.parseInt(row[0]);
			System.out.print(yTrue);
			for(int i=0;i<5;i++){
				yPredicted[i]=Float.parseFloat(row[i+1]);
				System.out.print("  \t  "+yPredicted[i]);
			}
			System.out.println();
			count++;
			if (count==10){
				break;
			}
		} 

	}
	
}
