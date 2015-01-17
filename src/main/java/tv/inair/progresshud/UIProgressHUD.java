package tv.inair.progresshud;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;

import java.util.HashMap;

import inair.app.DismissParam;
import inair.app.IANavigation;
import inair.app.InAiRApplication;
import inair.app.PresentParam;
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

  private IANavigation container;
  final HashMap<String, Delegate<TouchEventArgs>> doubleTapHandlerMap = new HashMap<>();
  final HashMap<String, Delegate<SwipeEventArgs>> swipeHandlerMap = new HashMap<>();

  private HUDView layout;
  private ViewModel viewModel;

  private static final UIProgressHUD instance = new UIProgressHUD();

  private UIProgressHUD() {
    _resources = InAiRApplication.getAppContext().getResources();
    viewModel = new ViewModel();
  }

  public static UIProgressHUD with(IANavigation container) {
    if (container == null) {
      throw new IllegalArgumentNullException("container");
    }
    if (instance.container == container && instance._showing) {
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
    viewModel.setIconWidth(width);
    return this;
  }

  public UIProgressHUD iconHeight(float height) {
    viewModel.setIconHeight(height);
    return this;
  }

  public boolean isShowing() {
    return _showing && _showCount > 0;
  }

  public UIProgressHUD show() {
    return show("");
  }

  public UIProgressHUD show(int statusResId) {
    return show(R.anim.spinner, statusResId);
  }

  public UIProgressHUD show(String status) {
    return show(R.anim.spinner, status);
  }

  public UIProgressHUD show(int drawableResId, int statusResId) {
    return show(_resources.getDrawable(drawableResId), _resources.getString(statusResId));
  }

  public UIProgressHUD showError(String status) {
    return show(R.drawable.error, status);
  }

  public UIProgressHUD showError(int statusResId) {
    return showError(_resources.getString(statusResId));
  }

  public UIProgressHUD showSuccess(String status) {
    return show(R.drawable.success, status);
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
    _showCount++;

    if (!_thenDismiss && _timer != null) {
      _cachedDrawable = drawable;
      _cachedStatus = status;
    } else {
      showImpl(drawable, status);
    }

    return this;
  }

  public static final UIViewDescriptor STARTING_STATE = UIViewDescriptor.create().alpha(0f).transform(Transform.fromIdentity().build()).seal();
  public static final UIViewDescriptor CHILD_STATE = UIViewDescriptor.create().alpha(1f).transform(Transform.fromIdentity().build()).seal();
  public static final UIViewDescriptor PARENT_STATE = UIViewDescriptor.create().alpha(.1f).transform(Transform.fromIdentity().build()).seal();

  synchronized private void showImpl(Drawable drawable, String status) {
    if (!ensureContainer()) {
      return;
    }
    viewModel.setIcon(drawable);
    viewModel.setMessage(status);

    if (!_showing) {
      PresentParam param = PresentParam.create()
          .childStartingState(STARTING_STATE)
          .childState(CHILD_STATE)
          .parentState(PARENT_STATE)
          .keepTVScreenState()
          .disableDefaultDismissGesture()
          .duration(500);

      if (_canDismiss) {
        layout.currentHud = this;
      }

      container.present(layout, param);
    }

    _showing = true;
  }

  public boolean dismiss() {
    return dismiss(false);
  }

  public boolean dismiss(boolean force) {
    _showCount = 0;
    _showing = false;

    if (_timer != null) {
      _timer.cancel();
    }

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
    _timer = new CountDownTimer(ms, ms) {
      @Override
      public void onTick(long millisUntilFinished) {
        // no tick
      }

      @Override
      public void onFinish() {
        if (_thenDismiss) {
          dismiss();
        } else {
          showImpl(_cachedDrawable, _cachedStatus);
        }
      }
    };

    _timer.start();

    return this;
  }

  public UIProgressHUD then() {
    _thenDismiss = false;
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
    if (container == null) {
      return false;
    }
    return true;
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
    _showing = false;
    _thenDismiss = true;
    _cachedDrawable = null;
    _cachedStatus = null;
    _canDismiss = true;

    if (_timer != null) {
      _timer.cancel();
    }
    _timer = null;

    if (viewModel != null) {
      viewModel.setIconWidth(32);
      viewModel.setIconHeight(32);
    }
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

  // TODO implement state machine
  private int _showCount = 0;
  private boolean _showing = false;
  private CountDownTimer _timer = null;
  private boolean _thenDismiss = true;
  private Drawable _cachedDrawable;
  private String _cachedStatus;
  private boolean _canDismiss = true;

  private Resources _resources;
  //endregion
}
