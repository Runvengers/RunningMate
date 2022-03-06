package toyproject.runningmate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import toyproject.runningmate.config.security.JwtTokenProvider;
import toyproject.runningmate.domain.user.User;
import toyproject.runningmate.dto.LoginDto;
import toyproject.runningmate.dto.UserDto;
import toyproject.runningmate.repository.FriendShipRepository;
import toyproject.runningmate.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final FriendShipRepository friendShipRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate redisTemplate;

    //토큰에서 User 추출
    public UserDto getUserByToken(HttpServletRequest request) {

        String token = request.getHeader("X-AUTH-TOKEN");
        String userEmail = jwtTokenProvider.getUserPk(token);

        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원")).toUserDto();
    }

    //토큰에서 메일만 추출
    public String getEmailByToken(HttpServletRequest request) {
        String token = request.getHeader("X-AUTH-TOKEN");
        return jwtTokenProvider.getUserPk(token);
    }

    //닉네임에서 User 얻기
    public UserDto getUserByNickName(String nickName) {
        return userRepository.findByNickName(nickName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원")).toUserDto();
    }

    @Transactional
    public void deleteUserById(Long userId){
        User deleteUser = userRepository.getById(userId);

        // 모든 친구/요청 삭제
        friendShipRepository.deleteUserAllFriendShip(deleteUser, deleteUser.getNickName());

        //유저삭제
        userRepository.deleteById(userId);
    }

    //중복 닉네임 확인
    public boolean isExistNickName(String nickName) {
        return userRepository.findByNickName(nickName).isPresent();
    }

    //수정
    @Transactional
    public UserDto updateUser(String nickName, UserDto changeUserDto) {
        User findUser = getUserEntity(nickName);

        if (!nickName.equals(changeUserDto.getNickName())) {
            if (isExistNickName(changeUserDto.getNickName())) {
                return null;
            }
        }

        findUser.update(changeUserDto);

        return findUser.toUserDto();
    }


    //회원가입
    @Transactional
    public Long join(LoginDto loginDto) {
        User member = User.builder()
                .email(loginDto.getEmail())
                .password(passwordEncoder.encode(loginDto.getPassword()))
                .roles(Collections.singletonList("ROLE_USER")) // 최초 가입시 USER 로 설정
                .address(loginDto.getAddress())
                .nickName(loginDto.getNickName())
                .regDate(LocalDateTime.now())
                .image(loginDto.getImage())
                .build();

        userRepository.save(member);

        return member.getId();
    }

    public UserDto login(LoginDto loginDto) {
        log.info("userEmail = {}", loginDto.getEmail());

        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일"));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호");
        }

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRoles());

        UserDto userDto = user.toUserDto();
        userDto.setToken(token);

        return userDto;
    }

    public User getUserEntity(String nickName) {
        return userRepository.findByNickName(nickName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));
    }

    public String validateToken(HttpServletRequest request) {

        String token = request.getHeader("X-AUTH-TOKEN");

        if (jwtTokenProvider.validateToken(token)) return "유효한 토큰";
        else return "만료된 토큰";
    }

    public String logout(HttpServletRequest request){
        String token = jwtTokenProvider.resolveToken(request);

        if(!jwtTokenProvider.validateToken(token)){ // 이미 만료됐거나, 잘못된 형식의 토큰일 때
            return "잘못된 요청입니다";
        }
        Long expiration = jwtTokenProvider.getExpiration(token);
        redisTemplate.opsForValue().set(token, "logout", expiration, TimeUnit.MILLISECONDS);
        return "로그아웃 성공";
    }

}
