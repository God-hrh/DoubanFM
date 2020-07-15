package fm.douban.spider;

import com.alibaba.fastjson.JSON;
import fm.douban.model.Singer;
import fm.douban.model.Song;
import fm.douban.model.Subject;
import fm.douban.param.SongQueryParam;
import fm.douban.service.SingerService;
import fm.douban.service.SongService;
import fm.douban.service.SubjectService;
import fm.douban.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fm.douban.util.SubjectUtil.*;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/5/30 18:52
 */
@Component
public class SubjectSpider {
    //注入主题服务
    @Autowired
    private SubjectService subjectService;
    //注入歌手服务
    @Autowired
    private SingerService singerService;
    //系统启动时自动执行爬取任务
    @Autowired
    private SongService songService;

    private static final String User_Agent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36";
    private static final Logger LOG = LoggerFactory.getLogger(SubjectSpider.class);

//    @PostConstruct
    public void init(){
        doExcute();
    }
    //开始执行爬取任务
    public void doExcute(){
        //爬取从艺术家出发
        getArtistData();
        //爬取兆赫数据
        getSubjectData();
    }
    /*-----------------爬取兆赫和艺术家--------------*/
    private void getSubjectData(){
        getScenarioData();
        getLanguageData();
        getGenreData();
    }
    private Map getChannels(){
        String url = "https://fm.douban.com/j/v2/rec_channels?specific=all";
        String host = "fm.douban.com";
        String referer = "https://fm.douban.com/";
        Map<String,String> headerMap =HttpUtil.buildHeaderData(referer,host);
        headerMap.put("User-Agent",User_Agent);
        String content = HttpUtil.getContent(url,headerMap);
        Map returnData = JSON.parseObject(content,Map.class);
        Map data = (Map) returnData.get("data");
        Map channels = (Map) data.get("channels");
        return channels;
    }
    //爬取艺术家
    private void getArtistData(){
        Subject subject = null;
        Map channels = getChannels();
        List artists = (List) channels.get("artist");
        for (Object object :artists){
            Map m = (Map) object;
            subject = new Subject();
            subject.setCover((String) m.get("cover"));
            subject.setId((m.get("id").toString()));
            //设置艺术家链接的id
            subject.setMaster(String.valueOf(m.get("artist_id")) );
            subject.setDescription(String.valueOf(m.get("intro")));
            subject.setGmtCreated(LocalDateTime.now());
            subject.setGmtModified(LocalDateTime.now());
            subject.setName((String) m.get("name"));
            subject.setSubjectType(TYPE_MHZ);
            subject.setSubjectSubType(TYPE_SUB_ARTIST);

            List relatedSingersData = (List) m.get("related_artists");
            List<String> relatedSingerids = new ArrayList<>();
            Map relatedIdMap = null;
            for (int i = 0;i<relatedSingersData.size();i++){
                Map map = (Map) relatedSingersData.get(i);
                String id = (String) map.get("id");
                relatedIdMap = new HashMap();
                relatedIdMap.put("singersId",(String) map.get("id"));
                getRelatedSingers(relatedIdMap);
                relatedSingerids.add(id);
            }
            subject.setArtistIds(relatedSingerids);
            //设置歌曲列表的id
            subject.setSongIds(getSongDataBySingers(subject.getMaster()));
            if (subjectService.get(subject.getId())==null) {
                subjectService.addSubject(subject);
            }
        }
    }
    //设置Subject属性
    private void setSubjectPro(String type){
        Subject subject = null;
        Map channels = getChannels();
        List subjects = (List) channels.get(type);
        for (Object object :subjects){
            Map m = (Map) object;
            subject = new Subject();
            subject.setCover((String) m.get("cover"));
            subject.setSubjectType(TYPE_MHZ);
            if (type.equals("language")) {
                subject.setSubjectSubType(TYPE_SUB_AGE);
            }
            if (type.equals("scenario")) {
                subject.setSubjectSubType(TYPE_SUB_MOOD);
            }
            if (type.equals("genre")) {
                subject.setSubjectSubType(TYPE_SUB_STYLE);
            }
            subject.setId((m.get("id").toString()));
            subject.setGmtCreated(LocalDateTime.now());
            subject.setGmtModified(LocalDateTime.now());
            subject.setName((String) m.get("name"));
            subject.setDescription(String.valueOf(m.get("intro")));
            getSubjectSongData((m.get("id").toString()));
            if (subjectService.get(subject.getId())==null) {
                subjectService.addSubject(subject);
            }

        }
    }
/*-----------------不同的subject设置不同的二级标题--------------------------*/
    private void getLanguageData(){
        setSubjectPro("language");
    }
    private void getScenarioData(){
        setSubjectPro("scenario");
    }
    private void getGenreData(){
        setSubjectPro("genre");
    }




