package tv.inair.progresshud;

import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;

import inair.app.IARootLayout;
import inair.tv.TVScreen;
import inair.view.UIAnimation;
import inair.view.UIAnimationDescriptor;
import inair.view.UIViewDescriptor;

/**
 * <p>
 * Note this class is currently under early design and development.
 * The API will likely change in later updates of the compatibility library,
 * requiring changes to the source code of apps when they are compiled against the newer version.
 * </p>
 */
public class UIProgressHUD {

  IARootLayout mRootLayout;
  boolean mCancelable = true;

  public final Layout layout;
  public final ViewModel viewModel;

  public UIProgressHUD(IARootLayout rootLayout) {
    mRootLayout = rootLayout;

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
    return show(mRootLayout.getResources().getDrawable(drawableResId), mRootLayout.getResources().getString(statusResId));
  }

  public UIProgressHUD showError(String status) {
    return show(R.drawable.error, status);
  }

  public UIProgressHUD showSuccess(String status) {
    return show(R.drawable.success, status);
  }

  public UIProgressHUD show(int resId, String status) {
    return show(mRootLayout.getResources().getDrawable(resId), status);
  }

  synchronized public UIProgressHUD show(Drawable drawable, String status) {
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
      UIAnimationDescriptor parentAnim = UIAnimationDescriptor.createFromViewDescriptor(UIViewDescriptor.create(0.1f, UIAnimation.identityMatrix(), false), 1000);
      UIViewDescriptor tvState = TVScreen.DefaultState.APP_OPENED.getState();
      mRootLayout.presentChildLayout(layout, null, null, parentAnim, tvState, true);
    }

    _thenDismiss = true;
    _timer = null;
    _showing = true;
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
    return this;
  }

  public void dismiss() {
    layout.dismissLayout();
  }

  //region internal
  private boolean _showing = false;
  private CountDownTimer _timer = null;
  private boolean _thenDismiss = true;
  private Drawable _cachedDrawable;
  private String _cachedStatus;
  //endregion
}
