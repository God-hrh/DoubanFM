package fm.douban.app.control;

import fm.douban.model.Singer;
import fm.douban.service.SingerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/5/25 16:25
 */
@RestController
@RequestMapping("/test/singer")
public class SingerTestControl {
    private static final Logger LOG = LoggerFactory.getLogger(SingerTestControl.class);
    @Autowired
    private SingerService singerService;
    @PostConstruct
    public void init(){
//        LOG.info("SingerTestControl类加载成功");
//        if (singerService!=null){
//            LOG.info("singerService初始化成功");
//        }else {
//            LOG.info("singerService初始化失败");
//        }
    }
    @GetMapping("/add")
    public Singer testAddSinger(){
        Singer singer = new Singer();
        singer.setGmtCreated(LocalDateTime.now());
        singer.setId("0");
        return  singerService.addSinger(singer);
    }
    @GetMapping("/getAll")
    public List<Singer> testGetAll(){
        return  singerService.getAll();
    }
    @GetMapping("/getOne")
    public Singer testGetSinger(){
        //@RequestParam ("id") String id
        return  singerService.get("0");
    }
    @GetMapping("/modify")
    public boolean testModifySinger(){
        //@RequestParam(name = "id",required = true) String id,@RequestParam(name = "avatar",required = false,value = "") String avatar,@RequestParam(name = "homepage",required = false,value = "") String homepage,@RequestParam(name = "name",required = false,value = "") String name
        Singer singer = new Singer();
        //设定当前时间
        singer.setGmtModified(LocalDateTime.now());
        singer.setId("1");
        singer.setAvatar("2");
        singer.setHomepage("3");
        singer.setName("5");
        return singerService.modify(singer);

    }
    @GetMapping("/del")
    public boolean testDelSinger(){
        //@RequestParam String id
        Singer singer = new Singer();
        singer.setId("0");
        return singerService.delete(singer.getId());
    }
}
