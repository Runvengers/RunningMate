package toyproject.runningmate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import toyproject.runningmate.config.security.JwtTokenProvider;
import toyproject.runningmate.domain.user.User;
import toyproject.runningmate.dto.UserDto;
import toyproject.runningmate.repository.UserRepository;
import toyproject.runningmate.service.UserService;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserService userService;


    //회원가입
    @PostMapping("/join")
    public Long join(@RequestBody Map<String, String> user) {
        return userRepository.save(User.builder()
                .email(user.get("email"))
                .password(passwordEncoder.encode(user.get("password")))
                .roles(Collections.singletonList("ROLE_USER")) // 최초 가입시 USER 로 설정
                .build()).getId();
    }
    //로그인
    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> user) {
        User member = userRepository.findByEmail(user.get("email"))
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일"));
        if (!passwordEncoder.matches(user.get("password"), member.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호");
        }
        return jwtTokenProvider.createToken(member.getUsername(), member.getRoles());
    }

    @GetMapping("/user")
    public String hi() {
        return "hi user";
    }

    @GetMapping("/admin")
    public String hello() {
        return "hello admin";
    }

    @GetMapping("/mypage")
    public UserDto getMyPage(HttpServletRequest request){

        String token = request.getHeader("X-AUTH-TOKEN");

        String userEmail = jwtTokenProvider.getUserPk(token);

        User member = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));

        UserDto userDto = userService.getUserDto(member);

        return userDto;

        //header : token -> 해석 ->   "email" : "hyeonwoo"
    }

    /**
     * 마이페이지에서 삭제 가능
     * -> 예외처리 할 필요 X
     * @param userId
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<String> deleteUser(@RequestParam Long userId) {
        userRepository.deleteById(userId);

        return ResponseEntity.ok("삭제 완료");
    }

    /**
     * FE에서 id, (변경할) email, nickName, address를 요청
     * @param user
     * @return
     */
    @Transactional
    @PostMapping("/user/{userId}")
    public ResponseEntity<String> updateUser(@RequestBody Map<String, String> user) {
        User member = userRepository.findById(Long.parseLong(user.get("id")))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));

        //이메일 중복확인
        boolean isExistEmail = userRepository.findByEmail(user.get("email")).isEmpty();

        if(!isExistEmail){
            throw new IllegalArgumentException("중복된 메일입니다.");
        }

        member.update(user.get("email"), user.get("nickName"), user.get("address"));

        return ResponseEntity.ok("수정 완료");
    }
    /**
     * 3번 유저가 2번 유저 프로필 볼 때
     *
     * FE에서 nickName(2번 유저), token(3번 유저, 현재 로그인 상태)
     */

}