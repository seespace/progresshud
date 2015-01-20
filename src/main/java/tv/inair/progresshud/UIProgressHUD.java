package tv.inair.progresshud;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import java.util.HashMap;

import inair.app.DismissParam;
import inair.app.IANavigation;
import inair.app.InAiRApplication;
import inair.app.PresentParam;
import inair.collection.CollectionChangedEventArgs;
import inair.collection.ObservableCollection;
import inair.event.AnonymousHandler;
import inair.event.Delegate;
import inair.exception.IllegalArgumentNullException;
import inair.input.SwipeEventArgs;
import inair.input.TouchEventArgs;
import inair.utils.Transform;
import inair.view.UIView;
import inair.view.UIViewDescriptor;

/**
 * <p>
 * Note this class is currently under early design and development.
 * The API will likely change in later updates of the compatibility library,
 * requiring changes to the source code of apps when they are compiled against the newer version.
 * </p>
 */
public class UIProgressHUD {

  public static final String TAG = "UIProgressHUD";
  private static final long DEFAULT_DURATION = 3000;

  private IANavigation container;
  final HashMap<String, Delegate<TouchEventArgs>> doubleTapHandlerMap = new HashMap<>();
  final HashMap<String, Delegate<SwipeEventArgs>> swipeHandlerMap = new HashMap<>();

  private HUDView layout;
  private ViewModel viewModel;
  private final ObservableCollection<Task> taskQ = new ObservableCollection<>();

  private static final UIProgressHUD instance = new UIProgressHUD();

  private static Drawable LOADING_DRAWABLE;
  private static Drawable ERROR_DRAWABLE;
  private static Drawable SUCCESS_DRAWABLE;

  private UIProgressHUD() {
    _resources = InAiRApplication.getAppContext().getResources();
    LOADING_DRAWABLE = _resources.getDrawable(R.anim.spinner);
    ERROR_DRAWABLE = _resources.getDrawable(R.drawable.error);
    SUCCESS_DRAWABLE = _resources.getDrawable(R.drawable.success);

    viewModel = new ViewModel();
    taskQ.collectionDidChange.addHandler(Delegate.createHandler(onTaskQChanged, CollectionChangedEventArgs.class));
  }

  public static UIProgressHUD with(IANavigation container) {
    if (container == null) {
      throw new IllegalArgumentNullException("container");
    }
    if (instance.container == container && instance.isShowing()) {
      instance.reset();
      return instance;
    }

    instance.setup(container);
    return instance;
  }

  public UIProgressHUD basedOnFrame(UIView view) {
    if (ensureContainer()) {

      viewModel.setContainer(view);
    }
    return this;
  }

  //region API
  public UIProgressHUD iconWidth(float width) {
    if (!taskQ.isEmpty()) {
      Task task = taskQ.get(taskQ.size() - 1);
      task.setIconWidth(width);
    }
    return this;
  }

  public UIProgressHUD iconHeight(float height) {
    if (!taskQ.isEmpty()) {
      Task task = taskQ.get(taskQ.size() - 1);
      task.setIconHeight(height);
    }
    return this;
  }

  public boolean isShowing() {
    if (taskQ.isEmpty()) {
      return false;
    }

    Task task = taskQ.get(taskQ.size() - 1);
    return task.promise().isPending();
  }

  public UIProgressHUD show() {
    return show("");
  }

  public UIProgressHUD show(int statusResId) {
    return show(LOADING_DRAWABLE, statusResId);
  }

  public UIProgressHUD show(String status) {
    return show(LOADING_DRAWABLE, status);
  }

  public UIProgressHUD show(Drawable drawable, int statusResId) {
    return show(drawable, _resources.getString(statusResId));
  }

  public UIProgressHUD show(int drawableResId, int statusResId) {
    return show(_resources.getDrawable(drawableResId), _resources.getString(statusResId));
  }

  public UIProgressHUD showError(String status) {
    return show(ERROR_DRAWABLE, status);
  }

