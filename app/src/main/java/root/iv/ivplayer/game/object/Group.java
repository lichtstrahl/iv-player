package root.iv.ivplayer.game.object;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;

@Data
public class Group {
    private List<DrawableObject> objects;

    public Group() {
        objects = new ArrayList<>();
    }

    public Group(DrawableObject ... objects) {
        this.objects = Arrays.stream(objects).collect(Collectors.toList());
    }

    public static Group empty() {
        return new Group();
    }

    public void add(DrawableObject object) {
        objects.add(object);
    }

    public void render(Canvas canvas) {
        objects.forEach(obj -> obj.render(canvas));
    }

    public int size() {
        return objects.size();
    }
}
