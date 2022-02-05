package toyproject.runningmate.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CrewService {

    private final CrewRepository crewRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final EntityManager em;
    private final UserService userService;

    @Transactional
    public Long save(String email, CrewDto crewDto) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));

        if(user.getCrew() != null){
            throw new IllegalArgumentException("이미 크루 존재");
        }

        Crew crew = Crew.builder()
                .crewLeaderId(user.getId())
                .crewRegion(crewDto.getCrewRegion())
                .openChat(crewDto.getOpenChat())
                .crewName(crewDto.getCrewName())
                .explanation(crewDto.getExplanation())
                .build();

        user.addCrew(crew);
        user.setCrewLeader(true);

        crewRepository.save(crew);

        return crew.getId();  // Entity로 저장
    }

    public CrewDto getCrewInfo(String crewName) {
        Crew crew = em.createQuery(
                "select distinct c from Crew c" +
                        " join fetch c.users" +
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
    public void rejectUser(String userName){
        RequestUserToCrew requestUserToCrew = getRequestEntity(userName);

        requestRepository.delete(requestUserToCrew);
    }

    @Transactional
    public void admitUser(String userName) {
        RequestUserToCrew req = em.createQuery(
                "select r from RequestUserToCrew  r" +
                        " join fetch r.crew" +
                        " where r.nickName=:name", RequestUserToCrew.class)
                .setParameter("name", userName)
                .getSingleResult();
        User user = userService.getUserEntity(userName);

        user.addCrew(req.getCrew());

        //"요청"을 수락한 뒤 "요청" 삭제
        requestRepository.delete(req);
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
    public void changeCrewLeader(String leaderEmail, String userName) {
        User leader = em.createQuery(
                "select u from User u" +
                        " join fetch u.crew c" +
                        " where u.email=:email", User.class)
                .setParameter("email", leaderEmail)
                .getSingleResult();

        User nonleader = userService.getUserEntity(userName);

        log.info("leader name: {}", leader.getNickName());
        log.info("nonleader name: {}", nonleader.getNickName());

        leader.setCrewLeader(false);
        nonleader.setCrewLeader(true);

        leader.getCrew().changeCrewLeaderId(nonleader.getId());
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
        log.info("userName = {}", userName);
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