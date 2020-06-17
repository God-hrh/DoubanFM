package fm.douban.param;

import fm.douban.model.Song;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/5/26 13:40
 */
public class SongQueryParam extends Song {
    private int pageNum=1;
    private int pageSize=10;

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
