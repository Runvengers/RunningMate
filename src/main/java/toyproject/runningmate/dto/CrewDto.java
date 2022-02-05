package toyproject.runningmate.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import toyproject.runningmate.domain.crew.Crew;
import toyproject.runningmate.domain.request.RequestUserToCrew;
import toyproject.runningmate.domain.user.User;

import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;



@Getter
@Setter
@NoArgsConstructor
public class CrewDto {

    private Long id;
    private Long crewLeaderId;
    private String crewRegion;
    private String openChat;
    private String crewName;
    private String explanation;
    private List<UserDto> userDtos = new ArrayList<>();
    private List<String> requestUsers= new ArrayList<>();

    @Builder
    public CrewDto(Long id, Long crewLeaderId, String crewRegion, String openChat, String crewName, String explanation) {
        this.id = id;
        this.crewLeaderId = crewLeaderId;
        this.crewRegion = crewRegion;
        this.openChat = openChat;
        this.crewName = crewName;
        this.explanation = explanation;
    }

    public Crew toEntity(){
        return Crew.builder()
                .crewLeaderId(crewLeaderId)
                .crewRegion(crewRegion)
                .openChat(openChat)
                .crewName(crewName)
                .explanation(explanation)
                .build();
    }
}