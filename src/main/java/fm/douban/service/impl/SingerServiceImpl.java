package fm.douban.service.impl;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import fm.douban.model.Singer;
import fm.douban.service.SingerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/5/25 15:49
 */
@Service
public class SingerServiceImpl implements SingerService {
    private static final Logger LOG = LoggerFactory.getLogger(SingerServiceImpl.class);
    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public Singer addSinger(Singer singer) {
        if (singer==null){
            LOG.error("添加歌手参数错误");
            return null;
        }
        return mongoTemplate.insert(singer);
    }

    @Override
    public Singer get(String singerId) {
        if (!StringUtils.hasText(singerId)){
            LOG.error("根据ID获取歌手参数错误");
            return null;
        }
        return mongoTemplate.findById(singerId,Singer.class);
    }

    @Override
    public Singer getByMaster(String master) {
        if (!StringUtils.hasText(master)){
            LOG.error("根据master获取歌手参数错误");
            return null;
        }
        Query query = new Query(Criteria.where("name").is(master));
        List<Singer> singerList = mongoTemplate.find(query,Singer.class);
        if (singerList.isEmpty()){
            LOG.error("根据master查询歌手结果集错误");
        }
        return singerList.get(0);
    }

    @Override
    public List<Singer> getAll() {
        return mongoTemplate.findAll(Singer.class);
    }

    @Override
    public boolean modify(Singer singer) {
        if (singer==null|| !StringUtils.hasText(singer.getId())){
            LOG.error("修改歌手参数错误");
            return false;
        }
        Query query = new Query(Criteria.where("id").is(singer.getId()));
        Update update = new Update();
        if (StringUtils.hasText(singer.getAvatar())){
            update.set("avatar",singer.getAvatar());
        }
        if (StringUtils.hasText(singer.getHomepage())){
            update.set("homepage",singer.getHomepage());
        }
        if (StringUtils.hasText(singer.getName())){
            update.set("name",singer.getName());
        }
        UpdateResult result = mongoTemplate.updateFirst(query,update,Singer.class);
        return result!=null&&result.getModifiedCount()>0;
    }

    @Override
    public boolean delete(String singerId) {
        if (!StringUtils.hasText(singerId)){
            LOG.error("删除歌手参数错误");
            return false;
        }
        Singer singer = new Singer();
        singer.setId(singerId);
        DeleteResult result = mongoTemplate.remove(singer);
        return result!=null&&result.getDeletedCount()>0;
    }
}
