package toyproject.runningmate.domain.user;


import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import toyproject.runningmate.domain.crew.Crew;

import toyproject.runningmate.domain.friend.FriendShip;
import toyproject.runningmate.dto.FriendShipDto;
import toyproject.runningmate.dto.LoginDto;
import toyproject.runningmate.dto.UserDto;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor
public class User implements UserDetails {

    //spring security는 UserDetails 객체를 통해 권한 정보를 관리하기 때문에
    //User 클래스에 UserDetails를 구현하고 추가 정보를 재정의 해야한다.
    //Entity와 UserDetails는 구분할 수도 같은 클래스에서 관리할 수도 있다.
    //여기에서는 같은 클래스에서 관리하는 방법을 사용
    @Id
    @GeneratedValue
    @Column(name = "USER_ID")
    private Long id;

    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    private String password;

    @Column(name = "NICK_NAME", nullable = false, unique = true)
    private String nickName;

    @Column(name = "REG_DATE")
    private LocalDateTime regDate;

    @Column(name = "ADDRESS", nullable = false)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREW_ID")
    private Crew crew;

    @Column(name = "IS_CREW_LEADER")
    private boolean isCrewLeader;

    @OneToMany(mappedBy = "sendUser")
    private List<FriendShip> friendShipList = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        System.out.println("User.getAuthorities");
        return this.roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Builder
    public User(String email, String password, String nickName, LocalDateTime regDate, String address, boolean isCrewLeader, List<String> roles) {
        this.email = email;
        this.password = password;
        this.nickName = nickName;
        this.regDate = regDate;
        this.address = address;
        this.isCrewLeader = isCrewLeader;
        this.roles = roles;
    }


    //이 메서드를 통해 spring security에서 사용하는 username을 가져간다.
    //우리는 email을 사용한다.

    @Override
    public String getUsername() {
        System.out.println("User.getUsername");
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        System.out.println("User.isAccountNonExpired");
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        System.out.println("User.isAccountNonLocked");
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        System.out.println("User.isCredentialsNonExpired");
        return true;
    }

    @Override
    public boolean isEnabled() {
        System.out.println("User.isEnabled");
        return true;
    }

    public void update(String nickName, String address){
        this.nickName = nickName;
        this.address = address;
    }

    //양방향 편의 메서드
    public void addCrew(Crew crew) {
        this.crew = crew;
        crew.getUsers().add(this);
    }

    public void setCrewLeader(boolean crewLeader) {
        isCrewLeader = crewLeader;
    }

    public void deleteCrew() {
        crew.getUsers().remove(this);
        this.crew = null;
    }

    public UserDto toUserDto() {        // Entity -> UserDto
        UserDto userDto = UserDto.builder()
                .id(id)
                .email(email)
                .nickName(nickName)
                .regDate(regDate)
                .address(address)
                .roles(roles)
                .isCrewLeader(isCrewLeader)
                .build();

        if(crew != null){
            userDto.setCrewName(crew.getCrewName());
        }

        return userDto;
    }

    public LoginDto toLoginDto() {        // Entity -> LoginDto
        LoginDto loginDto = LoginDto.builder()
                .id(id)
                .email(email)
                .nickName(nickName)
                .regDate(regDate)
                .address(address)
                .roles(roles)
                .isCrewLeader(isCrewLeader)
                .password(password)
                .build();
        return loginDto;
    }

    public List<FriendShipDto> userFriendShipListToDto(){
        return friendShipList.stream()
                .map(FriendShip::toFriendShipDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return isCrewLeader() == user.isCrewLeader() &&
                Objects.equals(getId(), user.getId()) &&
                Objects.equals(getEmail(), user.getEmail()) &&
                Objects.equals(getPassword(), user.getPassword()) &&
                Objects.equals(getNickName(), user.getNickName()) &&
                Objects.equals(getRegDate(), user.getRegDate()) &&
                Objects.equals(getAddress(), user.getAddress()) &&
                Objects.equals(getCrew(), user.getCrew()) &&
                Objects.equals(getRoles(), user.getRoles());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getEmail(), getPassword(), getNickName(), getRegDate(), getAddress(), getCrew(), isCrewLeader(), getRoles());
    }



}
