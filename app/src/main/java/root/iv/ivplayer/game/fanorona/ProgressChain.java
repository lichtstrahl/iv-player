package root.iv.ivplayer.game.fanorona;

import androidx.core.util.Consumer;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

// Компонент для отслеживания и сохранения последовательности из нескольких ходов
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProgressChain<P> {

    private ArrayDeque<P> chain;
    @Getter
    private boolean end;

    public static <T> ProgressChain<T> empty() {
        return new ProgressChain<>(new ArrayDeque<>(), false);
    }

    public void step(P progress) {
        if (end) throw new IllegalStateException("Chain end");

        chain.add(progress);
    }

    public boolean isEmpty() {
        return chain.isEmpty();
    }

    // Последний пришедший в очередь элемент
    public P last() {
        return chain.peekLast();
    }

    public Stream<P> asStream() {
        return chain.stream();
    }

    public int size() {
        return chain.size();
    }

    public void end() {
        end = true;
    }

    // Применяя действие для каждого элемента: удаляем его из очереди
    // После обработки последовательность снова открыта для записи
    public void process(Consumer<? super P> action) {
        for (P progress = chain.poll(); progress != null; progress = chain.poll()) {
            action.accept(progress);
        }

        end = false;
    }
}
