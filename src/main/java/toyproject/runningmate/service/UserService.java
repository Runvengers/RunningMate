package toyproject.runningmate.service;

import lombok.RequiredArgsConstructor;
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
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final FriendShipRepository friendShipRepository;
    private final PasswordEncoder passwordEncoder;

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

    //이메일에서 User 얻기
    public LoginDto getUserByEmail(String email) {
        return userRepository.findByEmail(email).
                orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원")).toLoginDto();
    }

    //수정
    @Transactional
    public void updateUser(String nickName, UserDto changeUserDto) {
        User findUser = getUserEntity(nickName);

        findUser.update(changeUserDto.getNickName(), changeUserDto.getAddress());
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
                .build();

        userRepository.save(member);

        return member.getId();
    }

    @Transactional
    public void updateCrewLeaderStatus(String nickName){
        User findUser = getUserEntity(nickName);

        findUser.setCrewLeader(!findUser.isCrewLeader());
    }

    public boolean hasCrew(String nickName){
        User user = getUserEntity(nickName);

        if(user.getCrew() != null) return true;

        return false;
    }

    public User getUserEntity(String nickName) {
        return userRepository.findByNickName(nickName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));
    }

}
