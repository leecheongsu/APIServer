package com.insrb.app.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.insrb.app.util.InsuStringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@WebFilter(urlPatterns = "/*")
@Order(1)
public class AuthFilter implements Filter {

	// base64 of "Copyright ⓒ insurobo.co.kr All rights reserved."
	private static String SERVICE_KEY = "Q29weXJpZ2h0IOKTkiBpbnN1cm9iby5jby5rciBBbGwgcmlnaHRzIHJlc2VydmVkLg==";

	/**
	 * @param request
	 * @param response
	 * @param chain
	 * @throws IOException
	 * @throws ServletException
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String service_key = req.getHeader("X-insr-servicekey");
		log.info("Auth Filter: {}: {}, {}", req.getMethod(), req.getRequestURI(), service_key);

		if (service_key == null) {
			res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "invalid access.");
		} else if (!InsuStringUtil.equals(service_key, SERVICE_KEY)) {
			res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "invalid service key.");
		} else {
			chain.doFilter(request, response);
		}
	}

	/**
	 * 2021.02.04 JUEUS 에서는 오류나서 이부분 막아야 한다. Tmax 기술지원, 조성환 매니저.
	 */
	@Override
	public void destroy() {
		// Filter.super.destroy();
	}

	/**
	 * 2021.02.04 JUEUS 에서는 오류나서 이부분 막아야 한다. Tmax 기술지원, 조성환 매니저.
	 * @param filterConfig
	 * @throws ServletException
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// Filter.super.init(filterConfig);
	}
}
