package fm.douban.service;

import fm.douban.model.Subject;

import java.util.List;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/5/27 14:35
 */
public interface SubjectService {
    //添加一个主题
    Subject addSubject(Subject subject);
    //查询单个主题
    Subject get(String subjectId);
    //查询一组主题
    List<Subject> getSubjects(String type);
    //查询一组主题
    List<Subject> getSubjects(String type,String subType);
    //通过master查询subject
    List<Subject> getSubjects(Subject subjectParam);
    //删除一个主题
    boolean delete(String subjectId);
}
