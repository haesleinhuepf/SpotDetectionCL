package haesleinhuepf.benchmarkingdog;

import ij.IJ;
import ij.ImagePlus;
import net.imagej.ImageJ;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;

import java.util.Random;

public class Main
{
  public static void main(final String... args) throws Exception
  {
    // Run ImageJ
    final ImageJ ij = new ImageJ();
    ij.ui().showUI();

    // Create test data
    int size = 256;

    ImagePlus imp = IJ.openImage("C:/structure/data/HisYFP_SPIM_Fused.tif");
    imp.show();

    Img<FloatType> img = ImageJFunctions.convertFloat(imp);

    Cursor<FloatType> cursor = img.cursor();
    Random random = new Random();
    while (cursor.hasNext())
    {
      cursor.next().set(random.nextFloat() * 65536);
    }

    Object[]
        imglibParameters =
        new Object[] { "currentData",
                       img,
                       "sigma1",
                       8,
                       "sigma2",
                       12 };


    ij.ui().show(img);

    ij.command()
      .run(DoGLookupWeightsClearCL.class, true, imglibParameters);

    //
  }
}
