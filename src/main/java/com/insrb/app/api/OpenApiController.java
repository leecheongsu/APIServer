package com.insrb.app.api;

import com.insrb.app.implements_.InsuroboDataBaseAccessTestWithDatabaseMapping;
import com.insrb.app.implements_.InsuroboDataBaseAccessTestWithDatabaseMapping.DatabaseMappers;
import com.insrb.app.implements_.InsuroboDataBaseAccessTestWithDatabaseMapping.DatabaseMappingHandler;
import com.insrb.app.mapper.*;
import com.insrb.app.openservice.api.InsuroboOpenApi;
import com.insrb.app.openservice.api.OpenServiceForInsrbBuildingAssessment;
import com.insrb.app.openservice.database.InsuroboDataBaseAccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@SuppressWarnings("unchecked")
@RestController
@RequestMapping("/apis")
public class OpenApiController {
	@Autowired
	OPEN0001 _open0001;
	@Autowired
	IN006CMapper in006cMapper;
	@Autowired
	IN001TMapper in001tMapper;
	@Autowired
	IN002TMapper in002tMapper;
	@Autowired
	IN010TMapper in010tMapper;
	
	DatabaseMappingHandler handler=new DatabaseMappingHandler() {
		@Override
		public void initializeDatabaseMapper(InsuroboDataBaseAccessTestWithDatabaseMapping map) {
			map.setMapper(DatabaseMappers.OPEN0001,_open0001);
			map.setMapper(DatabaseMappers.IN006CMapper,in006cMapper);
			map.setMapper(DatabaseMappers.IN001TMapper,in001tMapper);
			map.setMapper(DatabaseMappers.IN002TMapper,in002tMapper);
			map.setMapper(DatabaseMappers.IN010TMapper,in010tMapper);
		}
	};
	@PostMapping(path = "/quotes/assessment/group")
	public Map<String, Object> quoteAssessmentForGroup(
		@RequestHeader(name="x-insurobo-authorization",required=true) String authorization,
		@RequestHeader(name="apiVersion",required=true) String apiVersion,
		@RequestParam(name = "sigungucd", required = true) String sigungucd,
		@RequestParam(name = "bjdongcd", required = true) String bjdongcd,
		@RequestParam(name = "bun", required = true) int bun,
		@RequestParam(name = "ji", required = true) int ji
	) {
		InsuroboDataBaseAccess dba=new InsuroboDataBaseAccessTestWithDatabaseMapping(handler);

		return new OpenServiceForInsrbBuildingAssessment()
					.quoteAssessmentForGroup(InsuroboOpenApi.getInstance(dba, authorization), sigungucd, bjdongcd, bun, ji);
	}

	@PostMapping(path = "/quotes/assessment/single")
	public Map<String, Object> quoteAssessmentForSingle(
			@RequestHeader(name="x-insurobo-authorization",required=true) String authorization,
			@RequestHeader(name="apiVersion",required=true) String apiVersion,
			@RequestBody(required = true) Map<String, Object> body) {
		
		InsuroboDataBaseAccess dba=new InsuroboDataBaseAccessTestWithDatabaseMapping(handler);
		
		return new OpenServiceForInsrbBuildingAssessment()
				.quoteAssessmentForSingle(InsuroboOpenApi.getInstance(dba, authorization), 
						(Map<String, Object>)body.get("cover"), (Map<String, Object>)body.get("detail"));
	}

	
	@GetMapping(path = "/quotes/houseinfo/covers")
	public List<Map<String, Object>> quoteHouseInfoCovers(
		@RequestParam(name = "sigungucd", required = true) String sigungucd,
		@RequestParam(name = "bjdongcd", required = true) String bjdongcd,
		@RequestParam(name = "bun", required = true) int bun,
		@RequestParam(name = "ji", required = true) int ji
	) {
		return new OpenServiceForInsrbBuildingAssessment()
					.quoteHouseInfoCovers(new InsuroboDataBaseAccessTestWithDatabaseMapping(handler)
													, sigungucd, bjdongcd, bun, ji);
	}

	@GetMapping(path = "/quotes/houseinfo/details")
	public List<Map<String, Object>> quoteHouseInfoDetails(
		@RequestParam(name = "sigungucd", required = true) String sigungucd,
		@RequestParam(name = "bjdongcd", required = true) String bjdongcd,
		@RequestParam(name = "bun", required = true) int bun,
		@RequestParam(name = "ji", required = true) int ji,
		@RequestParam(name = "dongnm", required = true) String dongnm,
		@RequestParam(name = "honm", required = false) String honm
	) {
		return new OpenServiceForInsrbBuildingAssessment()
					.quoteHouseInfoDetails(sigungucd, bjdongcd, bun, ji, dongnm, honm);
	}
}
