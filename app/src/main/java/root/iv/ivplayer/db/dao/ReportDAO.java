package root.iv.ivplayer.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import io.reactivex.Single;
import root.iv.ivplayer.db.entity.Report;

@Dao
public interface ReportDAO {

    @Insert
    Single<Long> insert(Report report);

    @Update
    Single<Integer> update(Report report); // Количество успешно обновлённых записей
    
    @Query("DELETE FROM Report WHERE id = :id")
    Single<Integer> deleteById(long id);
}
