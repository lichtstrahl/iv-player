package root.iv.bot;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FanoronaBot {
    private int rowCount;
    private int columnCount;
    private Role role;

    public static FanoronaBot defaultSize(Role role) {
        return new FanoronaBot(5, 9, role);
    }
}
