package root.iv.ivplayer.service.command;

import android.media.AudioRecord;

import timber.log.Timber;

public class CommandAudioRead  {
    private AudioRecord audioRecord;
    private int bufferSize;
    private boolean active;

    public CommandAudioRead(AudioRecord audioRecord) {
        this.audioRecord = audioRecord;
        bufferSize = 8192;
        active = true;
    }

    private void action() {
        byte[] buffer = new byte[bufferSize];
        int readCount = 0;
        int totalCount = 0;

        while (active) {
            readCount = audioRecord.read(buffer, 0, bufferSize);
            totalCount += readCount;
            Timber.i("readCount: %d, totalCount: %d", readCount, totalCount);
        }
    }

    public void stop() {
        active = false;
    }
}
