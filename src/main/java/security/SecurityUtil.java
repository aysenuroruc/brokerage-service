package security;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class SecurityUtil {

    public static Long currentCustomerIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            Object c = jwt.getClaim("customerId");
            if (c instanceof Number n) return n.longValue();
            try {
                return Long.valueOf(String.valueOf(c));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            Object roles = jwt.getClaim("roles");
            if (roles instanceof java.util.Collection<?> coll) {
                return coll.stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(String.valueOf(r)));
            }
        }
        return false;
    }
}
