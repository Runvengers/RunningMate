package toyproject.runningmate.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private String secretKey = "jwtsecret";

    //유효기간 30분
    private long tokenValidTime = 30 * 60 * 1000L;

    private final UserDetailsService userDetailsService;

    //객체 초기화, secretKey를 Base64로 인코딩한다.
    @PostConstruct
    protected void init() {
        System.out.println("JwtTokenProvider.init");
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    //JWT 토큰 생성
    public String createToken(String userPk, List<String> roles) {
        System.out.println("JwtTokenProvider.createToken");
        Claims claims = Jwts.claims().setSubject(userPk); //JWT payload에 저장되는 정보단위
        System.out.println("userPk = " + userPk);
        claims.put("roles", roles); //정보는 key / value 쌍으로 저장
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims) //정보 저장
                .setIssuedAt(now) //토큰 발행 시간 정보
                .setExpiration(new Date(now.getTime() + tokenValidTime)) //만료기간 설정
                .signWith(SignatureAlgorithm.HS256, secretKey) //사용할 암호화 알고리즘,
                //signature에 들어갈 secret값 세팅
                .compact();
    }

    //JWT 토큰에서 인증 정보 조회
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserPk(token));
        System.out.println("JwtTokenProvider.getAuthentication");
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    //토큰에서 회원 정보 추출
    public String getUserPk(String token) {
        String userInfo = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
        System.out.println("userInfo = " + userInfo);
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    //Request의 Header에서 token 값을 가져온다. "X-AUTH-TOKEN" : "TOKEN 값"
    public String resolveToken(HttpServletRequest request) {
        System.out.println("JwtTokenProvider.resolveToken");
        return request.getHeader("X-AUTH-TOKEN");
    }

    //토큰의 유효성 + 만료일자 확인
    public boolean validateToken(String jwtToken) {
        try {
            System.out.println("JwtTokenProvider.validateToken");
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            System.out.println("만료됐다");
            return false;
        }
    }

    //  토큰의 만료시간 return
    public Long getExpiration(String jwtToken) {
        // accessToken 남은 유효시간
        Date expiration = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken).getBody().getExpiration();
        // 현재 시간
        Long now = new Date().getTime();
        return (expiration.getTime() - now);
    }


}