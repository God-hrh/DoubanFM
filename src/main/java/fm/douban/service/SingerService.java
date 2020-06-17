package fm.douban.service;

import fm.douban.model.Singer;

import java.util.List;

/**
 * @author Heerh
 * @version 1.0
 * @date 2020/5/25 15:47
 */
public interface SingerService {
    public Singer addSinger(Singer singer);
    public Singer get(String singerId);
    public Singer getByMaster(String master);
    public List<Singer> getAll();
    public boolean modify(Singer singer);
    public boolean delete(String singerId);
}
