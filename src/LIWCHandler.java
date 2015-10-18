import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


public class LIWCHandler {
		// Constants
	 	private static int categoriesStartIndex = 1;
	 	private static int wordsStartIndex = 65;
	 	
	 	// Global Variables to store in serialized object
	 	HashMap<Integer,String> categoriesID; // key = id, value = string of category
	 	HashMap<String,Category> categories; // key = string of category, value = object of Category class
	 	HashMap<String,Word> words; // key = string of word, value = object of Word class
	 	HashMap<String, ArrayList<Double>> correlationTable; // key = category from LIWC, value = 5 tuple correlations
	 	
	 	public LIWCHandler(String path) {
	        loadLIWCFromDictionaryFile(path+"LIWC2007_English080730.dic");
	        readCorrelations(path+"correlations.csv");
	 	}
	 	
	 	// Purpose: Uses an LIWC .dic file to populate in-memory data structures
	 	public void loadLIWCFromDictionaryFile(String fileName) {
	 		try{
	 			// Initialize variables
	 			FileReader inputFile = new FileReader(fileName);
	 			BufferedReader bufferReader = new BufferedReader(inputFile);
	 			String line;
	 			List<String> fileContents = new ArrayList<String>();
	 			categoriesID = new HashMap<Integer,String>();
	 			categories = new HashMap<String,Category>();
	 			words = new HashMap<String,Word>();

	 			// Read the contents of the dictionary into an ArrayList
	 			while ((line = bufferReader.readLine()) != null) fileContents.add(line);
	 			
	 			System.out.println("\n===== Done reading LIWC into ArrayList! =====\n");
	 			
	 			// Read the list of categories and append to appropriate HashMaps
	 			for (int i = categoriesStartIndex; i < wordsStartIndex; i++) {
	 				String[] temp = fileContents.get(i).split("\\s+");
	 				Integer categoryID = new Integer(temp[0]);
	 				String categoryString = temp[1];
	 				Category category = new Category(categoryID, categoryString);
	 				categories.put(categoryString, category);
	 				categoriesID.put(categoryID, categoryString);
	 			}

	 			// Read the words and mark their categories
	 			for (int i = wordsStartIndex; i < fileContents.size(); i++) {
	 				String[] temp = fileContents.get(i).split("\\s+");
	 				String wordString = temp[0];
	 				// wordString = wordString.replace("*","");
	 				Word word = new Word(wordString);
	 				words.put(wordString, word);
	 				for (int j = 1; j < temp.length; j++) {
	 					Integer categoryID = new Integer(temp[j]);
	 					word.putCategory(categories.get(categoriesID.get(categoryID)));
	 					categories.get(categoriesID.get(categoryID)).putWord(word);
	 				}
	 			}

	 			bufferReader.close();
	 		} catch(Exception e){
	 			System.out.println("Error while loading LIWC from dictionary file: " + e.getMessage());                      
	 		}
	 	}

	 	// Tags the string with the categories each word falls in
	 	public HashMap<String,Integer> tagString(String input) {
	 		HashMap<String,Integer> categoryCounts = new HashMap<String,Integer>();
	 		input = input.toLowerCase();
	 		String[] wordsInString = input.split(" ");
	 		System.out.println("===== Matches in the dictionary: =====\n");
	 		for (String str : wordsInString) {
	 			Word word = null;
	 			if (words.containsKey(str))
	 				word = words.get(str);
	 			else
	 				word = getStarWordString(str);
	 			if (word != null) {
	 				HashMap<String,Category> wordCategories = word.getCategories();
	 				System.out.print(word.getWordString() + " ");
	 				for (Category c : wordCategories.values()) {
	 					if (categoryCounts.containsKey(c.getCategoryString()))
	 						categoryCounts.put(c.getCategoryString(), categoryCounts.get(c.getCategoryString()) + 1); 
	 					else
	 						categoryCounts.put(c.getCategoryString(), 1);
	 				}
	 			}
	 		}
	 		System.out.println("\n");
	 		System.out.println("\n===== Final Counts: =====\n");
	 		System.out.println(categoryCounts);
	 		System.out.println("\n");
	 		return categoryCounts;
	 	}

	 	// Checks if the LIWC dictionary contains a wildcard entry by breaking input at all indexes until a match is found
	 	public Word getStarWordString(String input) {
	 		for (int i = 0; i < input.length(); i++) {
	 			String temp = input.substring(0, input.length()-i);
	 			if (words.containsKey(temp+"*")) 
	 				return words.get(temp+"*");
	 		}
	 		return null;
	 	}

	 	public String readFileIntoString(String fileName) {
	 		BufferedReader br = null;
	 		String finalOutput = "";
	 		String output = "";
	 		try {
	 			br = new BufferedReader(new FileReader(fileName));
	 			while ((output = br.readLine()) != null)
	 				finalOutput+=output;
	 		} catch (IOException e) {
	 			e.printStackTrace();
	 		} finally {
	 			try {
	 				if (br != null)br.close();
	 			} catch (IOException ex) {
	 				ex.printStackTrace();
	 			}
	 		}
	 		return finalOutput;
	 	}
	 	
	 	// Purpose: Read correlations table into hashmap
	 	public void readCorrelations(String filename) {
			correlationTable = new HashMap<String, ArrayList<Double>>();
			String csvFile = filename;
			BufferedReader br = null;
			String line = "";
			String cvsSplitBy = ",";
		 
			try {
				br = new BufferedReader(new FileReader(csvFile));
				while ((line = br.readLine()) != null) {
					String[] country = line.split(cvsSplitBy);
					ArrayList<Double> nums = new ArrayList<Double>();
					for (int i = 1; i < country.length; i++) {
						nums.add(Double.parseDouble(country[i]));
					}
					correlationTable.put(country[0], nums);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			System.out.println(correlationTable);
		}
	 	
	 	// Purpose: Calculate correlations final values given categories hashmap	 	
	 	public double[] calculateCorrelations(HashMap<String,Integer> hash) {
	 		int total = 0;
	         for (Integer value : hash.values()) {
	        	 System.out.println("adding to total " + value.intValue());
	        	 total+=value.intValue();
	         }
	         System.out.println("Total is " + total);
	         double[] final_correlations = {0,0,0,0,0};
	         for (Entry<String,Integer> entry : hash.entrySet()) {
	        	 String category = entry.getKey();
	        	 int value = entry.getValue().intValue();
	        	 System.out.println(value);
	        	 double ratio = (double)value/(double)total;
	        	 System.out.println("ratio is " + ratio);
	        	 ArrayList<Double> correlations = correlationTable.get(category);
	        	 if (correlations != null) {
	        		 for (int k = 0; k < correlations.size(); k++) {
	        			final_correlations[k]+=ratio*correlations.get(k);
	        		 }
	        	 }
	         }
	         System.out.println("ratios final are \n");
	         for (double d : final_correlations)
	        	 System.out.println(d);
	         return final_correlations;
	 	}

}
