package fm.douban.app.config;

import fm.douban.app.interceptor.UserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/6/19 0:27
 */
@Configuration
public class SpringHttpSessionconfig implements WebMvcConfigurer {
    //重写addInterceptors()方法,添加拦截路径
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInterceptor()).addPathPatterns("/my")
                .addPathPatterns("/fav");
    }
}
