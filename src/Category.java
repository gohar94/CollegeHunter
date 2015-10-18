import java.util.HashMap;

public class Category {

	private String categoryString;
	private HashMap<String,Word> words;
	private int id;

	public Category(int _id, String category) {
		categoryString = category;
		words = new HashMap<String,Word>();
		id = _id;
	}

	// Utility Methods
	public boolean hasWord(String word) {
		return words.containsKey(word);
	}

	// Getter Methods
	public String getCategoryString() {
		return categoryString;
	}

	// Setter Methods
	public void putWord(Word word) {
		words.put(word.getWordString(), word);
	}

}