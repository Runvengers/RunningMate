package toyproject.runningmate.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import toyproject.runningmate.config.security.JwtTokenProvider;
import toyproject.runningmate.domain.crew.Crew;
import toyproject.runningmate.domain.user.User;
import toyproject.runningmate.dto.CrewDto;
import toyproject.runningmate.exception.UserNullException;
import toyproject.runningmate.repository.CrewRepository;
import toyproject.runningmate.repository.UserRepository;
import toyproject.runningmate.service.CrewService;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor

public class CrewController {

    private final CrewService crewService;
    private final UserRepository userRepository;
    private final CrewRepository crewRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/crew/new")            // 이미 크루에 속해있는지 체크할 것
    public String createCrewForm(HttpServletRequest httpServletRequest) {    // body에 json형식으로 날라옴 email:kim123

        String tok = httpServletRequest.getHeader("X-AUTH-TOKEN");
        String userEmail = jwtTokenProvider.getUserPk(tok); // token으로부터 email 얻음

       User findUser = searchUserByEmail(userEmail);

       if(findUser.getCrew() != null){        // 크루가 속해있다면
            return "이미 크루가 존재합니다 : " + findUser.getCrew().getName();
      }
       return "ok";
    }

    @PostMapping("/crew/new")
    public String create(@RequestBody Map crewFormData){         // userId  + 크루명,지역,소개글  추가할 크루원

//        String email = (String) crewFormData.get("email");
//        String crewName = (String) crewFormData.get("crewName");
//        String location = (String) crewFormData.get("location");
//        String intro = (String) crewFormData.get("intro");
//        List crewList = (List) crewFormData.get("crewList");
//
//
//        // 크루생성 .. dto?
//        CrewDto crewDto = new CrewDto();
//
//
//        // 이제 이 회원은 크루에 속해있게 된다.
//        User findUser = searchUserByEmail(email);

        return "redirect:/";        // 크루 상세페이지로 리다이렉션
    }

    @GetMapping("/crew/new/emailSearch")     // 추가할 크루 멤버 검색(이메일)
    public String crewSearchByEmail(@RequestBody String email){

        return "emailSearch";
    }
    @GetMapping("/crew/new/friendSearch")  // 추가할 크루 멤버 검색(친구목록)
    public String crewSearchByFriendList(){

        return "friendSearch";
    }

    public User searchUserByEmail(String email){        // 실제로는 Dto 이용
        Optional<User> userOpt = userRepository.findByEmail(email);
        Long findUserId = userOpt.orElseThrow(UserNullException::new).getId();
        User findUser = userRepository.getById(findUserId);
        return findUser;
    }

}