  public UIProgressHUD showError(int statusResId) {
    return showError(_resources.getString(statusResId));
  }

  public UIProgressHUD showSuccess(String status) {
    return show(SUCCESS_DRAWABLE, status);
  }

  public UIProgressHUD showSuccess(int statusResId) {
    return showSuccess(_resources.getString(statusResId));
  }

  public UIProgressHUD show(int resId, String status) {
    return show(_resources.getDrawable(resId), status);
  }

  synchronized public UIProgressHUD show(Drawable drawable, String status) {
    if (!container.isInitialized()) {
      return this;
    }

    Task task = new Task(viewModel);
    task.setDrawable(drawable);
    task.setMessage(status);
    taskQ.add(task);

    task.promise().done(new DoneCallback<Task>() {
      @Override
      public void onDone(Task result) {
        if (result.isThenDismiss()) {
          dismiss();
        }
      }
    });

    task.promise().fail(new FailCallback<String>() {
      @Override
      public void onFail(String reason) {
        Log.d(TAG, "Task cancelled, reason: " + reason);
      }
    });

    return this;
  }

  public static final UIViewDescriptor STARTING_STATE = UIViewDescriptor.create().alpha(0f).transform(Transform.fromIdentity().build()).seal();
  public static final UIViewDescriptor CHILD_STATE = UIViewDescriptor.create().alpha(1f).transform(Transform.fromIdentity().build()).seal();
  public static final UIViewDescriptor PARENT_STATE = UIViewDescriptor.create().alpha(.1f).transform(Transform.fromIdentity().build()).seal();

  public boolean dismiss() {
    return dismiss(false);
  }

  public boolean dismiss(boolean force) {
    if (layout == null || !ensureContainer()) {
      return true;
    }

    // clean up
    layout.setDataContext(null);
    doubleTapHandlerMap.remove(container.getClass().getName());
    swipeHandlerMap.remove(container.getClass().getName());
    return force ? layout.dismiss(FORCE_PARAM) : layout.dismiss();
  }

  private static final DismissParam FORCE_PARAM = DismissParam.create().duration(1);

  public UIProgressHUD in(long ms) {
    if (!taskQ.isEmpty()) {
      Task task = taskQ.get(taskQ.size() - 1);
      task.setDuration(ms);
    }
    return this;
  }

  public UIProgressHUD then() {
    if (!taskQ.isEmpty()) {
      Task task = taskQ.get(taskQ.size() - 1);
      if (task.getDuration() == Integer.MAX_VALUE) {
        task.setDuration(DEFAULT_DURATION);
      }
    }
    return this;
  }

  public UIProgressHUD dismissable() {
    _canDismiss = true;
    return this;
  }
  //endregion

  //region Events
  public UIProgressHUD terminateAppOnSwipeLeft() {
    terminateAppOnSwipe(SwipeEventArgs.Direction.Left);
    return this;
  }

  public UIProgressHUD terminateAppOnSwipe(SwipeEventArgs.Direction direction) {
    _onSwipe(makeDelegateWith(direction));
    return this;
  }

  private Delegate<SwipeEventArgs> makeDelegateWith(final SwipeEventArgs.Direction directionAction) {
    return Delegate.createHandler(new AnonymousHandler<SwipeEventArgs>() {
      @Override
      public void handler(Object sender, SwipeEventArgs args) {
        if (args.direction == directionAction) {
          dismiss(true);
          InAiRApplication.terminateApp();
        }
      }
    }, SwipeEventArgs.class);
  }

  public UIProgressHUD onDoubleTap(Delegate<TouchEventArgs> handler) {
    _onDoubleTap(handler);
    return this;
  }

  public UIProgressHUD onDismiss(Delegate<Void> handler) {
    layout.didDismiss.addHandler(handler);
    return this;
  }

  private UIProgressHUD _onDoubleTap(Delegate<TouchEventArgs> handler) {
    if (ensureContainer()) {
      doubleTapHandlerMap.put(container.getClass().getName(), handler);
    }
    return this;
  }

