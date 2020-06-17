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
        //歌单爬虫
        getCollectionsData();
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
            //如果已经有该歌手则不再重复添加
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
    //歌单整体爬虫
    private void getCollectionsData(){
        String url = "https://fm.douban.com/j/v2/songlist/explore?type=hot&genre=0&limit=20&sample_cnt=5";
        String host = "fm.douban.com";
        String referer = "https://fm.douban.com/explore/songlists";
        Map<String,String> headerMap =HttpUtil.buildHeaderData(referer,host);
        headerMap.put("User-Agent",User_Agent);
        String content = HttpUtil.getContent(url,headerMap);
        List returnData = JSON.parseObject(content,List.class);
        //创建歌单对象
        Subject subject = null;
        for (int i = 0;i<returnData.size();i++){
            Map returnDataMap = (Map) returnData.get(i);
            //new一个歌单对象
            subject= new Subject();
            //给歌单对象设置属性
            subject.setId(String.valueOf(returnDataMap.get("id")));
            //歌单详情里的歌曲id
            List <String> songIds = getconnectondetail(subject.getId());
            subject.setSongIds(songIds);
//            LOG.info("从subject对象获取subject.getSongIds()---------------------------"+subject.getSongIds());
            //设置封面
            subject.setCover(String.valueOf(returnDataMap.get("cover")));
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            subject.setGmtCreated(LocalDateTime.parse(String.valueOf(returnDataMap.get("created_time")),df));
            subject.setGmtModified(LocalDateTime.parse(String.valueOf(returnDataMap.get("updated_time")),df));
            subject.setSubjectType(TYPE_COLLECTION);
            subject.setSubjectSubType(TYPE_COLLECTION_MAIN);
            subject.setDescription((String) returnDataMap.get("intro"));
            subject.setName(String.valueOf(returnDataMap.get("title")));
            Map creator = (Map) returnDataMap.get("creator");
            //歌单创建者爬虫  把歌单创建者当成singer
            Singer singer = new Singer();
            singer.setId(String.valueOf(creator.get("id")));
            singer.setHomepage(String.valueOf(creator.get("url")));
            singer.setName(String.valueOf(creator.get("name")));
            //设置歌单的创建者名称属性
            subject.setMaster(String.valueOf(creator.get("name")));
            //歌单创建者的其他歌单爬虫
            List<String> othercollectionIds = othercollection(singer.getId(),subject.getId());
            //设置创建者其他歌单的id
            subject.setCollectionIds(othercollectionIds);
            //主页上面歌单显示的歌曲
            List sample_songs = (List) returnDataMap.get("sample_songs");
            List<String> songnames = new ArrayList<>();
            List<String> songauthors = new ArrayList<>();
            for (Object object : sample_songs ){
                Map map = (Map) object;
                String name = String.valueOf(map.get("title"));
                String artist = String.valueOf(map.get("artist"));
                songnames.add(name);
                songauthors.add(artist);
            }
            //设置歌单主页显示的歌曲名称属性
            subject.setSongnames(songnames);
            //设置歌单主页显示的歌曲作者属性
            subject.setSongauthors(songauthors);
            if (subjectService.get(subject.getId())==null){
                subjectService.addSubject(subject);
            }
            if (singerService.get(singer.getId())==null){
                singerService.addSinger(singer);
            }else {
                singerService.modify(singer);
            }
        }
    }
    //歌单详情爬虫
    public List<String> getconnectondetail(String sbujectId){
        List<String> songIds = new ArrayList<>();
        String url = "https://fm.douban.com/j/v2/songlist/"+sbujectId+"/?kbps=192";
        String host = "fm.douban.com";
        String referer = "https://fm.douban.com/songlist/"+sbujectId;
        Map<String,String> headerMap =HttpUtil.buildHeaderData(referer,host);
        headerMap.put("User-Agent",User_Agent);
        String content = HttpUtil.getContent(url,headerMap);
        Map returnData = JSON.parseObject(content,Map.class);
        List songs = (List) returnData.get("songs");
//        LOG.info("从页面爬取的songs------------------"+songs.toString());
        Song song = null;
        //该歌曲作者的id
        List<String> songauthors = new ArrayList<>();
        Singer singer = null;
        for (Object object : songs){
            Map map = (Map) object;
            song = new Song();
            song.setName((String) map.get("title"));
            song.setPublicYear((String) map.get("public_time"));
            song.setAlbumtitle((String) map.get("albumtitle"));
            song.setId(String.valueOf(map.get("sid")));
            song.setUrl(String.valueOf(map.get("url")));
            song.setCover(String.valueOf(map.get("picture")));
            //设置song的作者
            List singers = (List) map.get("singers");

            for (Object object1:singers){
                Map map1 = (Map) object1;
                singer = new Singer();
                singer.setName(String.valueOf(map1.get("name")));
                singer.setId(String.valueOf(map1.get("id")));
                singer.setAvatar(String.valueOf(map1.get("avatar")));
                if (singerService.get(singer.getId())==null){
                    singerService.addSinger(singer);
                }
                songauthors.add(singer.getId());
            }

            song.setSingerIds(songauthors);

            //把songId存起来返回出去
            songIds.add(song.getId());
//            LOG.info("保存从页面获取的songIds----------------------------------"+songIds);
            if (songService.get(song.getId())==null){
                songService.add(song);
            }
        }
        return songIds;
    }
    //创建者的其他歌单爬虫
    public List<String> othercollection(String userId,String sbujectId){
        List<String> othercollectionIds = new ArrayList<>();
        String url = "https://fm.douban.com/j/v2/songlist/user/"+userId;
        String host = "fm.douban.com";
        String referer = "https://fm.douban.com/songlist/"+sbujectId;
        Map<String,String> headerMap =HttpUtil.buildHeaderData(referer,host);
        headerMap.put("User-Agent",User_Agent);
        String content = HttpUtil.getContent(url,headerMap);
        Map returnData = JSON.parseObject(content,Map.class);
        List songlists = (List) returnData.get("songlists");
        Subject othercols = null;
        for (Object object : songlists){
            Map map = (Map) object;
            othercols = new Subject();
            othercols.setId(String.valueOf(map.get("id")));
            othercols.setName(String.valueOf(map.get("title")));
            //日期转换要先设定好日期格式
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            othercols.setGmtModified(LocalDateTime.parse(String.valueOf(map.get("updated_time")),df));
            othercols.setCover((String) map.get("cover"));
            othercols.setSubjectType(TYPE_COLLECTION);
            othercols.setSubjectSubType(TYPE_COLLECTION_OTHER);
            othercollectionIds.add(othercols.getId());
//            if (subjectService.get(othercols.getId())==null){
//                subjectService.addSubject(othercols);
//            }
        }
        return othercollectionIds;
    }
}
