package repository;

import model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByStatusOrderByCreateDateAsc(String status); // pending ordered by createDate
    List<Order> findByAssetNameAndStatusOrderByCreateDateAsc(String assetName, String status);
}
