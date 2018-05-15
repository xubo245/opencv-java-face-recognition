package dnn;

import org.opencv.core.*;
import org.opencv.dnn.*;
import org.opencv.imgproc.*;
import org.opencv.imgcodecs.*;

public class DarkNet {

  static String[] names = new String[]{
      "aeroplane","bicycle","bird","boat","bottle",
      "bus","car","cat","chair","cow",
      "diningtable","dog","horse","motorbike","person",
      "pottedplant","sheep","sofa","train","tvmonitor"
  };
  public static void main(String[] args) {
    System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

    Net net = Dnn.readNetFromDarknet("tiny-yolo-voc.cfg", "tiny-yolo-voc.weights");
    if ( net.empty() ) {
      System.out.println("Reading Net error");
    }

    String image_file = "dog.jpg";//IMG_9452.JPG
    Mat im = Imgcodecs.imread(image_file, Imgcodecs.IMREAD_COLOR);
    if( im.empty() ) {
      System.out.println("Reading Image error");
    }

    Mat frame = new Mat();
    Size sz1 = new Size(im.cols(),im.rows());
    Imgproc.resize(im, frame, sz1);

    Mat resized = new Mat();
    Size sz = new Size(416,416);
    Imgproc.resize(im, resized, sz);

    float scale = 1.0F / 255.0F;
    Mat inputBlob = Dnn.blobFromImage(im, scale, sz, new Scalar(0), false, false);
    net.setInput(inputBlob, "data");
    Mat detectionMat = net.forward("detection_out");
    if( detectionMat.empty() ) {
      System.out.println("No result");
    }

    for (int i = 0; i < detectionMat.rows(); i++)
    {
      int probability_index = 5;
      int size = (int) (detectionMat.cols() * detectionMat.channels());

      float[] data = new float[size];
      detectionMat.get(i, 0, data);
      float confidence = -1;
      int objectClass = -1;
      for (int j=0; j < detectionMat.cols();j++)
      {
        if (j>=probability_index && confidence<data[j])
        {
          confidence = data[j];
          objectClass = j-probability_index;
        }
      }

      if (confidence > 0.3)
      {
        System.out.println("Result Object: "+i);
        for (int j=0; j < detectionMat.cols();j++)
          System.out.print(" "+j+":"+ data[j]);
        System.out.println("");
        float x = data[0];
        float y = data[1];
        float width = data[2];
        float height = data[3];
        float xLeftBottom = (x - width / 2) * frame.cols();
        float yLeftBottom = (y - height / 2) * frame.rows();
        float xRightTop = (x + width / 2) * frame.cols();
        float yRightTop = (y + height / 2) * frame.rows();

        System.out.println("Class: "+ names[objectClass]);
        System.out.println("Confidence: "+confidence);

        System.out.println("ROI: "+xLeftBottom+" "+yLeftBottom+" "+xRightTop+" "+yRightTop+"\n");

        Imgproc.rectangle(frame, new Point(xLeftBottom, yLeftBottom),
            new Point(xRightTop,yRightTop),new Scalar(0, 255, 0),3);
      }
    }

    Imgcodecs.imwrite("out.jpg", frame );

  }

}