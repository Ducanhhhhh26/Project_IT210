package repository;

import model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    boolean existsByShowtimeIdAndSeatId(Long showtimeId, Long seatId);
    List<Ticket> findByShowtimeId(Long showtimeId);
}

