package toyproject.runningmate.dto;

import lombok.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private Set<String> requestUsers= new HashSet<>();

    @Builder
    public CrewDto(Long id, Long crewLeaderId, String crewRegion, String openChat, String crewName, String explanation) {
        this.id = id;
        this.crewLeaderId = crewLeaderId;
        this.crewRegion = crewRegion;
        this.openChat = openChat;
        this.crewName = crewName;
        this.explanation = explanation;
    }
}