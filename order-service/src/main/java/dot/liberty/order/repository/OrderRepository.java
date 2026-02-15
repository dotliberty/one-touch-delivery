package dot.liberty.order.repository;

import dot.liberty.order.entity.Order;
import dot.liberty.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Order> findByCourierIdOrderByCreatedAtDesc(Long courierId);
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status);

}
