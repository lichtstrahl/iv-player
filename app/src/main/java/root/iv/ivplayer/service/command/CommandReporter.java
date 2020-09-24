package root.iv.ivplayer.service.command;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import root.iv.ivplayer.app.App;
import root.iv.ivplayer.db.entity.Report;
import timber.log.Timber;

// Писатель отчётов. Бескончено составляет отчеты раз в заданное количество секунд
public class CommandReporter extends BaseCommand {

    private CompositeDisposable compositeDisposable;
    private int delay;

    private CommandReporter(int delaySeconds) {
        super(null);

        compositeDisposable = new CompositeDisposable();
        delay = delaySeconds;

        action = this::action;
    }

    public static CommandReporter create(int delaySeconds) {
        return new CommandReporter(delaySeconds);
    }

    @Override
    public void stop() {
        super.stop();
        compositeDisposable.dispose();
    }

    private void action() {
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
