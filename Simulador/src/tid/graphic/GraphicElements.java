package tid.graphic;

import java.awt.Point;

import javax.swing.JPanel;

public interface GraphicElements {
	public abstract void setStandarPosition(Point position);
	public abstract Point getStandarPosition();
	public abstract void painting();
	public String toHtmlString();
//	public JPanel toolTip();
}