    /*--------------------爬取Song-----------------*/
    private void getSubjectSongData(String subjectId){
        String url = "https://fm.douban.com/j/v2/playlist?channel="+subjectId+"&kbps=128&client=s%3Amainsite%7Cy%3A3.0&app_name=radio_website&version=100&type=n";
        String host = "fm.douban.com";
        String referer = "https://fm.douban.com/explore/channels";
        Map<String,String> headerMap =HttpUtil.buildHeaderData(referer,host);
        headerMap.put("User-Agent",User_Agent);
        String content = HttpUtil.getContent(url,headerMap);
        Map returnData = JSON.parseObject(content,Map.class);
        List songs = (List) returnData.get("song");
        foreachSongsAddsong(songs);
    }
//    点击艺术家标题，爬取歌手的歌曲
    private List<String> getSongDataBySingers(String singerId){
        //原来的
//        List<Singer> singers = singerService.getAll();
//        for (Singer singer : singers){
//            String singerId = singer.getId();
            String url = "https://fm.douban.com/j/v2/artist/"+singerId+"/";
            String host = "fm.douban.com";
            String referer = "https://fm.douban.com/artist/"+singerId;
            Map<String,String> headerMap =HttpUtil.buildHeaderData(referer,host);
            headerMap.put("User-Agent",User_Agent);
            String content = HttpUtil.getContent(url,headerMap);
            Map returnData = JSON.parseObject(content,Map.class);
            Map songList = (Map) returnData.get("songlist");
            List songs = (List) songList.get("songs");
            return foreachSongsAddsong(songs);

    }
//    遍历songs并判断数据库是否已经有该歌曲，如果有则不重复添加，否则添加
    private List<String> foreachSongsAddsong(List songs){
        List<String> songList = new ArrayList<>();
        for (Object object : songs){
            Map songMap = (Map) object;
            Song song = new Song();
            song.setId(String.valueOf(songMap.get("sid")));
            songList.add(song.getId());
            song.setName(String.valueOf(songMap.get("title")));
            song.setCover(String.valueOf(songMap.get("picture")));
            song.setUrl(String.valueOf(songMap.get("url")));
            List<Map> singerList = (List) songMap.get("singers");
            List<String> singerIds = new ArrayList<>();
            if (singerList!=null) {
                for (Map m : singerList) {
                    singerIds.add(String.valueOf(m.get("id")));
                }
            }
            song.setSingerIds(singerIds);
            //如果已经有该歌曲则不再重复添加
            if (songService.get(song.getId())==null) {
                songService.add(song);
            }
        }
        return songList;
    }
    //相似歌手的详情爬取
    private void getRelatedSingers(Map sourceData){
        String url = "https://fm.douban.com/j/v2/artist/"+sourceData.get("singersId");
        String host = "fm.douban.com";
        String referer = "https://fm.douban.com/artist/"+sourceData.get("singersId");
        Map<String,String> headerMap =HttpUtil.buildHeaderData(referer,host);
        headerMap.put("User-Agent",User_Agent);
        String content = HttpUtil.getContent(url,headerMap);
        Map returnData = JSON.parseObject(content,Map.class);
        Map related_channel = (Map) returnData.get("related_channel");
        List similar_artists = (List) related_channel.get("similar_artists");
        Singer singer = null;
        for (int i = 0;i<similar_artists.size();i++){
            Map songMap = (Map) similar_artists.get(i);
            singer = new Singer();
            singer.setName(String.valueOf(songMap.get("name")));
            singer.setAvatar(String.valueOf(songMap.get("avatar")));
            singer.setId(String.valueOf(songMap.get("id")));
            if (singerService.get(singer.getId())==null){
                singerService.addSinger(singer);
            }
        }
    }

}
