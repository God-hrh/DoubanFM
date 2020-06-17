package fm.douban.service.impl;

import com.mongodb.client.result.DeleteResult;
import fm.douban.model.Favorite;
import fm.douban.service.FavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.CodecRegistryProvider;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/6/16 21:26
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {
    private static final Logger LOG = LoggerFactory.getLogger(FavoriteServiceImpl.class);
    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public Favorite add(Favorite fav) {
        if (fav==(null)){
            LOG.error("添加喜欢传参错误");
        }
        return mongoTemplate.insert(fav);
    }

    @Override
    public List<Favorite> list(Favorite favParam) {
        if (favParam==(null)){
            LOG.error("计算喜欢传参错误");
        }
        String itemId = favParam.getItemId();
        String itemType = favParam.getItemType();
        String type = favParam.getType();
        String userId = favParam.getUserId();
        //总条件
        Criteria criteria = new Criteria();
        //条件集合
        List<Criteria> criteriaList = new ArrayList<>();
        if (String.valueOf(itemId)!="null") {
             criteriaList.add(Criteria.where("itemId").is(itemId));
        }
        if (String.valueOf(type)!="null") {
            criteriaList.add(Criteria.where("type").is(type));
        }
        if (String.valueOf(userId)!="null") {
            criteriaList.add(Criteria.where("userId").is(userId));
        }
        if (String.valueOf(itemType)!="null") {
            criteriaList.add(Criteria.where("itemType").is(itemType));
        }
        criteria.andOperator(criteriaList.toArray(new Criteria[]{}));
        Query query = new Query(criteria);
        return mongoTemplate.find(query,Favorite.class);
    }

    @Override
    public boolean delete(Favorite favParam) {
        if (favParam==(null)){
            LOG.error("删除喜欢传参错误");
        }
        String itemId = favParam.getItemId();
        Query query = new Query(Criteria.where("itemId").is(itemId));
        DeleteResult deleteResult =  mongoTemplate.remove(query,Favorite.class);
        if (deleteResult.getDeletedCount()==1){
            return true;
        }else {
            return false;
        }
    }

}
