package toyproject.runningmate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import toyproject.runningmate.dto.CrewDto;
import toyproject.runningmate.dto.UserDto;
import toyproject.runningmate.service.CrewService;
import toyproject.runningmate.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


@RestController
@RequiredArgsConstructor
public class CrewController {

    private final CrewService crewService;
    private final UserService userService;

    //페이징
    @GetMapping("/crews")
    public List<CrewDto> getCrews(@RequestParam("offset") int offset,
                                  @RequestParam("limit") int limit){
        return crewService.findByPage(offset, limit);
    }


    // '크루 참여 여부'는 로그인시 주었던 정보로 이미 검증이 이루어진다.

    /**
     * FE : 토큰 + crew 정보
     * BE
     * 크루 생성
     * 크루 만든 유저 -> 크루 리더 등록
     */
    @PostMapping("/crew/new")
    public Long createCrewV1(HttpServletRequest request, @RequestBody CrewDto crewDto) {

        String email = userService.getEmailByToken(request);

        return crewService.save(email, crewDto);
    }

    /**
     * 크루 상세 페이지
     *
     * FE : 크루 네임 전달
     *
     * BE : 크루 이름을 통해 크루Dto 반환
     */
    @GetMapping("/crews/{crew-name}")
    public CrewDto getCrewPage(@PathVariable("crew-name") String crewName){
        return crewService.getCrewInfo(crewName);
    }

    /**
     * 크루 신청
     *
     * FE : 크루 이름과 신청할 유저(토큰)
     *
     * BE : 크루를 찾고 해당 크루에게 요청
     */
    @PostMapping("/crews/{crew-name}/request")
    public Long registCrew(HttpServletRequest request, @PathVariable("crew-name") String crewName) {
        String userName = userService.getEmailByToken(request);

        return crewService.saveRequest(userName, crewName);
    }

    /**
     * 크루 신청 거절
     *
     * FE : 유저 이름
     *
     * BE : 요청 거절
     */
    @DeleteMapping("/crews/users/{user-name}/request")
    public ResponseEntity reject(@PathVariable("user-name") String nickName) {
        crewService.rejectUser(nickName);
        return ResponseEntity.ok("삭제 완료");
    }

    /**
     * 크루 신청 승낙
     *
     * FE : 유저 이름
     *
     * BE : 요청 수락
     *
     */
    @PostMapping("/crews/users/{user-name}/request")
    public ResponseEntity admit(@PathVariable("user-name") String nickName){

        crewService.admitUser(nickName);

        return ResponseEntity.ok("추가 완료");
    }

    /**
     * 크루 삭제
     *
     * FE : crew 이름
     *
     * BE : 크루를 삭제(soft delete)
     *
     */
    @DeleteMapping("/crews/{crew-name}")
    public ResponseEntity deleteCrew(@PathVariable("crew-name") String crewName) {
        crewService.deleteCrew(crewName);
        return ResponseEntity.ok("삭제 완료");
    }

    /**
     * 크루장 위임
     *
     * FE : 토큰(리더) , 크루장을 줄 username
     *
     * BE : 위임
     *xx
     */
    @PatchMapping("/crew/users/{user-name}/edit")
    public ResponseEntity changeCrewLeader(HttpServletRequest request,
                                           @PathVariable("user-name") String userName){

        String leaderEmail = userService.getEmailByToken(request);
        crewService.changeCrewLeader(leaderEmail,userName);

        return ResponseEntity.ok("위임 완료");
    }

    /**
     * 크루 이름 변경
     *
     * FE : 현재 크루 이름, 바꿀 크루 이름
     *
     * BE : 크루 이름 변경
     */
    @PostMapping("/crews/{crew-name}")
    public ResponseEntity changeCrewName(@PathVariable("crew-name") String crewName,
                                         @RequestBody String newName){

        crewService.changeCrewName(crewName, newName);

        return ResponseEntity.ok("이름 변경 완료");
    }


    /**
     * 크루원 추방 or 자진 탈퇴
     *
     * FE : 유저 이름
     *
     * BE : 해당 크루에서 유저 삭제
     */

    @PostMapping("/crew/users/{user-name}/edit")
    public ResponseEntity changeMember(@PathVariable("user-name") String userName) {
        crewService.changeMember(userName);
        return ResponseEntity.ok("추방 완료");
    }

    // 크루탈퇴 : 크루원이 1명인데(크루장) 탈퇴를 누를 때
}