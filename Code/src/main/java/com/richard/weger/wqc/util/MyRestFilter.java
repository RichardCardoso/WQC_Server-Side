package com.richard.weger.wqc.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ErrorResult.ErrorCode;
import com.richard.weger.wqc.result.ErrorResult.ErrorLevel;
import com.richard.weger.wqc.result.ResultService;

@Service
public class MyRestFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			chain.doFilter(request, response);
		} catch (Exception ex) {
			HttpServletResponse resp = (HttpServletResponse) response;
			HttpServletRequest req = (HttpServletRequest) request;
			HttpHeaders headers;
			if(req.getRequestURI().toLowerCase().contains("/rest/")) {
				ErrorResult err = new ErrorResult(ErrorCode.GENERAL_SERVER_FAILURE, "General server failure. Please contact your system's admin", ErrorLevel.SEVERE, getClass());
				headers = ResultService.getErrorHeaders(err);
				headers.forEach((k, v) -> resp.setHeader(k, v.get(0)));
				resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			}
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
