package detection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.face.FaceRecognizer;
import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

public class Train {

  public static void main(String[] args) throws IOException {

    getImagesAndLabels("/home/david/Documents/code/OpenCvObjectDetection/imagedb",
        "/home/david/Documents/code/OpenCvObjectDetection/model");

  }

  public static void getImagesAndLabels(String imageFolder, String saveFolder)
      throws IOException {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    FaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();
    CascadeClassifier faceCascade = new CascadeClassifier();
    faceCascade.load("/usr/share/OpenCV/haarcascades/haarcascade_frontalface_alt.xml");
    File folder = new File(imageFolder);
    File[] files = folder.listFiles();
    Map<String, Integer> nameMapId = new HashMap<String, Integer>(10);
    List<Mat> images = new ArrayList<Mat>(files.length);
    List<String> names = new ArrayList<String>(files.length);
    List<Integer> ids = new ArrayList<Integer>(files.length);
    for (int index = 0; index < files.length; index++ ) {
      File file = files[index];
      String name = file.getName().split("\\.")[1];
      Integer id = nameMapId.get(name);
      if (id == null) {
        id = names.size();
        names.add(name);
        nameMapId.put(name, id);
        faceRecognizer.setLabelInfo(id, name);
      }

      Mat mat = Imgcodecs.imread(file.getCanonicalPath());
      Mat gray = new Mat();

      // convert the frame in gray scale
      Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);

      MatOfRect faces = new MatOfRect();


      int absoluteFaceSize = 0;
      int height = gray.rows();
      if (Math.round(height * 0.5f) > 0) {
        absoluteFaceSize = Math.round(height * 0.5f);
      }

      // detect faces
      faceCascade.detectMultiScale(gray, faces, 1.1, 2, 0 | Objdetect.CASCADE_FIND_BIGGEST_OBJECT,
          new Size(absoluteFaceSize, absoluteFaceSize), new Size());

      Rect[] facesArray = faces.toArray();
      for (Rect face: facesArray) {
        images.add(gray.rowRange(face.y, face.y + face.height).colRange(face.x, face.x + face.width));
        System.out.println("add total " + images.size());
        ids.add(id);
      }
    }
    int[] idsInt = new int[ids.size()];
    for (int i = 0; i < idsInt.length; i++) {
      idsInt[i] = ids.get(i).intValue();
    }
    MatOfInt labels = new MatOfInt(idsInt);

    faceRecognizer.train(images, labels);
    faceRecognizer.save(saveFolder + "/face_model.yml");

  }

}
