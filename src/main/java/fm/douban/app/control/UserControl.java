package fm.douban.app.control;

import fm.douban.model.User;
import fm.douban.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/6/10 20:00
 */
@Controller
public class UserControl {
    @Autowired
    private UserService userService;
    //注册页
    @GetMapping("/sign")
    public String signPage(Model model){
        return "sign";
    }
    //提交注册
    @PostMapping("/register")
    @ResponseBody
    public Map registerAction(@RequestParam String name, @RequestParam String password, @RequestParam String mobile,
                              HttpServletRequest request, HttpServletResponse response){
        Map map = new HashMap();
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        user.setMobile(mobile);
        User user1 = userService.get(name);
        if (user1==null) {
            userService.add(user);
            map.put("result",true);
            map.put("message","register successful");
            HttpSession session = request.getSession();
            session.setAttribute("registUserInfo",user);
            return map;
        }else {
            map.put("result",false);
            map.put("message","login name already exist");
            return map;
        }
    }
    //登陆页
    @GetMapping("/login")
    public String loginPage(Model model){
        return "login";
    }
    //登陆操作
    @PostMapping("/authenticate")
//    @ResponseBody
    public String login(@RequestParam String name,@RequestParam String password,
                     HttpServletRequest request,HttpServletResponse response){
        Map map = new HashMap();
        User user = userService.get(name);
        if (user==null) {
            map.put("message","用户不存在");
        }else {
            if (user.getName().equals(name)&&user.getPassword().equals(password)){
                map.put("message","用户登陆成功");
                HttpSession session = request.getSession();
                session.setAttribute("user",user);
            }else {
                map.put("message","密码输入错误");
            }
        }
        return "redirect:index";
    }
}
