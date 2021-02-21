package com.insrb.app.mapper;

import java.util.Map;

public interface IN101TMapper {
	Map<String, Object> selectById(String quote_no);

	void insert(
		String quote_no,
		String email,
		String agmtkind,
		String bldtotlyrnum,
		String hsarea,
		String lsgccd,
		String polestrc,
		String roofstrc,
		String otwlstrc,
		String objcat,
		String bldfloors1,
		String bldfloors2,
		String lobzcd,
		String gitdtarifcat1,
		String objtypcd1,
		String objtypcd2,
		String objtypcd3,
		String elagorgninsdamt1,
		String elagorgninsdamt2,
		String elagorgninsdamt3,
		String ptykornm,
		String telcat,
		String telno,
		String ptybiznm,
		String bizno,
		String objonnaddrcat,
		String objzip,
		String objaddr1,
		String objaddr2,
		String objroadnmcd,
		String objtrbdcd,
		String objtrbdaddr,
		String partnerno,
		String insstdt,
		String inseddt,
		String inssttm,
		String insedtm,
		String prdins,
		String tpymprem,
		String perprem,
		String govtprem,
		String lgovtprem,
		String applno,
		String scno,
		String purpose,
		String localurltmp,
		String mappingno,
		String sessionexectime,
		String sessionid,
		String certconfmseqno
	);
	void delete(String quote_no);

	void updateEsignurl(String quote_no,String esignurl);
}
