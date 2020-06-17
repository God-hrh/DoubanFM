package fm.douban.service;


import fm.douban.model.User;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/6/10 20:29
 */
public interface UserService {
    User add(User user);
    User get(String userName);
}
