package tv.inair.progresshud;

import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;

import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

/**
 * <p>
 * Note this class is currently under early design and development.
 * The API will likely change in later updates of the compatibility library,
 * requiring changes to the source code of apps when they are compiled against the newer version.
 * </p>
 */
public class Task {
  private ViewModel  mViewModel;
  private DeferredObject<Task, String, Integer> mDeferredObject = new DeferredObject<>();

  private Drawable mDrawable;
  private String mMessage;
  private long mDuration;
  private float mIconWidth = 32;
  private float mIconHeight = 32;

  private CountDownTimer mTimer;

  private boolean mStarted = false;
  private boolean mThenDismiss = true;

  public Task(ViewModel viewModel) {
    mViewModel = viewModel;
    setDuration(Integer.MAX_VALUE);
  }

  public boolean isThenDismiss() {
    return mThenDismiss;
  }

  public void setThenDismiss(boolean thenDismiss) {
    mThenDismiss = thenDismiss;
  }

  public Drawable getDrawable() {
    return mDrawable;
  }

  public void setDrawable(Drawable drawable) {
    mDrawable = drawable;
  }

  public String getMessage() {
    return mMessage;
  }

  public void setMessage(String message) {
    mMessage = message;
  }

  public long getDuration() {
    return mDuration;
  }

  public void setDuration(long duration) {
    mDuration = duration;
    if (mTimer != null) {
      mTimer.cancel();
    }

    mTimer = new CountDownTimer(duration, duration) {
      @Override
      public void onTick(long millisUntilFinished) {
        // no tick
      }

      @Override
      public void onFinish() {
        mDeferredObject.resolve(Task.this);
      }
    };

    if (mStarted) {
      mTimer.start();
    }
  }

  public float getIconWidth() {
    return mIconWidth;
  }

  public void setIconWidth(float iconWidth) {
    mIconWidth = iconWidth;
  }

  public float getIconHeight() {
    return mIconHeight;
  }

  public void setIconHeight(float iconHeight) {
    mIconHeight = iconHeight;
  }

  public Promise<Task, String, Integer> promise() {
    return mDeferredObject.promise();
  }

  public void run() {
    mViewModel.setIconWidth(mIconWidth);
    mViewModel.setIconHeight(mIconHeight);
    mViewModel.setIcon(mDrawable);
    mViewModel.setMessage(mMessage);

    if (mTimer != null) {
      mTimer.start();
    }

    mStarted = true;
  }

  public void cancel() {
    cancel("User Cancelled");
  }

  public void cancel(String reason) {
    if (mTimer != null) {
      mTimer.cancel();
    }
    mDeferredObject.reject(reason);
  }
}
