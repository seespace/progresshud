package tv.inair.progresshud;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;

import java.util.HashMap;

import inair.app.DismissParam;
import inair.app.IALayout;
import inair.app.IARootLayout;
import inair.app.InAiRApplication;
import inair.app.PresentParam;
import inair.event.AnonymousHandler;
import inair.event.Delegate;
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

  public static final int DEFAULT_SHOW_DURATION = 3000;

  private IALayout container;
  private Layout layout;
  private ViewModel viewModel;

  private static final UIProgressHUD instance = new UIProgressHUD();

  private UIProgressHUD() {
    viewModel = new ViewModel();
  }

  public UIProgressHUD withContainerView(UIView container) {
    viewModel.setContainer(container);
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

  public UIProgressHUD reset() {
    _showing = false;
    _thenDismiss = true;
    _cachedDrawable = null;
    _cachedStatus = null;
    _cancelable = false;

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

  synchronized public UIProgressHUD show(Drawable drawable, String status) {
    if (!container.isAppeared()) {
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

  public UIProgressHUD iconWidth(float width) {
    viewModel.setIconWidth(width);
    return this;
  }

  public UIProgressHUD iconHeight(float height) {
    viewModel.setIconHeight(height);
    return this;
  }

  synchronized private void showImpl(Drawable drawable, String status) {
    viewModel.setIcon(drawable);
    viewModel.setMessage(status);

    if (!_showing) {
      UIViewDescriptor starting = UIViewDescriptor.create(0f, Transform.fromIdentity().build(), true);
      UIViewDescriptor child = UIViewDescriptor.create(1f, Transform.fromIdentity().build(), false);
      UIViewDescriptor parent = UIViewDescriptor.create(.1f, Transform.fromIdentity().build(), false);

      PresentParam param = PresentParam.create()
        .childStartingState(starting)
        .childState(child)
        .parentState(parent)
        .keepTVScreenState()
        .duration(500);

      container.present(layout, param);
    } else {
      layout.spin();
    }

    _showing = true;

    _cancelable = true;
  }

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
    _cancelable = false;
    return this;
  }

  public boolean dismiss() {
    return dismiss(false);
  }

  public boolean dismiss(boolean force) {
    if (layout == null) {
      return true;
    }

    _showCount = 0;
    _showing = false;

    if (_timer != null) {
      _timer.cancel();
    }

    DismissParam param = new DismissParam();

    if (force) {
      param.duration(1);
    }

    return layout.dismiss(param);
  }

  //region static
  public static UIProgressHUD with(IALayout container) {
    if (instance.container == container && instance._showing) {
      instance.reset();
      return instance;
    }
    instance.container = container;
    instance._resources = InAiRApplication.getAppContext().getResources();
    instance.setup();
    return instance;
  }

  public static UIProgressHUD with(IARootLayout container) {
    if (instance.container == container && instance._showing) {
      instance.reset();
      return instance;
    }
    instance.container = container;
    instance._resources = container.getResources();
    instance.setup();
    return instance;
  }

  private void setup() {
    instance.dismiss(true);

    instance.layout = new Layout();
    instance.layout.setDataContext(instance.viewModel);

    instance.reset();

    instance.layout.didPresent.addHandler(didPresent);
  }

  Delegate didPresent = Delegate.createHandler(new AnonymousHandler<Void>() {
    @Override
    public void handler(Object sender, Void args) {
      Delegate<TouchEventArgs> doubleTapHandler = doubleTapHandlerMap.get(instance.container);
      if (doubleTapHandler != null) {
        instance.layout.addHandlerForUIView(UIView.DoubleTapEvent, doubleTapHandler);
      }

      Delegate<SwipeEventArgs> swipeHandler = swipeHandlerMap.get(instance.container);
      if (swipeHandler != null) {
        instance.layout.addHandlerForUIView(UIView.SwipeEvent, swipeHandler);
      }
    }
  }, Void.class);
  //endregion

  //region events
  final HashMap<IALayout, Delegate<TouchEventArgs>> doubleTapHandlerMap = new HashMap<>();
  final HashMap<IALayout, Delegate<SwipeEventArgs>> swipeHandlerMap = new HashMap<>();

  public UIProgressHUD onDoubleTap(Delegate<TouchEventArgs> handler) {
    doubleTapHandlerMap.put(container, handler);
    return this;
  }

  public UIProgressHUD onSwipe(Delegate<SwipeEventArgs> handler) {
    swipeHandlerMap.put(container, handler);
    return this;
  }
  //endregion

  //region internal
  // TODO implement state machine
  private int _showCount = 0;
  private boolean _showing = false;
  private CountDownTimer _timer = null;
  private boolean _thenDismiss = true;
  private Drawable _cachedDrawable;
  private String _cachedStatus;
  private boolean _cancelable = false;

  private Resources _resources;
  //endregion
}
