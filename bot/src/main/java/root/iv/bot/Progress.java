package root.iv.bot;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Progress {
    public int from;
    public int to;
    public Role role;
    public Eats eats;
}
