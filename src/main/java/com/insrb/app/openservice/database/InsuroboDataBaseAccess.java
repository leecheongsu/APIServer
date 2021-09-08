package com.insrb.app.openservice.database;

import java.util.List;
import java.util.Map;

public abstract class InsuroboDataBaseAccess {
	public interface GetListByID{

		List<Map<String, Object>> selectById(String quote_no);
		
	}
	public interface GetLicence{
		int validateLicence(String licenceSerialKey,String accessTag);
		void resultLogToLicence(String licenceSerialKey, int accessHistoryKey, String resultLog);
	}
	public interface GetItemByID{

		Map<String, Object> selectById(String quote_no);
		
	}
	public interface GetInformation{

		Map<String, Object> selectByPcode(String code);
		
	}
	public interface GetBuildingType extends GetItemByID{

		String getBuildingType(String etc_purps, String main_purps_cd_nm, String p_cnt, String p_max_grnd_cnt, String p_total_area);
		
	}
	public interface InsertInsurance{

		void fireinsurance_insert(
				String quote_no,
				String building_type,
				String address_api,
				String group_ins,
				String bld_nm,
				String dong_info,
				String main_purps_cd_nm,
				String new_plat_plc,
				String etc_purps,
				String use_apr_day,
				String etc_roof,
				String dong_nm,
				String tot_area,
				String cnt_sedae,
				String grnd_flr_cnt,
				String ugrnd_flr_cnt,
				String etc_strct,
				String cover,
				String detail
			);
		
	}
	abstract public GetLicence getLicence();
	
	abstract public GetInformation getProducts();
	
	abstract public GetListByID getPremiums();
	
	abstract public GetBuildingType getBuildingType();
	
	abstract public InsertInsurance getInsuranceRegistration();
}
