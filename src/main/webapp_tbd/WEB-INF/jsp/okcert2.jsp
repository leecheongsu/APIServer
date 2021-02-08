<%@ page import="kcb.org.json.*" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<html>
<title>KCB 휴대폰 본인확인 서비스 샘플 2</title>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script type="text/javascript">
	function request(){
		document.form1.action = "${popupUrl}";
		document.form1.method = "post";
	
		document.form1.submit();
	}
</script>
</head>

<body>
	<form name="form1">
	<!-- 인증 요청 정보 -->
	<!--// 필수 항목 -->
	<input type="hidden" name="tc" value="kcb.oknm.online.safehscert.popup.cmd.P931_CertChoiceCmd"/>	<!-- 변경불가-->
	<input type="hidden" name="cp_cd" value="${CP_CD}">	<!-- 회원사코드 -->
	<input type="hidden" name="mdl_tkn" value="${MDL_TKN}">	<!-- 토큰 --> 
	<input type="hidden" name="target_id" value="">	
	<!-- 필수 항목 //-->	
	</form>
</body>
<script>request();</script>
</html>


