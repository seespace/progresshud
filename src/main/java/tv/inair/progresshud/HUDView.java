package tv.inair.progresshud;

import android.os.Bundle;

import inair.app.IAChildLayout;
import inair.data.PropertyChangedEventArgs;
import inair.event.Delegate;
import inair.view.UIImageView;

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

    ((UIImageView) findUIViewById(R.id.spinner)).start();

    ViewModel viewModel = (ViewModel) getDataContext();
    viewModel.propertyDidChange.addHandler(Delegate.create(this, "onPropertyChanged", PropertyChangedEventArgs.class));
  }

  public void onPropertyChanged(Object sender, PropertyChangedEventArgs args) {
    if (args.propertyName.equals("icon")) {
      spin();
    }
  }

  public void spin() {
    UIImageView icon = ((UIImageView) findUIViewById(R.id.spinner));
    if (icon != null) {
      icon.start();
    }
  }

}