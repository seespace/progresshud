package tv.inair.progresshud;

import android.graphics.drawable.Drawable;

import inair.view.UILayeredNavigationView;
import inair.view.UILayeredViewItem;
import inair.view.UIView;

/**
 * <p>
 * Note this class is currently under early design and development.
 * The API will likely change in later updates of the compatibility library,
 * requiring changes to the source code of apps when they are compiled against the newer version.
 * </p>
 */
public class ViewModel extends inair.data.ViewModel {

  public ViewModel() {

//    /**
//     * default usage for LayeredNavigation
//     * @see inair.sdk.R.integer
//     */
//    containerX = 1225;
//    containerY = 167;
//    containerZ = 700;
//
//    containerWidth = 320;
//    containerHeight = 746;
//
//    hudX = (containerWidth - resources.getInteger(R.integer.hud_width)) / 2;
//    hudY = (containerHeight - resources.getInteger(R.integer.hud_height)) / 2;
  }

  private String mMessage;
  private Drawable mIcon;

  public String getMessage() {
    return mMessage;
  }

  public void setMessage(String message) {
    mMessage = message;
    notifyPropertyChanged("message");
    notifyPropertyChanged("iconX");
    notifyPropertyChanged("iconY");
  }

  //region Icon
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
  //endregion

  //region Container
  public void setContainer(UIView container) {
    System.out.println("VIEW: " +container);
    if (container instanceof UILayeredNavigationView || container instanceof UILayeredViewItem) {
      setContainerX(resources.getInteger(R.integer.layered_x_navigator));
      setContainerY(resources.getInteger(R.integer.layered_y_navigator));
      setContainerZ(resources.getInteger(R.integer.layered_first_layer_z));

      setContainerWidth(resources.getInteger(R.integer.layered_layer_width));
      setContainerHeight(resources.getInteger(R.integer.layered_layer_height));
    } else {
      setContainerX(container.getPositionX());
      setContainerY(container.getPositionY());
      setContainerZ(container.getPositionZ());

      setContainerWidth(container.getWidth());
      setContainerHeight(container.getHeight());
    }

    setHudX((containerWidth - resources.getInteger(R.integer.hud_width)) / 2);
    setHudY((containerHeight - resources.getInteger(R.integer.hud_height)) / 2);
    System.out.println(hudX + " : " + hudY);
  }

  private float containerX;
  private float containerY;
  private float containerZ;
  private float containerWidth;
  private float containerHeight;

  public float getContainerX() {
    return containerX;
  }

  public void setContainerX(float containerX) {
    this.containerX = containerX;
    notifyPropertyChanged("containerX");
  }

  public float getContainerY() {
    return containerY;
  }

  public void setContainerY(float containerY) {
    this.containerY = containerY;
    notifyPropertyChanged("containerY");
  }

  public float getContainerZ() {
    return containerZ;
  }

  public void setContainerZ(float containerZ) {
    this.containerZ = containerZ;
    notifyPropertyChanged("containerZ");
  }

  public float getContainerWidth() {
    return containerWidth;
  }

  public void setContainerWidth(float containerWidth) {
    this.containerWidth = containerWidth;
    notifyPropertyChanged("containerWidth");
  }

  public float getContainerHeight() {
    return containerHeight;
  }

  public void setContainerHeight(float containerHeight) {
    this.containerHeight = containerHeight;
    notifyPropertyChanged("containerHeight");
  }
  //endregion

  //region HUD
  private float hudX;
  private float hudY;

  public float getHudY() {
    return hudY;
  }

  public void setHudY(float hudY) {
    this.hudY = hudY;
    notifyPropertyChanged("hudY");
  }

  public float getHudX() {
    return hudX;
  }

  public void setHudX(float hudX) {
    this.hudX = hudX;
    notifyPropertyChanged("hudX");
  }

  //endregion

  public float getIconY() {
    if (mMessage != null && mMessage.isEmpty()) {
      return 84f;
    } else {
      return 40f;
    }
  }

  public float getIconX() {
    return (200 - mIconWidth) / 2;
  }
}
