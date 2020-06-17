package fm.douban.service;

import fm.douban.model.Favorite;

import java.util.List;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/6/16 21:24
 */
public interface FavoriteService {
    //新增一个喜欢
    public Favorite add(Favorite fav);
    //计算喜欢数。如果大于0，就表示已经喜欢
    List<Favorite> list(Favorite favParam);
    //删除一个喜欢
    boolean delete(Favorite favParam);
}
