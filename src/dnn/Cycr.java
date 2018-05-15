package dnn;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;


import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;


public class Cycr  extends JPanel{
  private BufferedImage mImg;

  public void paintComponent(Graphics g) {
    if (mImg != null) {
      g.drawImage(mImg, 0, 0, mImg.getWidth(), mImg.getHeight(), this);
    }
  }


  public static void main(String[] args) {

    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    VideoCapture capture = new VideoCapture("123.mp4");
    Net net = Dnn.readNetFromDarknet("yolov2-tiny.cfg",
        "yolov2-tiny.weights");
    Mat capImg = new Mat();
    if (!capture.isOpened()) {
      return;
    } else {
      Cycr rycr = new Cycr();
      JFrame frame = new JFrame("camera1");
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      rycr.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent arg0) {
        }

        @Override
        public void mouseMoved(MouseEvent arg0) {
        }


        @Override
        public void mouseReleased(MouseEvent arg0) {
        }


        @Override
        public void mousePressed(MouseEvent arg0) {
        }
        @Override
        public void mouseExited(MouseEvent arg0) {
        }
        @Override
        public void mouseDragged(MouseEvent arg0) {
        }
      });
      frame.setContentPane(rycr);
      frame.setVisible(true);
      frame.setSize(800 + frame.getInsets().left + frame.getInsets().right,
          800 + frame.getInsets().top + frame.getInsets().bottom);


      while (true) {
        capture.read(capImg);
        BufferedImage testimg = dnn.Cycr.mat2BI(capImg);
        Rect  rect =new Rect(316, 149, 882 - 316, 717 - 149);//截取指定区域
        Mat im =new Mat(capImg,rect);
        if (im.empty()) {
        }
        BufferedImage imges=  test(im,net);
        if(imges!=null){
          rycr.mImg = imges;
        }else{
          rycr.mImg = testimg;
        }
        rycr.repaint();
      }
    }
  }





  public static BufferedImage test(Mat im,Net net){
    Mat frame1 = new Mat();
    Size sz1 = new Size(im.cols(), im.rows());
    Imgproc.resize(im, frame1, sz1);
    Mat resized = new Mat();
    Size sz = new Size(416, 416);
    Imgproc.resize(im, resized, sz);
    float scale = 1.0F / 255.0F;
    Mat inputBlob = Dnn.blobFromImage(im, scale, sz, new Scalar(0), false, false);
    net.setInput(inputBlob, "data");
    Mat detectionMat = net.forward("detection_out");
    if (detectionMat.empty()) {
      System.out.println("No result");
    }
    for (int i = 0; i < detectionMat.rows(); i++) {
      int probability_index = 5;//有80特征 前4都没用 第5个开始
      int size = (int) (detectionMat.cols() * detectionMat.channels());
      float[] data = new float[size];
      detectionMat.get(i, 0, data);
      float confidence = -1;
      if (data[5] > 0.4) {
        System.out.println(data[5]);
        float x = data[0];
        float y = data[1];
        float width = data[2];
        float height = data[3];
        float xLeftBottom = (x - width / 2) * frame1.cols();
        float yLeftBottom = (y - height / 2) * frame1.rows();
        float xRightTop = (x + width / 2) * frame1.cols();
        float yRightTop = (y + height / 2) * frame1.rows();
        System.out.println("Confidence: " + confidence);
        Imgproc.rectangle(frame1, new Point(xLeftBottom, yLeftBottom), new Point(xRightTop, yRightTop),
            new Scalar(0, 255, 0), 3);
        return dnn.Cycr.mat2BI(frame1);
      }
    }
    return null;
  }


  public static BufferedImage mat2BI(Mat mat) {
    int dataSize = mat.cols() * mat.rows() * (int) mat.elemSize();
    byte[] data = new byte[dataSize];
    mat.get(0, 0, data);
    int type = mat.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR;
    if (type == BufferedImage.TYPE_3BYTE_BGR) {
      for (int i = 0; i < dataSize; i += 3) {
        byte blue = data[i + 0];
        data[i + 0] = data[i + 2];
        data[i + 2] = blue;
      }
    }
    BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
    image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
    return image;
  }






}
