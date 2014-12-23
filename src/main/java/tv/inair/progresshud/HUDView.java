package tv.inair.progresshud;

import android.os.Bundle;

import inair.app.IAChildLayout;
import inair.event.AnonymousHandler;
import inair.event.Delegate;
import inair.input.SwipeEventArgs;
import inair.view.UIView;

/**
 * <p>
 * Note this class is currently under early design and development.
 * The API will likely change in later updates of the compatibility library,
 * requiring changes to the source code of apps when they are compiled against the newer version.
 * </p>
 */
@SuppressWarnings("unchecked")
public class HUDView extends IAChildLayout {

  @Override
  public void onInitialize(Bundle savedInstanceState) {
    setRootContentView(R.layout.progresshud);

    if (currentHud != null) {
      addHandlerForUIView(UIView.PreviewSwipeEvent, onSwipeToDismiss);
    }
  }

  boolean enableCallback;
  UIProgressHUD currentHud;

  final Delegate<SwipeEventArgs> onSwipeToDismiss = Delegate.createHandler(new AnonymousHandler<SwipeEventArgs>() {
    @Override
    public void handler(Object sender, SwipeEventArgs args) {
      if (args.direction == SwipeEventArgs.Direction.Right) {
        enableCallback = true;
        currentHud.dismiss(true);
      }
    }
  }, SwipeEventArgs.class);

  @Override
  protected void onDismissed() {
    if (currentHud != null) {
      currentHud._hudDismissed(enableCallback);
    }
  }
}
