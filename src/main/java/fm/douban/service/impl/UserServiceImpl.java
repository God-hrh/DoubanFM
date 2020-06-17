package fm.douban.service.impl;

import fm.douban.model.User;
import fm.douban.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/6/10 22:43
 */
@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public User add(User user) {
        if (user==null){
            LOG.error("添加用户传参失败！");
        }
        return mongoTemplate.insert(user);
    }

    @Override
    public User get(String userName) {
        Query query = new Query(Criteria.where("name").is(userName));
        List<User> userList =  mongoTemplate.find(query,User.class);
        LOG.info(userList.toString());
        if (userList==null||userList.isEmpty()){
           LOG.info("不存在该用户名的用户");
           return null;
        }
       return userList.get(0);
    }
}
