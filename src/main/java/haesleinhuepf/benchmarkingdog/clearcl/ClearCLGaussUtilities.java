package haesleinhuepf.benchmarkingdog.clearcl;

import clearcl.ClearCLImage;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.KernelAccessType;

public class ClearCLGaussUtilities
{
  public static ClearCLImage createBlur2DFilterKernelImage(ImageCache pKernelImageCache, float sigma, int pRadius)
  {
    int lKernelDimension = pRadius * 2 + 1;
    ClearCLImage lKernelClearCLImage =
        pKernelImageCache.get2DImage(HostAccessType.WriteOnly,
                                     KernelAccessType.ReadOnly,
                                     ImageChannelOrder.Intensity,
                                     ImageChannelDataType.Float,
                                     lKernelDimension,
                                     lKernelDimension);

    float[]
        lFilterKernelArray =
        new float[lKernelDimension * lKernelDimension];
    float sum = 0.0f;
    for (int x = -pRadius; x < pRadius + 1; x++)
    {
      for (int y = -pRadius; y < pRadius + 1; y++)
      {
        int pos = x + pRadius + (y + pRadius) * lKernelDimension;
        float
            weight =
            (float) Math.exp(-((float) (x * x + y * y) / (2f
                                                          * sigma
                                                          * sigma)));
        lFilterKernelArray[pos] = weight;
        sum += weight;
      }
    }

    for (int i = 0; i < lFilterKernelArray.length; i++)
    {
      lFilterKernelArray[i] = lFilterKernelArray[i] / sum;
    }
    lKernelClearCLImage.readFrom(lFilterKernelArray, true);
    return lKernelClearCLImage;
  }


  public static ClearCLImage createBlur3DFilterKernelImage(ImageCache pKernelImageCache, float sigma, int pRadius)
  {
    int lKernelDimension = pRadius * 2 + 1;
    ClearCLImage lKernelClearCLImage =
        pKernelImageCache.get3DImage(HostAccessType.WriteOnly,
                                     KernelAccessType.ReadOnly,
                                     ImageChannelOrder.Intensity,
                                     ImageChannelDataType.Float,
                                     lKernelDimension,
                                     lKernelDimension,
                                     lKernelDimension);

    float[]
        lFilterKernelArray =
        new float[lKernelDimension * lKernelDimension * lKernelDimension];
    float sum = 0.0f;
    for (int x = -pRadius; x < pRadius + 1; x++)
    {
      for (int y = -pRadius; y < pRadius + 1; y++)
      {
        for (int z = -pRadius; z < pRadius + 1; z++)
        {
          int pos = x + pRadius + (y + pRadius) * lKernelDimension + (z + pRadius) * lKernelDimension * lKernelDimension;
          float
              weight =
              (float) Math.exp(-((float) (x * x + y * y + z * z) / (3f
                                                            * sigma
                                                                    * sigma
                                                                    * sigma)));
          lFilterKernelArray[pos] = weight;
          sum += weight;
        }
      }
    }

    for (int i = 0; i < lFilterKernelArray.length; i++)
    {
      lFilterKernelArray[i] = lFilterKernelArray[i] / sum;
    }
    lKernelClearCLImage.readFrom(lFilterKernelArray, true);
    return lKernelClearCLImage;
  }
}
