package fm.douban.app.control;

import fm.douban.model.Subject;
import fm.douban.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static fm.douban.util.SubjectUtil.TYPE_MHZ;
import static fm.douban.util.SubjectUtil.TYPE_SUB_AGE;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/5/27 15:28
 */
@RestController
@RequestMapping("/test/subject")
public class SubjectTestControl {
    @Autowired
    private SubjectService subjectService;
    @RequestMapping("/add")
    public Subject testAdd(){
        Subject subject = new Subject();
        subject.setId("0");
        subject.setGmtCreated(LocalDateTime.now());
        subject.setSubjectType(TYPE_MHZ);
        subject.setSubjectSubType(TYPE_SUB_AGE);
        return subjectService.addSubject(subject);
    }
    @RequestMapping("/get")
    public Subject testGet(){
        Subject subject = new Subject();
        subject.setId("0");
        return subjectService.get(subject.getId());
    }
    @RequestMapping("/getByType")
    public List<Subject> testGetByType(){
        Subject subject = new Subject();
        subject.setSubjectType(TYPE_MHZ);
        return subjectService.getSubjects(subject.getSubjectType());
    }
    @RequestMapping("/getBySubType")
    public List<Subject> testGetBySubType(){
        Subject subject = new Subject();
        subject.setSubjectType(TYPE_MHZ);
        subject.setSubjectSubType(TYPE_SUB_AGE);
        return  subjectService.getSubjects(subject.getSubjectType(),subject.getSubjectSubType());
    }
    @RequestMapping("/del")
    public boolean testDelete(){
        Subject subject = new Subject();
        subject.setId("0");
        return subjectService.delete(subject.getId());
    }
    //测试分享页面使用
    @GetMapping(path = "/share")
    public String share(){
        return "share";
    }
}
