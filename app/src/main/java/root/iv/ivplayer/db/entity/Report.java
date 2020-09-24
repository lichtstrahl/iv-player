package root.iv.ivplayer.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Calendar;

import lombok.Data;

@Data
@Entity
public class Report {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    private String componentName;
    private Calendar timestamp;
    private String description;
    private String json;
}
