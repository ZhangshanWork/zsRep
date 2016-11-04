package im.vinci.server.songbook.persistence;

import im.vinci.server.songbook.model.SongBook;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by mlc on 2016/6/28.
 */
@Repository
public interface SongBookDao {
    @Select("select * from song_book  where user_id = #{userId}")
    public List<SongBook> getSongBook(SongBook songBook);

    @Insert("insert into song_book (sid, user_id, title, artist,type,create_date,ifdel) " +
            "values " +
            "(#{sid}, #{userId}, #{title}, #{artist}, #{type}, #{createDate}, #{ifdel})")
    @Options(useGeneratedKeys = true)
    public void doSave(SongBook songBook);

    @Update("update song_book set ifdel = #{ifdel} where id = #{id} and type =#{type}")
    public void doDel(SongBook songBook);

    @Delete("delete from song_book where 1=1")
    public void deDelAll();
}
