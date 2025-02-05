package nextstep.subway.applicaion;

import nextstep.subway.applicaion.dto.LineRequest;
import nextstep.subway.applicaion.dto.LineResponse;
import nextstep.subway.applicaion.dto.StationResponse;
import nextstep.subway.domain.*;
import nextstep.subway.exception.DuplicatedNameException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class LineService {

    private StationRepository stationRepository;
    private LineRepository lineRepository;

    public LineService(LineRepository lineRepository, StationRepository stationRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
    }

    public LineResponse saveLine(LineRequest request) {
        if(isDuplicatedNameOfLine(request.getName())) {
            throw new DuplicatedNameException();
        }

        Line line = new Line(request.getName(), request.getColor());
        saveSection(line, request);
        lineRepository.save(line);
        return createLineResponse(line);
    }

    @Transactional(readOnly = true)
    public List<LineResponse> findAllLines() {
        List<Line> lines = lineRepository.findAll();
        return lines.stream()
                .map(this::createLineResponse)
                .collect(Collectors.toList());
    }

    private LineResponse createLineResponse(Line line) {
        return new LineResponse(
                line.getId(),
                line.getName(),
                line.getColor(),
                createStationResponseList(line.getStationList()),
                line.getCreatedDate(),
                line.getModifiedDate()
        );
    }

    private List<StationResponse> createStationResponseList(List<Station> stationList) {
        return stationList.stream()
                .map(station -> new StationResponse(station.getId(), station.getName()
                        , station.getCreatedDate(), station.getModifiedDate()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LineResponse findLine(Long id) {
        Line line = lineRepository.findById(id).orElseThrow(NoSuchElementException::new);
        return createLineResponse(line);
    }

    public LineResponse updateLine(Long id, LineRequest request) {
        Line line = lineRepository.findById(id).orElseThrow(NoSuchElementException::new);
        line.updateLine(request.getName(), request.getColor());
        return createLineResponse(line);
    }

    public void deleteLineById(Long id) {
        lineRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean isDuplicatedNameOfLine(String name) {
        Optional<Line> line = lineRepository.findByName(name);
        return line.isPresent();
    }

    public LineResponse addSection(Long id, LineRequest request) {
        Line line = lineRepository.findById(id).orElseThrow(NoSuchElementException::new);
        saveSection(line, request);
        return createLineResponse(line);
    }

    private void saveSection(Line line, LineRequest request) {
        Section section = new Section(line, findStation(request.getUpStationId())
                                        , findStation(request.getDownStationId()), request.getDistance());
        line.registSection(section);
    }

    @Transactional(readOnly = true)
    public Station findStation(Long stationId) {
        return stationRepository.findById(stationId).orElseThrow(NoSuchElementException::new);
    }

    public void deleteSection(Long id, Long stationId) {
        Line line = lineRepository.findById(id).orElseThrow(NoSuchElementException::new);
        Station station = stationRepository.findById(stationId).orElseThrow(NoSuchElementException::new);
        line.deleteSection(station);
    }

}
