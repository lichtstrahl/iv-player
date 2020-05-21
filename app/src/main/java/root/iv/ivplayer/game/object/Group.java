package root.iv.ivplayer.game.object;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;

@Data
public class Group<T extends DrawableObject> {
    private List<T> objects;

    public Group() {
        objects = new ArrayList<>();
    }

    public Group(T ... objects) {
        this.objects = Arrays.stream(objects).collect(Collectors.toList());
    }

    public static <T extends DrawableObject> Group<T> empty() {
        return new Group<>();
    }

    public void add(T object) {
        objects.add(object);
    }

    public void render(Canvas canvas) {
        objects.forEach(obj -> obj.render(canvas));
    }

    public int size() {
        return objects.size();
    }

    public T getObject(int i) {
        return objects.get(i);
    }
}
