package root.iv.ivplayer.network.firebase;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import timber.log.Timber;

public abstract class FBDataListener implements ValueEventListener {

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
            Timber.i(databaseError.getMessage());
    }
}
