package toyproject.runningmate.domain.crew;

import toyproject.runningmate.domain.user.User;

import javax.persistence.*;
import java.util.List;

@Entity
public class Crew {     // making crew

    @Id
    @GeneratedValue
    @Column(name = "CREW_ID")
    private Long id;
}
