package toyproject.runningmate.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import toyproject.runningmate.domain.crew.Crew;

import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class UserDto {

    private Long id;
    private String email;
    private String password;
    private String nickName;
    private LocalDateTime regdate;
    private String address;
    private Crew crew;
    private List<String> roles = new ArrayList<>();

    /*


     */


}
