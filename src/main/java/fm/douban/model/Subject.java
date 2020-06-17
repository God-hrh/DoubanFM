package fm.douban.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/5/27 14:04
 */
public class Subject {
    private String id;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtCreated;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtModified;
    //名称
    private String name;
    //详细说明
    private String description;
    //封面图
    private String cover;
    //对歌单(collection)来说指作者
    // 对兆赫(MHZ)来讲指音乐家
    private String master;
    //发布时间
    private LocalDate publishedDate;
    //主题的一级分类。兆赫(MHZ)，歌单(collection)
    private String subjectType;
    //主题的二级分类。兆赫(mhz)下的细分类: artist (从艺术家出发)、mood (心情/场景)、age (语言/年代)、style (风格/流派)
    private String subjectSubType;
    //关联的歌曲列表
    private List<String> songIds;
    //关联的创建者其他歌单
    private List<String> collectionIds;
    //相关艺术家列表
    private List<String> artistIds;
    //单个歌单封面展示的歌曲名称列表
    private List<String> songnames;
    //单个歌单封面展示的歌曲作者列表
    private List<String> songauthors;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(LocalDateTime gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    public LocalDateTime getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String getSubjectSubType() {
        return subjectSubType;
    }

    public void setSubjectSubType(String subjectSubType) {
        this.subjectSubType = subjectSubType;
    }

    public List<String> getSongIds() {
        return songIds;
    }

    public void setSongIds(List<String> songIds) {
        this.songIds = songIds;
    }

    public List<String> getArtistIds() {
        return artistIds;
    }

    public void setArtistIds(List<String> artistIds) {
        this.artistIds = artistIds;
    }

    public List<String> getSongnames() {
        return songnames;
    }

    public void setSongnames(List<String> songnames) {
        this.songnames = songnames;
    }

    public List<String> getSongauthors() {
        return songauthors;
    }

    public void setSongauthors(List<String> songauthors) {
        this.songauthors = songauthors;
    }

    public List<String> getCollectionIds() {
        return collectionIds;
    }

    public void setCollectionIds(List<String> collectionIds) {
        this.collectionIds = collectionIds;
    }
}
