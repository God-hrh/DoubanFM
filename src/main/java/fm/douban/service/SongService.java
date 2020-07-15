package fm.douban.service;

import fm.douban.model.Song;
import fm.douban.param.SongQueryParam;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/5/26 13:38
 */
public interface SongService {
    Song add(Song song);
    Song get(String songId);
    Page<Song> list(SongQueryParam songQueryParam);
    boolean modify(Song song);
    boolean delete(String songId);
}
