package toyproject.runningmate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import toyproject.runningmate.domain.user.User;
import toyproject.runningmate.dto.UserDto;
import toyproject.runningmate.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {


    public UserDto getUserDto(User user) {
        UserDto userDto = UserDto.builder()
                .email(user.getEmail())
                .id(user.getId())
                .crew(user.getCrew())
                .nickName(user.getNickName())
                .regdate(user.getRegdate())
                .address(user.getAddress())
                .roles(user.getRoles())
                .build();

        return userDto;
    }
}
