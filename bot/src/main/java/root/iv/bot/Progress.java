package root.iv.bot;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Progress {
        private Role role;
        private Integer from;
        private Integer to;
}
