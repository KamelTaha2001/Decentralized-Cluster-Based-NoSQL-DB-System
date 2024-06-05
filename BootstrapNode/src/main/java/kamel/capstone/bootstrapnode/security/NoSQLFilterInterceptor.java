package kamel.capstone.bootstrapnode.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kamel.capstone.bootstrapnode.util.Constants;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class NoSQLFilterInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String key = request.getHeader("Authorization");
        if (key == null) return false;
        boolean success = key.equals(Constants.PRIVATE_KEY);
        if (!success)
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return success;
    }
}
