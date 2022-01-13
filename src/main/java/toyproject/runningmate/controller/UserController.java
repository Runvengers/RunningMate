package toyproject.runningmate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import toyproject.runningmate.config.security.JwtTokenProvider;
import toyproject.runningmate.domain.friend.FriendShip;
import toyproject.runningmate.domain.friend.FriendStatus;
import toyproject.runningmate.domain.user.User;
import toyproject.runningmate.dto.FriendShipDto;
import toyproject.runningmate.dto.LoginDto;
import toyproject.runningmate.dto.UserDto;
import toyproject.runningmate.service.FriendShipService;
import toyproject.runningmate.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    /**
     *회원가입
     *
     */
    @PostMapping("/join")
    @Transactional
    public Long join(@RequestBody LoginDto loginDto) {
        return userService.join(loginDto);
    }

    /**
     * 로그인
     *
     * 토큰 + userDto
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginDto loginDto) {

        LoginDto findLoginDto = userService.getUserByEmail(loginDto.getEmail());

        if (!passwordEncoder.matches(loginDto.getPassword(), findLoginDto.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호");
        }

        String token = jwtTokenProvider.createToken(findLoginDto.getEmail(), findLoginDto.getRoles());

        UserDto userDto = findLoginDto.loginDtoToUserDto();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("token", token);
        userInfo.put("userDto", userDto);

        return userInfo;
    }

    /**
     * 마이페이지
     *
     * 로그인 시
     * FE에서 토큰 주고
     * BE에서 userDto(유저 정보)
     */
    @GetMapping("/mypage")
    public UserDto getMyPage(HttpServletRequest request){
        return userService.getUserByToken(request);
    }

    /**
     * 회원 삭제
     *
     * FE에서 닉네임을 줌
     * BE에서 닉네임으로 유저 찾고 삭제
     *
     */
    @DeleteMapping("/user/{nickName}")
    public ResponseEntity<String> deleteUser(HttpServletRequest request) {
        userService.deleteUserById(userService.getUserByToken(request).getId());
        return ResponseEntity.ok("삭제 완료");
    }

    /**
     * 회원 정보 수정 ( 닉네임 또는 이메일)
     *
     * FE에서 토큰, 변경할 nickName, address
     * BE에서 수정
     *
     */
    @PostMapping("/user")
    public ResponseEntity<String> updateUser(HttpServletRequest request,  @RequestBody UserDto userDto) {

        UserDto findUserDto = userService.getUserByToken(request);

        //닉네임을 변경하려는 시도라면
        if(!findUserDto.getNickName().equals(userDto.getNickName())) {
            if (userService.isExistNickName(userDto.getNickName())) {
                return new ResponseEntity<>("중복된 닉네임입니다.", HttpStatus.CONFLICT);
            }
        }

        //바꿀 값
        userService.updateUser(findUserDto.getNickName(), userDto);

        return ResponseEntity.ok("수정 완료");
    }

    /**
     * 다른 사람 or 자기 자신의 프로필을 눌러 정보 조회할 경우
     *
     * 3번 유저가 2번 유저 프로필 볼 때 -> 2번 유저 정보 조회
     * 3번 유저가 3번 유저 프로필 볼 때 -> mypage로 리다이렉트
     *
     * FE에서 nickName(2번 유저), token(3번 유저, 현재 로그인 상태)
     * BE에선 닉네임을 비교하여 같으면 mypage, 다르면 현재 선택한 정보 조회
     **/
    @GetMapping("/user/{nickName}")
    public ResponseEntity<UserDto> getOtherUser(HttpServletRequest request, @PathVariable("nickName") String nickName) {
        UserDto userDto = userService.getUserByToken(request);

        if(userDto.getNickName().equals(nickName)){
            //같으면 mypage로 redirect
            return ResponseEntity.ok(userDto);
        }

        UserDto findMemberDto = userService.getUserByNickName(nickName);

        return ResponseEntity.ok().body(findMemberDto);
    }

    @GetMapping("/user/token/validate")
    public ResponseEntity<String> validateToken(HttpServletRequest request){

        String token = request.getHeader("X-AUTH-TOKEN");

        if(jwtTokenProvider.validateToken(token)) return ResponseEntity.ok().body("ok");

        return ResponseEntity.ok().body("토큰 만료");

    }
}