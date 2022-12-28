package com.twillio.callback.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KeysRetrivalServiceImpl {

	public Map<String, String> getKeys(Map<String, Object> ivrPayload, String string) {
		log.info("--------IvrPayload : {} and level : {}", ivrPayload, string);
		ModelMap m = new ModelMap();
		m.put("keys", findKeys("", ivrPayload, new ArrayList<>()));

		String[] splitPath = string.split("-");
		List<String> keys = (List<String>) m.get("keys");
		String[] newKeys = keys.stream().toArray(String[]::new);

		log.info("-------NewKeys  in keysRetrival : {}", newKeys);
		Set<String> collect = Arrays.stream(newKeys).filter(finalKey -> {
			return finalKey.split("\\.").length == splitPath.length + 1;
		}).map(k -> k.split("\\.")[1]).collect(Collectors.toSet());

		Map<String, String> output = new LinkedHashMap<String, String>();
		collect.stream().forEach(key -> {
			String k = key.substring(6, 7);
			output.put(k, key);
		});

		log.info("--------Final Output after key retrival : {}", output);
		return output;
	}

	private List<String> findKeys(String parentKey, Map<String, Object> treeMap, List<String> keys) {
		treeMap.forEach((key, value) -> {
			if (value instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) value;
				findKeys(parentKey + key + ".", map, keys);
			}
			if (value instanceof List) {
				List<?> list = (List<?>) value;
				if (!list.isEmpty()) {
					fillListData(parentKey + key, list, keys);
				}
			}
			if (parentKey.isEmpty()) {
				keys.add(key);
			} else {
				keys.add(parentKey + key);
			}
		});
		return keys;
	}

	@SuppressWarnings("unchecked")
	private void fillListData(String parentKey, List<?> list, List<String> keys) {
		for (int iter = 0; iter < list.size(); iter++) {
			if (list.get(iter) instanceof List) {
				List<?> l = (List<?>) (list.get(iter));
				fillListData(parentKey + "[" + iter + "]" + ".", l, keys);
			}
			if (list.get(iter) instanceof Map) {
				Map<String, Object> map = (Map<String, Object>) list.get(iter);
				Integer position = iter + 1;
				findKeys(parentKey + "[" + position + "]" + ".", map, keys);
			}
		}
	}
}