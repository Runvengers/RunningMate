package toyproject.runningmate.domain.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import toyproject.runningmate.domain.crew.Crew;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class RequestUserToCrew {

    @Id
    @GeneratedValue
    @Column(name = "REQUEST_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREW_ID")
    private Crew crew;

    @Column(name="NICK_NAME")
    private String nickName;

    @Builder
    public RequestUserToCrew(String nickName) {
        this.nickName = nickName;
    }

    public void addCrew(Crew crew) {
        this.crew = crew;
        crew.getRequests().add(this);
    }
}