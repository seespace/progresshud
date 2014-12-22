package tv.inair.progresshud;

import android.os.Bundle;

import inair.app.IAChildLayout;

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
  }
}
