package pl.skirent.service;

import pl.skirent.model.Rental;
import pl.skirent.model.Ski;
import pl.skirent.repository.RentalRepository;
import pl.skirent.repository.SkiRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReportService {
    private final SkiRepository skiRepository;
    private final RentalRepository rentalRepository;

    public ReportService(SkiRepository skiRepository, RentalRepository rentalRepository) {
        this.skiRepository = skiRepository;
        this.rentalRepository = rentalRepository;
    }

    public List<Ski> getAvailableSkis() {
        LocalDateTime now = LocalDateTime.now();
        List<Integer> rentedSkiIds = new ArrayList<>();
        for (Rental r : rentalRepository.findAll()) {
            if ("ACTIVE".equals(r.getStatus())) {
                if (r.getFrom() != null && r.getTo() != null
                        && r.getFrom().isBefore(now) && r.getTo().isAfter(now)) {
                    if (r.getSkiIds() != null) rentedSkiIds.addAll(r.getSkiIds());
                }
            }
        }
        return skiRepository.findAll().stream()
                .filter(s -> !rentedSkiIds.contains(s.getId()))
                .collect(Collectors.toList());
    }

    public List<Ski> getCurrentlyRentedSkis() {
        LocalDateTime now = LocalDateTime.now();
        List<Integer> rentedSkiIds = new ArrayList<>();
        for (Rental r : rentalRepository.findAll()) {
            if ("ACTIVE".equals(r.getStatus())) {
                if (r.getFrom() != null && r.getTo() != null
                        && r.getFrom().isBefore(now) && r.getTo().isAfter(now)) {
                    if (r.getSkiIds() != null) rentedSkiIds.addAll(r.getSkiIds());
                }
            }
        }
        return skiRepository.findAll().stream()
                .filter(s -> rentedSkiIds.contains(s.getId()))
                .collect(Collectors.toList());
    }

    public List<Rental> getOverdueRentals() {
        return rentalRepository.findAll().stream()
                .filter(r -> "LATE".equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    // Keep old method names for compatibility
    public List<Rental> getCurrentlyRented() { return new ArrayList<>(); }
    public List<Rental> getOverdue() { return getOverdueRentals(); }
}
