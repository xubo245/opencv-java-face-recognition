package ColorBasedObjectTracker;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import javax.swing.JPanel;
import org.opencv.core.Mat;

public class Panel extends JPanel {
	private static final long serialVersionUID = 1L;
	private BufferedImage image;
 
	public Panel() {
		super();
	}
	private BufferedImage getimage() {
		return image;
	}
	public void setimage(BufferedImage newimage) {
		image = newimage;
		return;
	}
 
	public void setimagewithMat(Mat newimage) {
		image = this.matToBufferedImage(newimage);
		return;
	}
 


  private static BufferedImage matToBufferedImage(Mat original)
  {
    // init
    BufferedImage image = null;
    int width = original.width(), height = original.height(), channels = original.channels();
    byte[] sourcePixels = new byte[width * height * channels];
    original.get(0, 0, sourcePixels);

    if (original.channels() > 1)
    {
      image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    }
    else
    {
      image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    }
    final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

    return image;
  }

 
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
 
		BufferedImage temp = getimage();
 
		if (temp != null)
			g.drawImage(temp, 10, 10, temp.getWidth(), temp.getHeight(), this);
	}
}