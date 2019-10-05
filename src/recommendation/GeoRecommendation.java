package recommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import db.mysql.MySQLConnection;
import entity.Item;

public class GeoRecommendation {
	public List<Item> recommendItems(String userId, double lat, double lon) {
		List<Item> recommendedItems = new ArrayList<>();
		// create a connection to db
		DBConnection connection = DBConnectionFactory.getConnection();
		// get favoritedItemIds from db
		Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);
		// a map of favorite and count
		Map<String, Integer> allCategories = new HashMap<>(); 
		
		for (String itemId : favoritedItemIds) {
			// get categories from db using itemId
			Set<String> categories = connection.getCategories(itemId);
			for (String category : categories) {
				allCategories.put(category, allCategories.getOrDefault(category, 0) + 1);
			}
		}
		// now we get the most "popular" categories for this user!!!
		// sort by count
		// eg: {"sports": 5, "music": 3, "art": 2}
		List<Entry<String, Integer>> categoryList = new ArrayList<>(allCategories.entrySet());
		Collections.sort(categoryList, (Entry<String, Integer> e1, Entry<String, Integer> e2) -> {
			return Integer.compare(e2.getValue(), e1.getValue());
		});
		
		// for each popular item, if it has not be loved by this user, nor already in the recommend list, recommend it!
		Set<String> visitedItemIds = new HashSet<>();
		for (Entry<String, Integer> category : categoryList) {
			List<Item> items = connection.searchItems(lat, lon, category.getKey());
			for (Item item : items) {
				// not in the favorite list                         // not in the recommend list
				if (!favoritedItemIds.contains(item.getItemId()) && !visitedItemIds.contains(item.getItemId())) {
					recommendedItems.add(item);
					visitedItemIds.add(item.getItemId());
				}
			}
		}
		connection.close();
		return recommendedItems;
	}
}
