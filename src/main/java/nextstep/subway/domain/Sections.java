package nextstep.subway.domain;

import nextstep.subway.exception.RegistrationException;
import nextstep.subway.exception.ValidationException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Embeddable
public class Sections {
    @OneToMany(mappedBy = "line",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();
    private static final int DELETION_MINIMUM_SIZE = 1;

    public Sections() {
    }

    public List<Section> getSections() {
        return sections;
    }

    public Section getLastSection() {
        return sections.stream().reduce((first, second) -> second).orElse(null);
    }

    public void addSection(Section section) {
        sections.add(section);
    }

    public List<Station> createStationList() {
        return this.sections.stream()
                .flatMap(s -> Stream.of(s.getUpStation(), s.getDownStation()))
                .distinct()
                .collect(Collectors.toList());
    }

    public void deleteSection(Station station) {
        Section lastSection = getLastSection();
        validateIsExistSection(lastSection);
        validateLastSection(lastSection, station.getId());
        validateMinimumSizeOfSection();
        sections.remove(lastSection);
    }

    private void validateIsExistSection(Section lastSection) {
        if(lastSection == null) {
            throw new ValidationException("구간이 존재하지 않습니다.");
        }
    }

    private void validateLastSection(Section lastSection, Long stationId) {
        if(!lastSection.isDownStation(stationId)) {
            throw new ValidationException("마지막 구간의 하행 종점역이 아닙니다.");
        }
    }

    private void validateMinimumSizeOfSection() {
        if(sections.size() <= DELETION_MINIMUM_SIZE) {
            throw new ValidationException("구간이 1개 이하인 경우 삭제할 수 없습니다.");
        }
    }

    public void verifyNewUpStationIsDownStation(Section section) {
        Section lastSection = getLastSection();
        if(lastSection != null && !lastSection.getDownStation().equals(section.getUpStation())) {
            throw new RegistrationException("새로운 구간이 해당 노선의 하행 종점역이 아닙니다.");
        }
    }

    public void verifyStationAlreadyRegistered(Section section) {
        List<Station> stationList = createStationList();
        if(stationList.contains(section.getDownStation())) {
            throw new RegistrationException("새로운 구간의 하행역이 해당 노선에 이미 등록된 역 입니다.");
        }
    }

}
