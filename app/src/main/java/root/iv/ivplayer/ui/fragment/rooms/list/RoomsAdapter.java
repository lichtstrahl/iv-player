package root.iv.ivplayer.ui.fragment.rooms.list;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import root.iv.ivplayer.R;
import root.iv.ivplayer.network.firebase.dto.FBRoom;
import root.iv.ivplayer.network.firebase.dto.RoomUI;

@AllArgsConstructor
public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.RoomViewHolder> {
    private List<RoomUI> rooms;
    private LayoutInflater inflater;
    private View.OnClickListener roomClickListener;
    private View.OnCreateContextMenuListener createContextMenuListener;
    @Getter
    private int position;

    public static RoomsAdapter empty(LayoutInflater inflater, View.OnClickListener clickListener, View.OnCreateContextMenuListener contextMenuListener) {
        return new RoomsAdapter(new LinkedList<>(), inflater, clickListener, contextMenuListener, Menu.NONE);
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MaterialCardView roomView = (MaterialCardView) inflater.inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(roomView);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        holder.itemView.setOnLongClickListener(v -> {
            this.position = position;
            return false;
        });

        holder.bind(rooms.get(position));
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }


    public void roomNotify(String name, FBRoom room) {
        boolean exist = roomNames().contains(name);

        if (exist)
            updateRoom(name, room);
        else
            addRoom(name, room);
    }

    public void removeRoom(String name) {
        int index = roomNames().indexOf(name);
        rooms.remove(index);
        notifyItemRemoved(index);
    }

    public RoomUI getRoom(int index) {
        return rooms.get(index);
    }

    public List<String> roomNames() {
        return rooms.stream().map(RoomUI::getName).collect(Collectors.toList());
    }

    private void updateRoom(String name, FBRoom fbRoom) {
        int index = roomNames().indexOf(name);
        RoomUI room = RoomUI
                .builder()
                .name(name)
                .fbRoom(fbRoom)
                .build();
        rooms.set(index, room);
        notifyItemChanged(index);
    }

    private void addRoom(String name, FBRoom fbRoom) {
        RoomUI room = RoomUI
                .builder()
                .name(name)
                .fbRoom(fbRoom)
                .build();
        int count = rooms.size();
        rooms.add(room);
        notifyItemInserted(count);
    }

    class RoomViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView viewRoomName;
        private final MaterialTextView viewRoomState;
        private final MaterialTextView viewEmail1;
        private final MaterialTextView viewEmail2;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);

            viewRoomName = itemView.findViewById(R.id.viewRoomName);
            viewRoomState = itemView.findViewById(R.id.viewRoomState);
            viewEmail1 = itemView.findViewById(R.id.viewEmailPlayer1);
            viewEmail2 = itemView.findViewById(R.id.viewEmailPlayer2);
            itemView.setOnClickListener(roomClickListener);
            itemView.setOnCreateContextMenuListener(createContextMenuListener);
        }

        private void bind(RoomUI room) {
            viewRoomName.setText(room.getName());
            viewRoomState.setText(room.getState().name());
            viewEmail1.setText(room.name1());
            viewEmail2.setText(room.name2());
        }
    }
}
