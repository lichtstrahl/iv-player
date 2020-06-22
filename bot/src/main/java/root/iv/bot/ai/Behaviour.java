package root.iv.bot.ai;

import root.iv.bot.Board;
import root.iv.bot.Move;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public interface Behaviour {
    Random RND = new Random(System.currentTimeMillis());

    Move pick(List<Move> moves, Board board);

    default Move randomMove(List<Move> moves){
        if(moves.size()==1){
            return moves.get(0);
        }
        return moves.get(RND.nextInt(moves.size()-1));
    }

    Behaviour NO_THINKING = new NoThinking();
    class NoThinking implements Behaviour{
        public Move pick(List<Move> moves, Board board) {
            return randomMove(moves);
        }
    }

    Behaviour LONGEST = new Longest();
    class Longest implements Behaviour{
        public Move pick(List<Move> moves, Board board) {
            int longest = moves.stream().reduce(0, this::findLength, Math::max);
            moves = moves.stream().filter(it->findLength(0,it)==longest).collect(Collectors.toList());
            return randomMove(moves);
        }
        private int findLength(int l, Move m){
            for(int i=1; ; ++i){
                m=m.cont;
                if(m==null){
                    return Math.max(l, i);
                }
            }
        }
    }
}
