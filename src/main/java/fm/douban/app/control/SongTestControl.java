package fm.douban.app.control;

import fm.douban.model.Song;
import fm.douban.param.SongQueryParam;
import fm.douban.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/5/26 16:30
 */
@RestController
@RequestMapping("/test/song")
public class SongTestControl {
    @Autowired
    private SongService songService;
    @GetMapping("/add")
    public Song testAdd(){
        Song song = new Song();
        song.setId("0");
        song.setGmtCreated(LocalDateTime.now());
        return songService.add(song);
    }
    @GetMapping("/get")
    public Song testGet(){
        Song song = new Song();
        song.setId("0");
        return songService.get(song.getId());
    }
    @GetMapping("/list")
    public Page<Song> testList(){
        SongQueryParam songQueryParam = new SongQueryParam();
        songQueryParam.setId("0");
        return songService.list(songQueryParam);
    }
    @GetMapping("/modify")
    public boolean testModify(){
        Song song = new Song();
        song.setId("0");
        song.setGmtModified(LocalDateTime.now());
        song.setLyrics("1");
        return songService.modify(song);
    }
    @GetMapping("/del")
    public boolean testDelete(){
        Song song = new Song();
        song.setId("0");
        return songService.delete(song.getId());
    }





}
