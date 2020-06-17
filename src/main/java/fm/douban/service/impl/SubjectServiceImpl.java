package fm.douban.service.impl;

import com.mongodb.client.result.DeleteResult;
import fm.douban.model.Subject;
import fm.douban.service.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/5/27 14:40
 */
@Service
public class SubjectServiceImpl implements SubjectService {
    public static final Logger LOG = LoggerFactory.getLogger(SubjectServiceImpl.class);
    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public Subject addSubject(Subject subject) {
        if (subject==null){
            LOG.error("添加主题参数错误");
            return null;
        }
        return mongoTemplate.insert(subject);
    }

    @Override
    public Subject get(String subjectId) {
        if (!StringUtils.hasText(subjectId)){
            LOG.error("查询单个主题参数错误");
            return null;
        }
        return mongoTemplate.findById(subjectId,Subject.class);
    }

    @Override
    public List<Subject> getSubjects(String type) {
        if (!StringUtils.hasText(type)){
            LOG.error("查询一组主题参数错误");
            return null;
        }
        Query query = new Query(Criteria.where("subjectType").is(type));
        return mongoTemplate.find(query,Subject.class);
    }

    @Override
    public List<Subject> getSubjects(String type, String subType) {
        if ((!StringUtils.hasText(type))&&(!StringUtils.hasText(subType))){
            LOG.error("二级类型查询一组主题参数错误");
            return null;
        }
        Subject subjectParam = new Subject();
        subjectParam.setSubjectType(type);
        subjectParam.setSubjectSubType(subType);
        return getSubjects(subjectParam);
    }

    @Override
    public List<Subject> getSubjects(Subject subjectParam) {
        // 作为服务，要对入参进行判断，不能假设被调用时，入参一定正确
        if (subjectParam == null) {
            LOG.error("input subjectParam is not correct.");
            return null;
        }
        String type = subjectParam.getSubjectType();
        String subType = subjectParam.getSubjectSubType();
        String master = subjectParam.getMaster();
        // 查询 Subject
        Criteria criteria = new Criteria();
        List<Criteria> criteriaList = new ArrayList<>();
        if (!type.isEmpty()&&type.trim()!=""){
            criteriaList.add(Criteria.where("subjectType").is(type));
        }
        if (String.valueOf(subType)!="null"){
            criteriaList.add(Criteria.where("subjectSubType").is(subType));
        }
        if (String.valueOf(master)!="null"){
            criteriaList.add(Criteria.where("name").is(master));
        }
        criteria.andOperator(criteriaList.toArray(new Criteria[]{}));
        Query query = new Query(criteria);
        return mongoTemplate.find(query,Subject.class);
    }
    @Override
    public boolean delete(String subjectId) {
        if (!StringUtils.hasText(subjectId)){
            LOG.error("删除主题参数错误");
            return false;
        }
        Subject subject = new Subject();
        subject.setId(subjectId);
        DeleteResult result = mongoTemplate.remove(subject);
        return result!=null&&result.getDeletedCount()>0;
    }
}
