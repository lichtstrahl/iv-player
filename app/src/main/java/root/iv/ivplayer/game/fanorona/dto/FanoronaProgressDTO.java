package root.iv.ivplayer.game.fanorona.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import root.iv.bot.Eats;
import root.iv.bot.Progress;
import root.iv.bot.Role;
import root.iv.ivplayer.game.fanorona.AttackType;
import root.iv.ivplayer.game.fanorona.FanoronaRole;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FanoronaProgressDTO {
    private FanoronaRole state;
    private Integer from;
    private Integer to;
    private AttackType attack;

    public static FanoronaProgressDTO forward(FanoronaRole role, Integer from, Integer to) {
        return new FanoronaProgressDTO(role, from, to, AttackType.FORWARD);
    }

    public static FanoronaProgressDTO back(FanoronaRole role, Integer from, Integer to) {
        return new FanoronaProgressDTO(role, from, to, AttackType.BACK);
    }

    public static FanoronaProgressDTO passive(FanoronaRole role, Integer from, Integer to) {
        return new FanoronaProgressDTO(role, from, to, AttackType.NO);
    }

    public static FanoronaProgressDTO of(Progress progress) {
        FanoronaRole role = FanoronaRole.of(progress.getRole());
        int from = progress.getFrom();
        int to = progress.getTo();

        switch (progress.getEats()) {
            case ATK:
                return forward(role, from, to);
            case WTH:
                return back(role, from, to);
            case NO:
                return passive(role, from, to);
        }

        throw new IllegalStateException("Не удалось подобрать представление для хода бота");
    }

    public Progress export() {
        Role role = this.state.export();
        int from = this.from;
        int to = this.to;


        switch (attack) {
            case FORWARD:
                return new Progress(from, to, role, Eats.ATK);
            case BACK:
                return new Progress(from, to ,role, Eats.WTH);
            case NO:
                return new Progress(from, to, role, Eats.NO);
        }

        throw new IllegalStateException("Не представить ход для обработки ботом");
    }
}
