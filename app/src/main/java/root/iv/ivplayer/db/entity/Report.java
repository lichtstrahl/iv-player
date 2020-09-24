package root.iv.ivplayer.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Calendar;

import lombok.Data;
import root.iv.ivplayer.db.converters.CalendarConverter;

@Data
@Entity
@TypeConverters({CalendarConverter.class})
public class Report {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    private String componentName;
    private Calendar timestamp;
    private String description;
    private String json;
}
