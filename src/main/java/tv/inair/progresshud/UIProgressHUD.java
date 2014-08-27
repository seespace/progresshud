package tv.inair.progresshud;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;

import inair.app.IAChildLayout;
import inair.app.IALayout;
import inair.app.IARootLayout;
import inair.app.PresentParam;
import inair.tv.TVScreen;
import inair.utils.Transform;
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

  public final IALayout container;
  public final Layout layout;
  public final ViewModel viewModel;

  public UIProgressHUD(IAChildLayout container) {
    this.container = container;
    _resources = container.getResources();

    viewModel = new ViewModel();
    layout = new Layout();
    layout.setDataContext(viewModel);
  }

  public UIProgressHUD(IARootLayout container) {
    this.container = container;
    _resources = container.getResources();

    viewModel = new ViewModel();
    layout = new Layout();
    layout.setDataContext(viewModel);
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

  public UIProgressHUD showSuccess(String status) {
    return show(R.drawable.success, status);
  }

  public UIProgressHUD show(int resId, String status) {
    return show(_resources.getDrawable(resId), status);
  }

  public UIProgressHUD cancel() {
    _showing = false;
    _timer = null;
    _thenDismiss = true;
    _cachedDrawable = null;
    _cachedStatus = null;
    _cancelable = false;
    return this;
  }

  synchronized public UIProgressHUD show(Drawable drawable, String status) {
    if (_cancelable) {
      cancel();
    }

    if (!_thenDismiss && _timer != null) {
      _cachedDrawable = drawable;
      _cachedStatus = status;
    } else {
      showImpl(drawable, status);
    }

    return this;
  }

  synchronized private void showImpl(Drawable drawable, String status) {
    viewModel.setIcon(drawable);
    viewModel.setMessage(status);

    if (!_showing) {
      UIViewDescriptor starting = UIViewDescriptor.create(0f, Transform.Identity().build(), true);
      UIViewDescriptor child = UIViewDescriptor.create(1f, Transform.Identity().build(), false);
      UIViewDescriptor parent = UIViewDescriptor.create(.1f, Transform.Identity().build(), false);
      container.present(layout, PresentParam.create().startingState(starting).childState(child).parentState(parent).screenState(TVScreen.DefaultState.APP_OPENED).duration(1000));
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

  public void dismiss() {
    layout.dismiss();
  }

  //region static
  public static UIProgressHUD with(IAChildLayout container) {
    return new UIProgressHUD(container);
  }

  public static UIProgressHUD with(IARootLayout container) {
    return new UIProgressHUD(container);
  }
  //endregion

  //region internal
  // TODO show more than 2 statuses
  private boolean _showing = false;
  private CountDownTimer _timer = null;
  private boolean _thenDismiss = true;
  private Drawable _cachedDrawable;
  private String _cachedStatus;
  private boolean _cancelable = false;

  private Resources _resources;
  //endregion
}
