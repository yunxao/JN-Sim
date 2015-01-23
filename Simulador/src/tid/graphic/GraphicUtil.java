package tid.graphic;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;

import javax.swing.ImageIcon;

public class GraphicUtil {
	public static Image loadImage(String rute){
		return new ImageIcon(rute).getImage();
	}
	protected static Point centerPosition(Dimension d){
		Point center = new Point();

		center.x = (d.width / 2);
		center.y = (d.height / 2);
		return center;
	}
	protected static Point centerPosition(Component component) {
		Point center = new Point();

		center.x = (component.getWidth() / 2);
		center.y = (component.getHeight() / 2);
		return center;
	}

	protected static Point locationToBeCenter(Component base, Component element,
			boolean absolute) {
		Point position = new Point();
		Point baseCenter = centerPosition(base);
		position.x = baseCenter.x - element.getWidth() / 2;
		position.y = baseCenter.y - element.getHeight() / 2;
		if (absolute) {
			position.x += base.getLocation().x;
			position.y += base.getLocation().y;
		}

		return position;
	}
}
