package root.iv.ivplayer.game;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum GameType {
    TIC_TAC("Крестики-нолики"),
    FANORONA("Фанорона");

    @Getter
    private String description;
}
