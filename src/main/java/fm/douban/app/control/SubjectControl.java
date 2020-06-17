package fm.douban.app.control;

import fm.douban.model.Singer;
import fm.douban.model.Song;
import fm.douban.model.Subject;
import fm.douban.param.SongQueryParam;
import fm.douban.service.SingerService;
import fm.douban.service.SongService;
import fm.douban.service.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

import static fm.douban.util.SubjectUtil.*;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/6/8 21:45
 */
@Controller
public class SubjectControl {
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SongService songService;
    @Autowired
    private SingerService singerService;
    private static final Logger LOG = LoggerFactory.getLogger(SubjectControl.class);
    @GetMapping("/artist")
    public String mhzDetail(Model model, @RequestParam(name = "subjectId") String id){
        Subject subject = subjectService.get(id);
        model.addAttribute("subject",subject);
        List<String> songsId = subject.getSongIds();
        List<Song> songList = new ArrayList<>();
        for (String sid:songsId){
            Song song = songService.get(sid);
            if (song!=null) {
                songList.add(song);
            }
        }
        model.addAttribute("songs",songList);
        List<String> singers = subject.getArtistIds();
        model.addAttribute("singer",singers);
        List<Singer> singerList = new ArrayList<>();
        for (String s: singers) {
            Singer singer1 = singerService.get(s);
            if (singer1!=null){
                singerList.add(singer1);
            }
        }
        model.addAttribute("simSingers",singerList);
        return "mhzdetail";
    }
    //歌单列表
    @GetMapping("/collection")
    public String collection(Model model){
        List<Subject> subjectList = subjectService.getSubjects(TYPE_COLLECTION,TYPE_COLLECTION_MAIN);
//        List<Subject> subjectList = subjectService.getSubjects(TYPE_COLLECTION);
        List<List> songnameList = new ArrayList<>();
        List<List> songauthorList = new ArrayList<>();
        for (Subject subject : subjectList) {
            List<String> songauthors = subject.getSongauthors();
            List<String> songnames = subject.getSongnames();
            songauthorList.add(songauthors);
            songnameList.add(songnames);
        }
        model.addAttribute("subjectList",subjectList);
        model.addAttribute("songnameList",songnameList);
        model.addAttribute("songauthorList",songauthorList);
        return "collection";
    }
    //歌单详情
    @GetMapping("/collectiondetail")
    public String collectionDetail(Model model,@RequestParam String subjectId){
        //向前台传输歌单信息
        Subject subject = subjectService.get(subjectId);
        model.addAttribute("subject",subject);
        //向前台传输歌单创建者信息
        Singer singer = singerService.getByMaster(subject.getMaster());
        model.addAttribute("singer",singer);
        //向前台传输歌单详情的一组song
        List<String> songIds = subject.getSongIds();
        List<Song> songs = new ArrayList<>();
        Song song = null;
        for (String songId : songIds){
            song = songService.get(songId);
            if (song!=null){
                songs.add(song);
            }
        }
        model.addAttribute("songs",songs);
        String master = subject.getMaster();
        Subject subjectParam = new Subject();
        subjectParam.setSubjectType(TYPE_COLLECTION);
//        subjectParam.setSubjectSubType(TYPE_COLLECTION_OTHER);
        subjectParam.setName(master);
        List<Subject> otherSubjects = subjectService.getSubjects(subjectParam);
//        LOG.info("otherSubjects------------------"+otherSubjects.toString());
        model.addAttribute("otherSubjects",otherSubjects);
        return "collectiondetail";
    }
}
