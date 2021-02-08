<%@ page import="kcb.org.json.*" %>
<%@ page language="java" contentType="text/html; charset=euc-kr" pageEncoding="euc-kr"%>

<title>KCB 휴대폰 본인확인 서비스 샘플 3</title>
<script type="text/javascript" >
	function fncOpenerSubmit() {
		opener.document.kcbResultForm.CP_CD.value = "<%=CP_CD%>";
		opener.document.kcbResultForm.TX_SEQ_NO.value = "<%=TX_SEQ_NO%>";
		opener.document.kcbResultForm.RSLT_CD.value = "<%=RSLT_CD%>";
		opener.document.kcbResultForm.RSLT_MSG.value = "<%=RSLT_MSG%>";
		
		//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
		//' 주의. 개인정보에 해당하는 내용임.
		//' 불필요하게 노출되지 않도록 주의 필요. (resultData 부분 변수선언 추가 필요함)
	    //'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
		opener.document.kcbResultForm.RSLT_NAME.value = "<%=RSLT_NAME%>";
		opener.document.kcbResultForm.RSLT_BIRTHDAY.value = "<%=RSLT_BIRTHDAY%>";
		opener.document.kcbResultForm.RSLT_SEX_CD.value = "<%=RSLT_SEX_CD%>";
		opener.document.kcbResultForm.RSLT_NTV_FRNR_CD.value = "<%=RSLT_NTV_FRNR_CD%>";
		
		opener.document.kcbResultForm.DI.value = "<%=DI%>";
		opener.document.kcbResultForm.CI.value = "<%=CI%>";
		opener.document.kcbResultForm.CI_UPDATE.value = "<%=CI_UPDATE%>";
		opener.document.kcbResultForm.TEL_COM_CD.value = "<%=TEL_COM_CD%>";
		opener.document.kcbResultForm.TEL_NO.value = "<%=TEL_NO%>";
		
		opener.document.kcbResultForm.RETURN_MSG.value = "<%=RETURN_MSG%>";

		opener.document.kcbResultForm.action = "phone_popup4.jsp";
		opener.document.kcbResultForm.submit();
		
		self.close();
	}	
</script>
</head>
<body>
</body>
<%
	//인증결과 복호화 성공
	// 인증결과를 확인하여 페이지분기등의 처리를 수행해야한다.
	if ("B000".equals(RSLT_CD)) {
		out.println("<script>alert('본인인증성공'); fncOpenerSubmit();</script>");
	}
	else {
		out.println("<script>alert('본인인증실패 : " + RSLT_CD + " : " + RSLT_MSG + "'); fncOpenerSubmit();</script>");
	}
%>
</html>
