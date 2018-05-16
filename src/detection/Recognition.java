package detection;

import java.text.DecimalFormat;
import javax.swing.*;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.face.FaceRecognizer;
import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

public class Recognition {

  public static void main(String[] args) {

    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

    JFrame cameraFrame = new JFrame("camera");
    cameraFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    cameraFrame.setSize(640, 480);
    cameraFrame.setBounds(0, 0, cameraFrame.getWidth(), cameraFrame.getHeight());
    VideoPanel videoPanel = new VideoPanel();
    cameraFrame.setContentPane(videoPanel);
    cameraFrame.setVisible(true);

    CascadeClassifier faceCascade = new CascadeClassifier();
    faceCascade.load("/usr/share/OpenCV/haarcascades/haarcascade_frontalface_alt.xml");
    FaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();
    faceRecognizer.read("/home/david/Documents/code/OpenCvObjectDetection/model/face_model.yml");
    VideoCapture capture = new VideoCapture();

    try {
      capture.open(0);
      if (capture.isOpened()) {
        Mat image = new Mat();
        while(true) {
          capture.read(image);
          if (!image.empty()) {
            detectAndDisplay(image, faceCascade, faceRecognizer);
            videoPanel.setImageWithMat(image);
            cameraFrame.repaint();
            Thread.sleep(20);
          } else {
            break;
          }
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      capture.release();
    }
  }

  public static void detectAndDisplay(Mat frame, CascadeClassifier faceCascade, FaceRecognizer faceRecognizer)
  {
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
      int[] label = new int[1];
      double[] confidence = new double[1];
      faceRecognizer.predict(
          grayFrame
              .colRange(facesArray[i].x, facesArray[i].x + facesArray[i].width)
              .rowRange(facesArray[i].y, facesArray[i].y + facesArray[i].height),
          label,
          confidence);
      String name = faceRecognizer.getLabelInfo(label[0]);
      Scalar color;
      if (confidence[0] < 50) {
        if (label[0] == 0) {
          color = new Scalar(255,0,0);
        } else if (label[0] == 1) {
          color = new Scalar(0,255,0);
        } else {
          color = new Scalar(0,0,255);
        }
      } else {
        name = "unknown";
        color = new Scalar(255,255,255);
      }

      Imgproc.putText(
          frame,
          name + " " + new DecimalFormat("#.00").format(confidence[0]),
          new Point(facesArray[i].x, facesArray[i].y - 8),
          Core.FONT_HERSHEY_PLAIN, 2, color,2);
      Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), color, 3);
    }

  }

}
