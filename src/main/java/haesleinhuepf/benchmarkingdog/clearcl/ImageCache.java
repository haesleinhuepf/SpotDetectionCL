package haesleinhuepf.benchmarkingdog.clearcl;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.KernelAccessType;

public class ImageCache
{
  private ClearCLContext mContext;
  private ClearCLImage mClearCLImage;
  private int imageDimension = -1;

  public ImageCache(ClearCLContext pContext) {
    mContext = pContext;
  }

  public ClearCLImage get2DImage(HostAccessType pHostAccessType, KernelAccessType pKernelAccessType, ImageChannelOrder pImageChannelOrder, ImageChannelDataType pImageChannelDataType, long pWidth, long pHeight) {
    if (mClearCLImage == null ||
        mClearCLImage.getHostAccessType() != pHostAccessType ||
        mClearCLImage.getKernelAccessType() != pKernelAccessType ||
        mClearCLImage.getChannelOrder() != pImageChannelOrder ||
        mClearCLImage.getChannelDataType() != pImageChannelDataType ||
        mClearCLImage.getWidth() != pWidth ||
        mClearCLImage.getHeight() != pHeight ||
         imageDimension != 2) {

      imageDimension = 2;

      mClearCLImage = mContext.createImage(pHostAccessType,
                           pKernelAccessType,
                           pImageChannelOrder,
                           pImageChannelDataType,
                           pWidth,
                           pHeight);
    }
    return mClearCLImage;
  }


  public ClearCLImage get3DImage(HostAccessType pHostAccessType, KernelAccessType pKernelAccessType, ImageChannelOrder pImageChannelOrder, ImageChannelDataType pImageChannelDataType, long pWidth, long pHeight, long pDepth) {
    if (mClearCLImage == null ||
        mClearCLImage.getHostAccessType() != pHostAccessType ||
        mClearCLImage.getKernelAccessType() != pKernelAccessType ||
        mClearCLImage.getChannelOrder() != pImageChannelOrder ||
        mClearCLImage.getChannelDataType() != pImageChannelDataType ||
        mClearCLImage.getWidth() != pWidth ||
        mClearCLImage.getHeight() != pHeight ||
        mClearCLImage.getDepth() != pDepth ||
        imageDimension != 3
        ) {

      imageDimension = 3;

      mClearCLImage = mContext.createImage(pHostAccessType,
                                           pKernelAccessType,
                                           pImageChannelOrder,
                                           pImageChannelDataType,
                                           pWidth,
                                           pHeight,
                                           pDepth);
    }
    return mClearCLImage;
  }

  public void invalidate() {
    mClearCLImage = null;
  }
}
