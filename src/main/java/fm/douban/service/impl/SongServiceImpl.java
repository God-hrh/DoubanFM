package fm.douban.service.impl;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import fm.douban.model.Song;
import fm.douban.param.SongQueryParam;
import fm.douban.service.SongService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/5/26 13:41
 */
@Service
public class SongServiceImpl implements SongService {
    private static final Logger LOG = LoggerFactory.getLogger(SongServiceImpl.class);
    @Autowired
    private SongService songService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public Song add(Song song) {
        if (song==null){
            LOG.error("添加方法传参错误");
        }
        return mongoTemplate.insert(song);
    }

    @Override
    public Song get(String songId) {
        if (!StringUtils.hasText(songId)){
            LOG.error("根据ID获取Song方法传参错误");
        }
        return mongoTemplate.findById(songId,Song.class);
    }

    @Override
    public Page<Song> list(SongQueryParam songQueryParam) {
        //创建查询条件
        Criteria criteria = new Criteria();
        List<Criteria> criteriaList = new ArrayList<>();
        if (StringUtils.hasText(songQueryParam.getCover())){
            criteriaList.add(Criteria.where("cover").is(songQueryParam.getCover()));
        }
        if (StringUtils.hasText(songQueryParam.getLyrics())){
            criteriaList.add(Criteria.where("lyrics").is(songQueryParam.getLyrics()));
        }
        if (StringUtils.hasText(songQueryParam.getName())){
            criteriaList.add(Criteria.where("name").is(songQueryParam.getName()));
        }
        if (StringUtils.hasText(songQueryParam.getUrl())){
            criteriaList.add(Criteria.where("url").is(songQueryParam.getUrl()));
        }
        if (StringUtils.hasText(songQueryParam.getId())){
            criteriaList.add(Criteria.where("id").is(songQueryParam.getId()));
        }
        Query query = null;
        if (!criteriaList.isEmpty()){
            //Criteria.where("id").is(songQueryParam.getId())
            //query = new Query(Criteria.where("id").is(songQueryParam.getId()));
            criteria.andOperator(criteriaList.toArray(new Criteria[]{}) );
        }
        query = new Query(criteria);

        //查询结果
        List<Song> SongQueryParams = mongoTemplate.find(query,Song.class);
        //获取记录总数
        Long count = mongoTemplate.count(query,Song.class);
        // 构建分页对象。注意此对象页码号是从 0 开始计数的。
        Pageable pageable = PageRequest.of(songQueryParam.getPageNum()-1,songQueryParam.getPageSize());
        query.with(pageable);
        //构建分页器
        Page<Song> songPage = PageableExecutionUtils.getPage(SongQueryParams, pageable, new LongSupplier() {
            @Override
            public long getAsLong() {
                return count;
            }
        });
        return songPage;
    }

    @Override
    public boolean modify(Song song) {
        if (song==null){
            LOG.error("修改方法传参错误");
        }
        Query query = new Query(Criteria.where("id").is(song.getId()));
        Update update = new Update();
        if (song.getCover()!=null){
            update.set("cover",song.getCover());
        }
        if (song.getLyrics()!=null){
            update.set("lyrics",song.getLyrics());
        }
        if (song.getName()!=null){
            update.set("name",song.getName());
        }
        if (song.getUrl()!=null){
            update.set("url",song.getUrl());
        }
        UpdateResult result = mongoTemplate.updateFirst(query,update,Song.class);
        return  result!=null&&result.getModifiedCount()>0;
    }

    @Override
    public boolean delete(String songId) {
        if (!StringUtils.hasText(songId)){
            LOG.error("删除方法传参错误");
        }
        Song song = new Song();
        song.setId(songId);
        DeleteResult result = mongoTemplate.remove(song);
        return result!=null&&result.getDeletedCount()>0;
    }
}
