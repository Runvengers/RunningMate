package toyproject.runningmate.dto;


import lombok.*;
import toyproject.runningmate.domain.crew.Crew;

@Getter @Setter
@NoArgsConstructor
public class CrewDto {

    private Long id;
    private Long leaderId;
    private String name;

    public Crew toEntity(){
        Crew crew = Crew.builder()
                .id(id)
                .leaderId(leaderId)
                .name(name)
                .build();
        return crew;
    }

    @Builder
    public CrewDto(Long id, Long leaderId, String name) {
        this.id = id;
        this.leaderId=leaderId;
        this.name=name;
    }



}
