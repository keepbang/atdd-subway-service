package nextstep.subway.line.domain;

import nextstep.subway.exception.SectionDistanceOverException;
import nextstep.subway.station.domain.Station;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "line_id")
    private Line line;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "up_station_id")
    private Station upStation;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "down_station_id")
    private Station downStation;

    private int distance;

    public Section() {
    }

    public Section(Line line, Station upStation, Station downStation, int distance) {
        this.line = line;
        this.upStation = upStation;
        this.downStation = downStation;
        this.distance = distance;
    }

    public Long getId() {
        return id;
    }

    public Line getLine() {
        return line;
    }

    public Station getUpStation() {
        return upStation;
    }

    public Station getDownStation() {
        return downStation;
    }

    public int getDistance() {
        return distance;
    }

    public int getSurcharge() {
        return line.getSurcharge();
    }

    public boolean isIncludeSection(Section addSection) {
        return isEqualsUpStation(addSection.upStation)
                || isEqualsDownStation(addSection.downStation);
    }

    public void updateStationByAddSection(Section addSection) {
        if (this.distance <= addSection.distance) {
            throw new SectionDistanceOverException();
        }

        if (isEqualsDownStation(addSection.downStation)) {
            downStation = addSection.upStation;
        }

        if (isEqualsUpStation(addSection.upStation)) {
            upStation = addSection.downStation;
        }

        this.distance -= addSection.distance;

    }

    public boolean isEqualsUpStation(Station station) {
        return upStation.equals(station);
    }

    public boolean isEqualsDownStation(Station station) {
        return downStation.equals(station);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Section)) return false;
        Section section = (Section) o;
        return distance == section.distance
                && Objects.equals(id, section.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, line, upStation, downStation, distance);
    }
}
