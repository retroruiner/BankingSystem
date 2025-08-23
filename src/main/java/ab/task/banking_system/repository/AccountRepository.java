package ab.task.banking_system.repository;

import ab.task.banking_system.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("update Account a set a.balance = a.balance + :amount where a.id = :id")
    int deposit(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Modifying(clearAutomatically = true)
    @Query("update Account a set a.balance = a.balance - :amount " + "where a.id = :id and a.balance >= :amount")
    int withdrawIfEnough(@Param("id") Long id, @Param("amount") BigDecimal amount);
}
