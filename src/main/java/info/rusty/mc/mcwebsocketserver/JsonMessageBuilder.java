package info.rusty.mc.mcwebsocketserver;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonMessageBuilder {
	private final Map<String, String> message = new HashMap<>();
	public JsonMessageBuilder(String partString) {
		//String is in format:
		// "key1=value1,key2=value2,key3=value3"
		// Split by ","
		String[] parts = partString.split(",");

		// Loop through parts
		for (String part : parts) {
			// Split by "="
			String[] keyValue = part.split("=");
			if (keyValue.length == 2) {
				// Add to map
				this.message.put(keyValue[0], keyValue[1]);
			}
		}
	}

	public String toJson() {
		Gson j = new Gson();
		return j.toJson(this.message);
	}
}
