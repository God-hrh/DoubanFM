package fm.douban.app.control;

import fm.douban.model.*;
import fm.douban.param.SongQueryParam;
import fm.douban.service.FavoriteService;
import fm.douban.service.SingerService;
import fm.douban.service.SongService;
import fm.douban.service.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fm.douban.util.SubjectUtil.*;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/5/24 21:52
 */
@Controller
public class MainControl {
    private static final Logger LOG = LoggerFactory.getLogger(MainControl.class);
    @Autowired
    private SongService songService;
    @Autowired
    private SingerService singerService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    public FavoriteService favoriteService;
    //首页
    @RequestMapping("/index")
    public String index(Model model){
        SongQueryParam songQueryParam = new SongQueryParam();
        songQueryParam.setPageNum(1);
        songQueryParam.setPageSize(1);
        songQueryParam.setId("16");
        Song song =  songService.list(songQueryParam).getContent().get(0);
        model.addAttribute("song",song);
        List<Singer> singerList = new ArrayList<>();
        Singer singer = null;
        if ( song.getSingerIds()!=null) {
            for (String singersId : song.getSingerIds()) {
                singer = singerService.get(singersId);
                if (singer!=null){
                    singerList.add(singer);
                }
            }
        }
        model.addAttribute("singers",singerList);
        List artistList = subjectService.getSubjects(TYPE_MHZ,TYPE_SUB_ARTIST);
        List moodList = subjectService.getSubjects(TYPE_MHZ,TYPE_SUB_MOOD);
        List ageList = subjectService.getSubjects(TYPE_MHZ,TYPE_SUB_AGE);
        List styleList = subjectService.getSubjects(TYPE_MHZ,TYPE_SUB_STYLE);
        MhzViewModel mhzViewModelMood = new MhzViewModel();
        mhzViewModelMood.setSubjects(moodList);
        mhzViewModelMood.setTitle("心情 / 场景");
        MhzViewModel mhzViewModelAge = new MhzViewModel();
        mhzViewModelAge.setSubjects(ageList);
        mhzViewModelAge.setTitle("语言 / 年代");
        MhzViewModel mhzViewModelStyle = new MhzViewModel();
        mhzViewModelStyle.setSubjects(styleList);
        mhzViewModelStyle.setTitle("风格 / 流派");
        List<MhzViewModel> mhzViewModels = new ArrayList<>();
        mhzViewModels.add(mhzViewModelAge);
        mhzViewModels.add(mhzViewModelStyle);
        mhzViewModels.add(mhzViewModelMood);
        model.addAttribute("artistDatas",artistList);
        model.addAttribute("mhzViewModels",mhzViewModels);
        return "index";
    }
    //搜索页
    @GetMapping("/search")
    public String search(Model model){
        Map resultMap = searchContent("Stargazers");
        model.addAttribute("songs",resultMap.get("songs"));
        return "search";
    }
    //搜索结果
    @GetMapping("/searchContent")
    @ResponseBody
    public Map searchContent(@RequestParam("keyword") String keyword){
        Map resultMap = new HashMap();
        SongQueryParam songQueryParam = new SongQueryParam();
        songQueryParam.setName(keyword);
        List<Song> songList = songService.list(songQueryParam).getContent();
        resultMap.put("songs",songList);
        return resultMap;
    }
    //我的页面
    @GetMapping(path = "/my")
    public String myPage(Model model, HttpServletRequest request, HttpServletResponse response){
        //开启session
        HttpSession session = request.getSession();
        //获取用户数据
        User user = (User) session.getAttribute("user");
        if (user!=null) {
            Favorite favParam = new Favorite();
            favParam.setUserId(user.getId());
            List<Favorite> favoriteList = favoriteService.list(favParam);
            //获取列表的song
            Song song = null;
            List<Song> songs = new ArrayList<>();
            if (!favoriteList.isEmpty()) {
                for (Favorite favorite : favoriteList) {
                    String itemId = favorite.getItemId();
                    song = songService.get(itemId);
                    songs.add(song);
                }
            }
            model.addAttribute("favorites", favoriteList);
            model.addAttribute("songs", songs);
            return "my";
        }else {
            return "login";
        }
    }
    @GetMapping(path = "/fav")
    @ResponseBody
    public Map doFav(@RequestParam String itemType,@RequestParam String itemId, HttpServletRequest request, HttpServletResponse response){
        Map map = new HashMap();
        //开启session
        HttpSession session = request.getSession();
        //获取用户数据
        User user = (User) session.getAttribute("user");
        if (user!=null) {
            Favorite favParam = new Favorite();
            favParam.setUserId(user.getId());
            favParam.setItemType(itemType);
            favParam.setItemId(itemId);
            List<Favorite> favoriteList = favoriteService.list(favParam);
            if (favoriteList.isEmpty()) {
                favParam.setGmtCreated(LocalDateTime.now());
                favParam.setGmtModified(LocalDateTime.now());
                favoriteService.add(favParam);
            } else {
                favoriteService.delete(favParam);
            }
            map.put("message", "successful");
        }else {
            map.put("error","没有登陆");
        }
        return map;
    }
    @GetMapping(path = "/share")
    public String share(){
        return "share";
    }
}
