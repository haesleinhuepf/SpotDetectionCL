package haesleinhuepf.benchmarkingdog.clearcl;

import clearcl.*;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.KernelAccessType;
import clearcl.exceptions.OpenCLException;
import clearcl.ops.OpsBase;
import edu.mines.jtk.util.Stopwatch;
import haesleinhuepf.benchmarkingdog.StopWatch;

import java.io.IOException;
import java.util.Arrays;

public class ClearCLLookupWeightsDifferenceOfGaussian extends OpsBase
{
  ImageCache mMinuendFilterKernelImageCache;
  ImageCache mSubtrahendFilterKernelImageCache;
  ImageCache mOutputImageCache;

  ClearCLContext mContext;
  private ClearCLKernel mSubtractionConvolvedKernelImage2F;

  public ClearCLLookupWeightsDifferenceOfGaussian(ClearCLQueue pClearCLQueue) throws
                                                                 IOException
  {
    super(pClearCLQueue);
    mContext = getContext();
    mMinuendFilterKernelImageCache = new ImageCache(mContext);
    mSubtrahendFilterKernelImageCache = new ImageCache(mContext);
    mOutputImageCache = new ImageCache(mContext);

    ClearCLProgram
        lConvolutionProgram =
        getContext().createProgram(ClearCLLookupWeightsDifferenceOfGaussian.class,
                                   "convolution.cl");

    lConvolutionProgram.addBuildOptionAllMathOpt();
    lConvolutionProgram.addDefine("FLOAT");
    lConvolutionProgram.buildAndLog();

    mSubtractionConvolvedKernelImage2F =
        lConvolutionProgram.createKernel(
            "subtract_convolved_images_3d");
  }

  public ClearCLImage differenceOfGaussian(ClearCLImage pInputImage,
                                           float pMinuendSigma,
                                           float pSubtrahendSigma)
  {
    int
        lRadius =
        (int) Math.ceil(3.0f * Math.max(pMinuendSigma,
                                        pSubtrahendSigma));
    ClearCLImage
        lMinuendFilterKernelImage =
        ClearCLGaussUtilities.createBlur2DFilterKernelImage(
            mMinuendFilterKernelImageCache,
            pMinuendSigma,
            lRadius);
    ClearCLImage
        lSubtrahendFilterKernelImage =
        ClearCLGaussUtilities.createBlur2DFilterKernelImage(
            mSubtrahendFilterKernelImageCache,
            pSubtrahendSigma,
            lRadius);

    ClearCLImage
        output =
        mOutputImageCache.get2DImage(HostAccessType.ReadWrite,
                                     KernelAccessType.ReadWrite,
                                     ImageChannelOrder.Intensity,
                                     ImageChannelDataType.Float,
                                     pInputImage.getWidth(),
                                     pInputImage.getHeight());

    long[] imageDimensions = pInputImage.getDimensions();
//    long[] workgroupDimensions = new long[imageDimensions.length];
//    for (int i = 0; i < imageDimensions.length; i++) {
//      workgroupDimensions[i] = 4;
//    }

    System.out.println("gl: " + Arrays.toString(imageDimensions));
    //System.out.println("ws: " + Arrays.toString(workgroupDimensions));

    mSubtractionConvolvedKernelImage2F.setArgument("input",
                                                   pInputImage);
    mSubtractionConvolvedKernelImage2F.setArgument(
        "filterkernel_minuend",
        lMinuendFilterKernelImage);
    mSubtractionConvolvedKernelImage2F.setArgument(
        "filterkernel_subtrahend",
        lSubtrahendFilterKernelImage);
    mSubtractionConvolvedKernelImage2F.setArgument("output", output);
    mSubtractionConvolvedKernelImage2F.setArgument("radius", lRadius);
    mSubtractionConvolvedKernelImage2F.setGlobalSizes(imageDimensions);
    //mSubtractionConvolvedKernelImage2F.setLocalSizes(workgroupDimensions);

    long[] sizes = new long[pInputImage.getDimensions().length];
    long[] originalSizes = new long[pInputImage.getDimensions().length];
    long[] offsets = new long[pInputImage.getDimensions().length];
    for (int i = 0; i < sizes.length; i++) {
      originalSizes[i] = pInputImage.getDimensions()[i];
      offsets[i] = 0;
    }

    long originalSize0 = sizes[0];
    long originalSize1 = sizes[0];
    long numberOfSplitsX = sizes[0] * sizes[1] * sizes[2] / 256 / 256/ 256;

    long numberOfSplitsY = 1;

    while (numberOfSplitsX > numberOfSplitsY) {
      numberOfSplitsX = numberOfSplitsX / 2;
      numberOfSplitsY = numberOfSplitsY * 2;
    }

    numberOfSplitsX = numberOfSplitsX * 2;

    System.out.println(numberOfSplitsX);
    System.out.println(numberOfSplitsY);

    boolean cancelling = false;

    long blocksize[] = {16, 16, 16};


    while (true) {
      for (int i = 0; i < 3; i++) {
        sizes[i] = blocksize[i];
        if (sizes[i] + offsets[i] > originalSizes[i]) {
          sizes[i] = originalSizes[i] - offsets[i];
        }
      }


      System.out.println("s offset: " + Arrays.toString(offsets));
      System.out.println("s sizes: " + Arrays.toString(sizes));

      mSubtractionConvolvedKernelImage2F.setGlobalSizes(sizes);
      mSubtractionConvolvedKernelImage2F.setGlobalOffsets(offsets);
      StopWatch watch = new StopWatch();
      try
      {
        watch.start();
        mSubtractionConvolvedKernelImage2F.run();
      }
      catch (RuntimeException e)
      {
        e.printStackTrace();
        watch.stop("Excptn came after ");
        cancelling = true;
        break;
      }

      offsets[0] += blocksize[0];
      if (offsets[0] > originalSizes[0]) {
        offsets[0] = 0;
        offsets[1] += blocksize[1];
        if (offsets[1] > originalSizes[1]) {
          offsets[1] = 0;

          offsets[2] += blocksize[2];
          if (offsets[2] > originalSizes[2]) {
            break;
          }


        }

      }

    }
    if (true) return null;



    for (int j = 0; j < numberOfSplitsX; j++) {
      if (j < numberOfSplitsX - 1) {
        sizes[0] = originalSize0 / numberOfSplitsX;
      } else {
        sizes[0] = originalSize0 - offsets[0];
      }

      for (int k = 0; k < numberOfSplitsY; k++)
      {
        if (k < numberOfSplitsY - 1)
        {
          sizes[1] = originalSize1 / numberOfSplitsY;
        }
        else
        {
          sizes[1] = originalSize1 - offsets[1];
        }

        System.out.println("s offset: " + Arrays.toString(offsets));
        System.out.println("s sizes: " + Arrays.toString(sizes));

        offsets[1] += sizes[1];
      }
      offsets[1] = 0;
      offsets[0] += sizes[0];
      if (cancelling) {
        break;
      }
    }
    return output;
  }
}
