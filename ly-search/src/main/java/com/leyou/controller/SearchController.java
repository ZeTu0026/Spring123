package com.leyou.controller;

import com.leyou.service.SearchService;
import com.leyou.utils.SearchRequest;
import com.leyou.utils.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {
	@Autowired
	private SearchService searchService;

	@PostMapping("page")
	private ResponseEntity<SearchResult> page(@RequestBody SearchRequest searchRequest) {
		SearchResult searchResult = searchService.page(searchRequest);
		return null;
	}
}
