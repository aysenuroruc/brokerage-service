package controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.OrderService;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OrderService orderService;

    @PostMapping("/{orderId}/match")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> matchOrder(@PathVariable Long orderId) {
        log.info("Match order request for order: {}", orderId);

        orderService.matchOrder(orderId);

        return ResponseEntity.ok().build();
    }
}
