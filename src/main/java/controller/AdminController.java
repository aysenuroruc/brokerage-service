package controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.OrderService;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OrderService orderService;
//
//    @PostMapping("/match-orders")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<String> matchOrders() {
//        orderService.matchPendingOrders();
//        return ResponseEntity.ok("All pending orders processed/matched successfully");
//    }
}
