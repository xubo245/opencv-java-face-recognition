package detection;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import javax.swing.*;

import org.opencv.core.Mat;

public class VideoPanel extends JPanel {

  private BufferedImage image;
  private int width,height, channels;
  byte[] sourcePixels;
  byte[] targetPixels;

  public void setImageWithMat(Mat mat) {
    matToBufferedImage(mat);
  }

  private void matToBufferedImage(Mat original) {
    // init
    if (original.width() == width && height == original.height() && channels == original.channels()) {
      original.get(0, 0, sourcePixels);
      System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);
    } else {
      width = original.width();
      height = original.height();
      channels = original.channels();
      sourcePixels = new byte[width * height * channels];
      original.get(0, 0, sourcePixels);
      if (original.channels() > 1) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
      } else {
        image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
      }
      targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
      System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);
    }
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (image != null) g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), this);
  }
}