  private UIProgressHUD _onSwipe(Delegate<SwipeEventArgs> handler) {
    if (ensureContainer()) {
      swipeHandlerMap.put(container.getClass().getName(), handler);
    }
    return this;
  }

  void _hudDismissed(boolean dismissContainer) {
    if (dismissContainer && ensureContainer()) {
      container.dismiss();
    }
    container = null;
  }

  private boolean ensureContainer() {
    return container != null;
  }

  private AnonymousHandler<CollectionChangedEventArgs> onTaskQChanged = new AnonymousHandler<CollectionChangedEventArgs>() {
    @Override
    public void handler(Object sender, CollectionChangedEventArgs args) {
      if (args.action == CollectionChangedEventArgs.CollectionChangedAction.Add) {
        final Task firstTask = (Task) args.newItems.get(0);
        if (args.newStartingIndex == 0) {
          if (!layout.isAppeared()) {
            firstTask.run();

            PresentParam param = PresentParam.create()
              .childStartingState(STARTING_STATE)
              .childState(CHILD_STATE)
              .parentState(PARENT_STATE)
              .keepTVScreenState()
              .disableDefaultDismissGesture()
              .duration(500);

            if (_canDismiss) {
              layout.currentHud = UIProgressHUD.this;
            }
            container.present(layout, param);

            if (layout.isAppeared()) {
              layout.spinner.start();
            }
          } else {
            layout.spinner.stop();
            firstTask.run();
            layout.spinner.start();
          }
        }

        for (int i = 0; i < args.newItems.size() - 1; i++) {
          Task before = (Task) args.newItems.get(i);
          final Task after = (Task) args.newItems.get(i + 1);
          pipeTask(before, after);
        }

        if (args.newStartingIndex > 0 && taskQ.size() > args.newStartingIndex) {
          Task lastTask = taskQ.get(args.newStartingIndex - 1);
          if (lastTask.promise().isPending()) {
            pipeTask(lastTask, firstTask);
          } else {
            layout.spinner.stop();
            firstTask.run();
            layout.spinner.start();
          }
        }
      }
    }
  };

  private void pipeTask(final Task before, final Task after) {
    before.setThenDismiss(false);
    before.promise().done(new DoneCallback<Task>() {
      @Override
      public void onDone(Task task) {
        layout.spinner.stop();
        after.run();
        layout.spinner.start();
      }
    });
  }
  //endregion

  //region Internal
  private void setup(IANavigation container) {
    instance.dismiss(true);

    // setup for new hud
    instance.container = container;

    instance.layout = new HUDView();
    instance.viewModel.setContainer(container.getRootView());
    instance.layout.setDataContext(instance.viewModel);

    instance.reset();
    _canDismiss = false;
    instance.layout.didPresent.addHandler(Delegate.create(this, "onHUDPresented", Void.class));
  }

  private UIProgressHUD reset() {
    _canDismiss = true;

    if (viewModel != null) {
      viewModel.setIconWidth(32);
      viewModel.setIconHeight(32);
    }

    for (Task task : instance.taskQ) {
      if (task.promise().isPending()) {
        task.cancel();
      }
    }

    instance.taskQ.clear();

    return this;
  }

  public void onHUDPresented(Object sender, Void args) {
    if (ensureContainer()) {
      Delegate<TouchEventArgs> doubleTapHandler = doubleTapHandlerMap.get(container.getClass().getName());
      if (doubleTapHandler != null) {
        instance.layout.addHandlerForUIView(UIView.DoubleTapEvent, doubleTapHandler);
      }

      Delegate<SwipeEventArgs> swipeHandler = swipeHandlerMap.get(container.getClass().getName());
      if (swipeHandler != null) {
        instance.layout.addHandlerForUIView(UIView.SwipeEvent, swipeHandler);
      }
    }
  }

  private boolean _canDismiss = true;
  private Resources _resources;
  //endregion
}
