package com.insrb.app.api;

import com.insrb.app.insurance.AddressSearch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@SuppressWarnings("unchecked")
@RestController
@RequestMapping("/house")
public class HouseController {

	@Autowired
	AddressSearch addressSearch;

	@GetMapping(path = "juso")
	public Map<String, Object> juso(@RequestParam(name = "search", required = true) String search) {
		return addressSearch.getJusoList(search);
	}

	@GetMapping(path = "cover")
	public List<Map<String, Object>> cover(
		@RequestParam(name = "sigungucd", required = true) String sigungucd,
		@RequestParam(name = "bjdongcd", required = true) String bjdongcd,
		@RequestParam(name = "bun", required = true) int bun,
		@RequestParam(name = "ji", required = true) int ji
	) {
		Map<String, Object> search = addressSearch.getHouseCoverInfo(sigungucd, bjdongcd, bun, ji);

		Map<String, Object> response = (Map<String, Object>) search.get("response");
		Map<String, Object> header = (Map<String, Object>) response.get("header");

		if (!"00".equals(header.get("resultCode"))) throw new ResponseStatusException(HttpStatus.NO_CONTENT);

		Map<String, Object> body = (Map<String, Object>) response.get("body");
		Map<String, Object> items = (Map<String, Object>) body.get("items");

		List<Map<String, Object>> item = new ArrayList<Map<String, Object>>();

		// 단건인 경우, XML 파서가 단건인 경우 배열 처리 안하고 넘기는 것 같음.
		if (items.get("item") instanceof HashMap) {
			item.add((Map<String, Object>) items.get("item"));
		} else { //리스트로 올 경우
			item = (List<Map<String, Object>>) items.get("item");
		}

		return item;
	}

	// TODO: 위와 같이 item만 뽑아내는 코드 작성 요망.
	@GetMapping(path = "detail")
	public Map<String, Object> detail(
		@RequestParam(name = "sigungucd", required = true) String sigungucd,
		@RequestParam(name = "bjdongcd", required = true) String bjdongcd,
		@RequestParam(name = "bun", required = true) int bun,
		@RequestParam(name = "ji", required = true) int ji
	) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("cover", addressSearch.getHouseCoverInfo(sigungucd, bjdongcd, bun, ji));
		data.put("detail", addressSearch.getHouseDetailInfo(sigungucd, bjdongcd, bun, ji));
		return data;
	}
}
