package detection;

import java.awt.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.*;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

public class CollectData {

  private static int startFrom = 100;

  private static int sample = 0;

  public static String path = "./imagedb";

  public static String id = "qiang";

  static AtomicBoolean start = new AtomicBoolean(false);

  public static void main(String[] args) throws InterruptedException {

    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

    CascadeClassifier faceCascade = new CascadeClassifier();
    faceCascade.load(Properties.location+"haarcascade_frontalface_alt.xml");

    Scanner scanner = new Scanner(System.in);
    System.out.println("input id: ");
    id = scanner.next();
    System.out.println("generate image db for : " + id);

    JFrame cameraFrame = new JFrame("camera");
    cameraFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    cameraFrame.setSize(d.width, d.height);
    cameraFrame.setBounds(0, 0, cameraFrame.getWidth(), cameraFrame.getHeight());
    VideoPanel videoPanel = new VideoPanel();
    cameraFrame.setContentPane(videoPanel);
    cameraFrame.setVisible(true);

    new Thread() {
      @Override public void run() {
        try {
          Thread.sleep(10000);
          start.set(true);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

      }
    }.start();

    Mat img = new Mat();
    Mat gray = new Mat();
    MatOfRect faces = new MatOfRect();

    VideoCapture capture = new VideoCapture();
    capture.open(0);

    for (int i = 0; i < 1000; i++) {

      capture.read(img);

      detectAndDisplay(img, faceCascade);

      videoPanel.setImageWithMat(img);
      cameraFrame.repaint();
      Thread.sleep(200);
      if (sample >= 100) {
        break;
      }
    }
    capture.release();

  }

  public static void detectAndDisplay(Mat frame, CascadeClassifier faceCascade) {
    MatOfRect faces = new MatOfRect();
    Mat grayFrame = new Mat();

    // convert the frame in gray scale
    Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
    // equalize the frame histogram to improve the result
    Imgproc.equalizeHist(grayFrame, grayFrame);

    // compute minimum face size (20% of the frame height, in our case)
    int absoluteFaceSize = 0;
    int height = grayFrame.rows();
    if (Math.round(height * 0.2f) > 0) {
      absoluteFaceSize = Math.round(height * 0.2f);
    }

    // detect faces
    faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
        new Size(absoluteFaceSize, absoluteFaceSize), new Size());

    // each rectangle in faces is a face: draw them!
    Rect[] facesArray = faces.toArray();
    for (int i = 0; i < facesArray.length; i++) {

      if (start.get()) {
        sample++;
        System.out.println("sample: " + sample);

        Imgcodecs.imwrite(path + "/image." + id + "." + (startFrom + sample) + ".jpg", frame
            .rowRange(Math.max(facesArray[i].y - 16, 0),
                Math.min(facesArray[i].y + facesArray[i].height + 16, frame.rows()))
            .colRange(Math.max(facesArray[i].x - 16, 0),
                Math.min(facesArray[i].x + facesArray[i].width + 16, frame.cols())));
      }
      Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
    }
  }

}
