package root.iv.ivplayer.service.command;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import root.iv.ivplayer.app.App;
import root.iv.ivplayer.db.entity.Report;
import timber.log.Timber;

// Писатель отчётов. Бескончено составляет отчеты раз в заданное количество секунд
public class CommandReporter implements Runnable {

    private CompositeDisposable compositeDisposable;
    private int delay;
    private boolean active;

    public static CommandReporter create(int delaySeconds) {
        CommandReporter reporter = new CommandReporter();

        reporter.compositeDisposable = new CompositeDisposable();
        reporter.delay = delaySeconds;
        reporter.active = true;

        return reporter;
    }

    @Override
    public void run() {

        while (active) {
            try {
                Report report = Report.create(CommandReporter.class.getSimpleName());

                Disposable d = App.getIvDatabase().reportDAO().insert(report)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(id -> Timber.i("insert report: %d", id), Timber::e);
                compositeDisposable.add(d);

                Thread.sleep(1000L * delay);
            } catch (InterruptedException e) {
                Timber.e(e);
                Thread.currentThread().interrupt();
            }


        }
    }

    public void dispose() {
        compositeDisposable.dispose();
    }
}
