package toyproject.runningmate.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

//spring security를 사용하기 위해서는 spring security filter chain을 사용한다는 것을 명시해야함
//WebSecurityConfigurerAdapter를 상속받은 클래스에 아래 어노테이션 달면 된다.
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate redisTemplate;

    //암호화에 필요한 PasswordEncoder를 Bean 등록한다.
    @Bean
    public PasswordEncoder passwordEncoder() {
        System.out.println("WebSecurityConfig.passwordEncoder");
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    //authenticationManager를 Bean 등록한다.
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        System.out.println("WebSecurityConfig.authenticationManagerBean");
        return super.authenticationManagerBean();
    }

    //처음으로 작성
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        System.out.println("WebSecurityConfig.configure");
        http
                .httpBasic().disable() //rest api 만을 고려하여 기본 설정은 해제
                .csrf().disable() //csrf 보안 토큰 disable 처리
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                //토큰 기반 인증이므로 세션 역시 사용 안함
                .and()
                .cors()
                .and()
                .authorizeRequests() //요청에 대한 사용권한 체크
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/mypage").hasRole("USER")
                .antMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().permitAll() //그 외 나머지 요청은 누구나 접근 가능
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider,redisTemplate),
                        UsernamePasswordAuthenticationFilter.class);
        //JwtAuthenticationFilter를 UsernamePasswordAuthenicationFilter전에 넣는다.

    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
