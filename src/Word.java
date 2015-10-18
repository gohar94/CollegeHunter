import java.util.HashMap;

public class Word {

	private String wordString;
	private HashMap<String,Category> categories;

	public Word(String str) {
		wordString = str;
		categories = new HashMap<String,Category>();
	}

	// Utility Methods
	public boolean isCategory(String category) {
		return categories.containsKey(category);
	}

	// Getter Methods
	public String getWordString() {
		return wordString;
	}

	public HashMap<String,Category> getCategories() {
		return categories;
	}

	// Setter Methods
	public void putCategory(Category category) {
		categories.put(category.getCategoryString(), category);
	}

}