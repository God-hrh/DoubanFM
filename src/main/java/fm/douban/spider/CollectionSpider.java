package fm.douban.spider;

import com.alibaba.fastjson.JSON;
import fm.douban.model.Singer;
import fm.douban.model.Song;
import fm.douban.model.Subject;
import fm.douban.service.SingerService;
import fm.douban.service.SongService;
import fm.douban.service.SubjectService;
import fm.douban.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static fm.douban.util.SubjectUtil.*;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/6/19 0:13
 */
public class CollectionSpider {
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
        //歌单爬虫
        getCollectionsData();
    }
    //歌单整体爬虫
    private void getCollectionsData(){
        String url = "https://fm.douban.com/j/v2/songlist/explore?type=hot&genre=0&limit=20&sample_cnt=5";
        String host = "fm.douban.com";
        String referer = "https://fm.douban.com/explore/songlists";
        Map<String,String> headerMap = HttpUtil.buildHeaderData(referer,host);
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
