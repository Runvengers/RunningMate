package toyproject.runningmate.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import toyproject.runningmate.domain.crew.Crew;
import toyproject.runningmate.dto.CrewDto;
import toyproject.runningmate.repository.CrewRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CrewService {

    private final CrewRepository crewRepository;

    @Transactional
    public Long create(CrewDto crewDto) {
     return crewRepository.save(crewDto.toEntity()).getId();
    }


    public List<Crew> findCrews(){return crewRepository.findAll();}

    public Optional<Crew> findOne(Long crewId){return crewRepository.findById(crewId);}

    private CrewDto convertEntityToDto(Crew crew) {
        return CrewDto.builder()
                .id(crew.getId())
                .leaderId(crew.getLeaderId())
                .name(crew.getName())
                .build();
    }

}

