package fm.douban.app.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/6/19 0:23
 */
public class UserInterceptor implements HandlerInterceptor {
    // Controller方法执行之前
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只有返回true才会继续向下执行，返回false取消当前请求
        HttpSession session = request.getSession();
        if (session.getAttribute("user") != null) {
            return true;
        }

        // 跳转登录
        String url = "/login";
        response.sendRedirect(url);
        return false;
    }
    //Controller方法执行之后
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
    }
    // 整个请求完成后（包括Thymeleaf渲染完毕）
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
