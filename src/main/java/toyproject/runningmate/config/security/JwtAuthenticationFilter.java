package toyproject.runningmate.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.GenericFilterBean;
import toyproject.runningmate.exception.ExpiredTokenException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate redisTemplate;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

//      헤더에서 JWT를 받아온다.
        System.out.println("JwtAuthenticationFilter.doFilter");

        System.out.println(request.toString());
        HttpServletRequest newRequest = (HttpServletRequest) request;
        System.out.println("newRequest.getHeader(\"X-AUTH-TOKEN\") = " + newRequest.getHeader("X-AUTH-TOKEN"));
        System.out.println("newRequest.getMethod() = " + newRequest.getMethod());
        System.out.println("newRequest.getAuthType() = " + newRequest.getAuthType());
        System.out.println("newRequest.getRequestURI() = " + newRequest.getRequestURI());
        System.out.println("newRequest.getContentType() = " + newRequest.getContentType());

        String token = jwtTokenProvider.resolveToken((HttpServletRequest) request);
        System.out.println(token);

        //유효한 토큰인지 확인
        if (token != null && jwtTokenProvider.validateToken(token)) {

            // logout된 토큰인지 확인하는 절차
            String isLogout = (String) redisTemplate.opsForValue().get(token);

            if (ObjectUtils.isEmpty(isLogout)) {
                //토큰이 유효하면 토큰으로부터 유저 정보를 받아온다.
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                //SecurityContext에 Authentication 객체를 저장한다.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            else {
                throw new ExpiredTokenException("로그아웃 된 토큰입니다");
            }

        }

        final HttpServletResponse res = (HttpServletResponse) response;

        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE");
        res.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        res.setHeader("Access-Control-Max-Age", "3600");
        if ("OPTIONS".equalsIgnoreCase(((HttpServletRequest) request).getMethod())) {
            res.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(request, res);
        }
    }
}