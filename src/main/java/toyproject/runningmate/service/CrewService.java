package toyproject.runningmate.service;


import lombok.RequiredArgsConstructor;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    public Long save(UserDto userDto, CrewDto crewDto) {

        User user = userService.getUserEntity(userDto.getNickName());

        Crew crew = Crew.builder()
                .crewLeaderId(userDto.getId())
                .crewRegion(crewDto.getCrewRegion())
                .openChat(crewDto.getOpenChat())
                .crewName(crewDto.getCrewName())
                .explanation(crewDto.getExplanation())
                .build();

        user.addCrew(crew);
        crewRepository.save(crew);

        return crew.getId();  // Entity로 저장
    }

    public CrewDto getCrewInfo(String crewName) {
        Crew crew = em.createQuery(
                "select distinct c from Crew c" +
                        " join fetch c.users" +
                        " left outer join fetch c.requests" +
                        " where c.crewName = :name", Crew.class)
                .setParameter("name", crewName)
                .getSingleResult();

        CrewDto crewDto = crew.toCrewDto();
        crewDto.setUserDtos(crew.getUsers().stream()
                .map(o -> o.toUserDto())
                .collect(Collectors.toList())
        );

        crewDto.setRequestUsers(crew.getRequests().stream()
                .map(o -> o.getNickName())
                .collect(Collectors.toSet())
        );

        return crewDto;
    }

    public CrewDto getCrewByName(String crewName) {

        Crew crew = getCrewEntity(crewName);

        if(crew.isDeleteFlag()){
            throw new IllegalArgumentException("이미 삭제된 크루입니다");
        }

        return crew.toCrewDto();
    }

    @Transactional
    public Long saveRequest(String userName, String crewName) {
        RequestUserToCrew requestUserToCrew = RequestUserToCrew.builder()
                .nickName(userName)
                .build();

        Crew crew = getCrewEntity(crewName);

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

    //크루 삭제면 UserDto에 있는 crewName, User에 있는 isCrewLeader 변경
    @Transactional
    public void deleteCrew(String crewName) {
        Crew deletedCrew = getCrewEntity(crewName);
        Long crewLeaderId = deletedCrew.getCrewLeaderId();

        User leader = userRepository.findById(crewLeaderId)
                .orElseThrow(() -> new IllegalArgumentException("존재X"));

        leader.setCrewLeader(!leader.isCrewLeader());

        for (User user : deletedCrew.getUsers()) {
            user.deleteCrew();
        }

        deletedCrew.getUsers().clear();
        deletedCrew.getRequests().clear();
        deletedCrew.setDeleteFlag(true);
        deletedCrew.changeCrewName("DUMMYCREWNAME" + deletedCrew.getId());
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

    public List<CrewDto> findByPage(int offset, int limit) {
        return em.createQuery("select c from Crew c", Crew.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList().stream()
                .map(a -> a.toCrewDto())
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