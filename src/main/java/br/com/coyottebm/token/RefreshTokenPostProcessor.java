package br.com.coyottebm.token;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import br.com.coyottebm.config.property.CoyotteBetManagerApiProperty;


/**
 * Executado após a criação do Refresh Token.
 * Remove o Refresh Token do corpo da resposta e adiciona no cookie
 */
@SuppressWarnings("deprecation")
@ControllerAdvice
public class RefreshTokenPostProcessor implements ResponseBodyAdvice<OAuth2AccessToken> {
	
	@Autowired
	private CoyotteBetManagerApiProperty property;
	
	@Override
	public boolean supports(MethodParameter returnType, 
			                Class<? extends HttpMessageConverter<?>> converterType) {
		return returnType.getMethod().getName().equals("postAccessToken");
	}
	
	//Será executado se o método supports retornar true
	@Override
	public OAuth2AccessToken beforeBodyWrite(OAuth2AccessToken body,
			                                 MethodParameter returnType,
											 MediaType selectedContentType, 
											 Class<? extends HttpMessageConverter<?>> selectedConverterType,
											 ServerHttpRequest request, 
											 ServerHttpResponse response) {
		
		HttpServletRequest req = ((ServletServerHttpRequest) request).getServletRequest();
		HttpServletResponse resp = ((ServletServerHttpResponse) response).getServletResponse();
		
		String refreshToken = body.getRefreshToken().getValue();
		
		DefaultOAuth2AccessToken token = (DefaultOAuth2AccessToken) body;
		
		adicionarRefreshTokenNoCookie(refreshToken, req, resp);
		removerRefreshTokenDoBody(token);
		
		return body;
	}

	private void removerRefreshTokenDoBody(DefaultOAuth2AccessToken token) {
		token.setRefreshToken(null);
	}

	private void adicionarRefreshTokenNoCookie(String refreshToken, HttpServletRequest req, HttpServletResponse resp) {
		
		Cookie cookie = new Cookie("refreshToken", refreshToken); 
		
		cookie.setHttpOnly(true);
		cookie.setSecure(property.isHabilitaHttps()); 
		cookie.setPath(req.getContextPath().concat("/oauth/token"));
		cookie.setMaxAge(2592000);
		
		resp.addCookie(cookie);
	}

}
