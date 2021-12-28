package toyproject.runningmate.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import toyproject.runningmate.domain.crew.Crew;
import toyproject.runningmate.domain.request.RequestUserToCrew;
import toyproject.runningmate.domain.user.User;
import toyproject.runningmate.dto.CrewDto;
import toyproject.runningmate.dto.UserDto;
import toyproject.runningmate.repository.CrewRepository;
import toyproject.runningmate.repository.RequestRepository;
import toyproject.runningmate.repository.UserRepository;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CrewService {

    private final CrewRepository crewRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final EntityManager em;
    private final UserService userService;

    @Transactional
    public Long save(UserDto userDto, CrewDto crewDto) { // Dto로 받아서

        User findUserEntity = userService.getUserEntity(userDto.getNickName());

        crewDto.setCrewLeaderId(userDto.getId());

        Crew crewEntity = crewDto.toEntity();
        findUserEntity.addCrew(crewEntity);
        crewRepository.save(crewEntity);

        return crewEntity.getId();  // Entity로 저장
    }

    public CrewDto getCrewByName(String crewName) {

        Crew crew = getCrewEntity(crewName);

        if(crew.isDeleteFlag()){
            throw new IllegalArgumentException("이미 삭제된 크루입니다");
        }

        return crew.toCrewDto();
    }

    public List<UserDto> getCrewMembersByCrewName(String crewName){
        Crew crew = getCrewEntity(crewName);

        return crew.userEntityListToDtoList();
    }

    @Transactional
    public Long saveRequest(UserDto userDto, CrewDto crewDto) {
        RequestUserToCrew requestUserToCrew = RequestUserToCrew.builder()
                .nickName(userDto.getNickName())
                .build();

        Crew crew = getCrewEntity(crewDto.getCrewName());
        requestUserToCrew.addCrew(crew);

        requestRepository.save(requestUserToCrew);

        return requestUserToCrew.getId();
    }

    @Transactional
    public void rejectUser(String userNickName){
        RequestUserToCrew requestUserToCrew = getRequestEntity(userNickName);

        requestRepository.delete(requestUserToCrew);
    }

    @Transactional
    public void admitUser(String userNickName) {
        RequestUserToCrew requestUserToCrew = getRequestEntity(userNickName);

        //가입할 크루
        Crew crew = requestUserToCrew.getCrew();

        //가입할 회원
        User findUser = userService.getUserEntity(userNickName);

        findUser.addCrew(crew);
    }

    public List<String> getRequestList(String crewName) {
        Crew findCrew = getCrewEntity(crewName);

        List<String> requests = new ArrayList<>();

        for (RequestUserToCrew request : findCrew.getRequests()) {
            String nickName = request.getNickName();
            requests.add(nickName);
        }

        return requests;
    }

    //크루 삭제면 UserDto에 있는 crewName, User에 있는 isCrewLeader 변경
    @Transactional
    public void deleteCrew(String crewName) {
        Crew deletedCrew = getCrewEntity(crewName);
        Long crewLeaderId = deletedCrew.getCrewLeaderId();

        User leader = userRepository.findById(crewLeaderId)
                .orElseThrow(() -> new IllegalArgumentException("존재X"));

        leader.setCrewLeader(!leader.isCrewLeader());

        System.out.println(deletedCrew.getUsers().size());

        //크루 멤버들의 crew 연관관계를 깨야함
        for (User user : deletedCrew.getUsers()) {
            user.deleteCrew();
        }

        deletedCrew.setDeleteFlag(true);
        deletedCrew.changeCrewName("DUMMYCREWNAME" + deletedCrew.getId());
        deletedCrew.getRequests().clear();
    }

    //위임 현재 유저(토큰가지고 있는 얘가 리더), 파라미터로 들어오는 얘가 리더가 될 얘
    @Transactional
    public void changeCrewLeader(String leaderName, String userName) {
        User leader = userService.getUserEntity(leaderName);
        User user = userService.getUserEntity(userName);

        leader.setCrewLeader(false);
        user.setCrewLeader(true);

        Crew crew = getCrewEntity(user.getCrew().getCrewName());

        crew.changeCrewLeaderId(user.getId());
    }

    //crewName 변경
    @Transactional
    public void changeCrewName(String crewName, String newName) {
        Crew crew = getCrewEntity(crewName);

        //unique 속성으로 예외 가능
        crew.changeCrewName(newName);
    }

    @Transactional
    public void changeMember(String userName){
        User user = userService.getUserEntity(userName);
        user.deleteCrew();
    }

    public List<CrewDto> findByPage(PageRequest pageRequest) {
        return crewRepository.findAll(pageRequest).stream()
                .map(Crew::toCrewDto)
                .collect(Collectors.toList());
    }

    public Crew getCrewEntity(String crewName) {
        return crewRepository.findByCrewName(crewName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 크루"));
    }

    public RequestUserToCrew getRequestEntity(String nickName) {
        return requestRepository.findByNickName(nickName)
                .orElseThrow(() -> new IllegalArgumentException("요청한 적 없음"));
    }

}