package toyproject.runningmate.domain.crew;

import lombok.*;
import toyproject.runningmate.domain.user.User;

import javax.persistence.*;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Builder
public class Crew {     // making crew              -> 후에 이름 CrewEntity로 바꿀것

    @Id
    @GeneratedValue
    @Column(name = "CREW_ID")
    private Long id;

    private Long leaderId;
    private String name;

}