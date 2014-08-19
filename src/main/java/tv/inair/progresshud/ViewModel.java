package tv.inair.progresshud;

import android.graphics.drawable.Drawable;

/**
 * <p>
 * Note this class is currently under early design and development.
 * The API will likely change in later updates of the compatibility library,
 * requiring changes to the source code of apps when they are compiled against the newer version.
 * </p>
 */
public class ViewModel extends inair.data.ViewModel {

  private String mMessage;
  private Drawable mIcon;

  public String getMessage() {
    return mMessage;
  }

  public void setMessage(String message) {
    mMessage = message;
    notifyPropertyChanged("message");
  }

  public Drawable getIcon() {
    return mIcon;
  }

  public void setIcon(Drawable icon) {
    mIcon = icon;
    notifyPropertyChanged("icon");
  }

  private float mIconWidth = 32, mIconHeight = 32;

  public float getIconWidth() {
    return mIconWidth;
  }

  public void setIconWidth(float iconWidth) {
    mIconWidth = iconWidth;
    notifyPropertyChanged("iconWidth");
  }

  public float getIconHeight() {
    return mIconHeight;
  }

  public void setIconHeight(float iconHeight) {
    mIconHeight = iconHeight;
    notifyPropertyChanged("iconHeight");
  }
}
