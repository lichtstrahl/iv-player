package root.iv.ivplayer.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import root.iv.ivplayer.db.dao.ReportDAO;
import root.iv.ivplayer.db.entity.Report;

@Database(entities = {Report.class}, version = 1)
public abstract class IVDatabase extends RoomDatabase {
    public static final String NAME = "iv-database";

    public abstract ReportDAO reportDAO();
}
