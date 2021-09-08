package com.insrb.app.implements_;

import com.insrb.app.mapper.*;
import com.insrb.app.openservice.database.InsuroboDataBaseAccess;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsuroboDataBaseAccessTest extends InsuroboDataBaseAccess{
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

	public GetInformation getProducts(){
		return new GetInformation() {
			@Override
			public Map<String, Object> selectByPcode(String pCode) {
				return in006cMapper.selectByPcode(pCode);
			}};
	}

	public GetBuildingType getBuildingType(){
		return new GetBuildingType() {
			@Override
			public String getBuildingType(String etc_purps, String main_purps_cd_nm, String p_cnt,
					String p_max_grnd_cnt, String p_total_area) {
				return in001tMapper.getBuildingType(etc_purps,  main_purps_cd_nm,  p_cnt,
						 p_max_grnd_cnt,  p_total_area);
			}

			@Override
			public Map<String, Object> selectById(String quote_no) {
				return in001tMapper.selectById(quote_no);
			}};
	}

	public GetListByID getPremiums(){
		return new GetListByID() {
			@Override
			public List<Map<String, Object>> selectById(String quote_no) {
				return in002tMapper.selectById(quote_no);
			}};
	}

	public InsertInsurance getInsuranceRegistration(){
		return new InsertInsurance() {
			@Override
			public void fireinsurance_insert(String quote_no, String building_type, String address_api,
					String group_ins, String bld_nm, String dong_info, String main_purps_cd_nm, String new_plat_plc,
					String etc_purps, String use_apr_day, String etc_roof, String dong_nm, String tot_area,
					String cnt_sedae, String grnd_flr_cnt, String ugrnd_flr_cnt, String etc_strct, String cover,
					String detail) {
				in010tMapper.fireinsurance_insert(quote_no,  building_type,  address_api,
						 group_ins,  bld_nm,  dong_info,  main_purps_cd_nm,  new_plat_plc,
						 etc_purps,  use_apr_day,  etc_roof,  dong_nm,  tot_area,
						 cnt_sedae,  grnd_flr_cnt,  ugrnd_flr_cnt,  etc_strct, cover,
						 	detail);
			}};
	}

	@Override
	public GetLicence getLicence() {
		return new GetLicence() {
			@Override
			public int validateLicence(String licenceSerialKey,String accessTag) {
				Map<String, Object> params=new HashMap<String, Object>(0);
				params.put("licenceSerialKey", licenceSerialKey);
				params.put("accessTag", accessTag);
				params.put("accessHistoryKey", null);
				_open0001.validateLicence(params);
				return (int)params.get("accessHistoryKey");
			}

			@Override
			public void resultLogToLicence(String licenceSerialKey, int accessHistoryKey, String resultLog) {
				_open0001.resultLogToLicence(licenceSerialKey, accessHistoryKey, resultLog);
			}
			
		};
	}
}
