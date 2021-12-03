package toyproject.runningmate.domain.user;


import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import toyproject.runningmate.domain.crew.Crew;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    //spring security는 UserDetails 객체를 통해 권한 정보를 관리하기 때문에
    //User 클래스에 UserDetails를 구현하고 추가 정보를 재정의 해야한다.
    //Entity와 UserDetails는 구분할 수도 같은 클래스에서 관리할 수도 있다.
    //여기에서는 같은 클래스에서 관리하는 방법을 사용
    @Id
    @GeneratedValue
    private Long id;

    private String email;
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREW_ID")
    private Crew crew;

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        System.out.println("User.getAuthorities");
        return this.roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
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
}
