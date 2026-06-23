package pl.skirent.service;

import pl.skirent.model.Rental;
import pl.skirent.repository.RentalRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RentalService {
    private final RentalRepository rentalRepository;

    public RentalService(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    public Rental createRental(int customerId, List<Integer> skiIds, LocalDateTime from, LocalDateTime to) {
        for (int skiId : skiIds) {
            if (!isSkiAvailable(skiId, from, to)) {
                throw new IllegalStateException("Ski not available: " + skiId);
            }
        }
        Rental rental = new Rental();
        rental.setCustomerId(customerId);
        rental.setSkiIds(new ArrayList<>(skiIds));
        rental.setFrom(from);
        rental.setTo(to);
        rental.setStatus("ACTIVE");
        rentalRepository.add(rental);
        return rental;
    }

    public void returnRental(int rentalId, String remarks) {
        Rental rental = rentalRepository.findById(rentalId);
        if (rental != null) {
            rental.setStatus("RETURNED");
            rental.setRemarks(remarks);
            rentalRepository.update(rental);
        }
    }

    public void checkOverdue() {
        LocalDateTime now = LocalDateTime.now();
        for (Rental rental : rentalRepository.findAll()) {
            if ("ACTIVE".equals(rental.getStatus()) && rental.getTo() != null && rental.getTo().isBefore(now)) {
                rental.setStatus("LATE");
                rentalRepository.update(rental);
            }
        }
    }

    private boolean isSkiAvailable(int skiId, LocalDateTime from, LocalDateTime to) {
        for (Rental r : rentalRepository.findAll()) {
            if ("ACTIVE".equals(r.getStatus()) || "LATE".equals(r.getStatus())) {
                if (r.getSkiIds() != null && r.getSkiIds().contains(skiId)) {
                    if (r.getFrom() != null && r.getTo() != null
                            && r.getFrom().isBefore(to) && r.getTo().isAfter(from)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
