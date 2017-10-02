
__kernel void convolve_image_3d(
        __read_only image3d_t input,
        __read_only image3d_t filterkernel,
        __write_only image3d_t output,
        __private int radius
)
{
    const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

    int4 pos = {get_global_id(0), get_global_id(1), get_global_id(2), 0};

    float sum = 0.0f;

    for(int x = -radius; x < radius + 1; x++)
    {
        for(int y = -radius; y < radius + 1; y++)
        {
            for(int z = -radius; z < radius + 1; z++)
            {
                const int4 kernelPos = {x+radius, y+radius,  z+radius, 0};
                sum += read_imagef(filterkernel, sampler, kernelPos).x
                     * read_imagef(input, sampler, pos + (int4)( x, y, z, 0 )).x;
            }
        }
    }

    float4 pix = {sum,0,0,0};
	write_imagef(output, pos, pix);
}


__kernel void subtract_convolved_images_3d(
        __read_only image3d_t input,
        __read_only image3d_t filterkernel_minuend,
        __read_only image3d_t filterkernel_subtrahend,
        __write_only image3d_t output,
        __private int radius
)
{
    const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

    int4 pos = {get_global_id(0), get_global_id(1), get_global_id(2), 0};

    float sum_minuend = 0.0f;
    float sum_subtrahend = 0.0f;

    for(int x = -radius; x < radius + 1; x++)
    {
        for(int y = -radius; y < radius + 1; y++)
        {
            for(int z = -radius; z < radius + 1; z++)
            {
                const int4 kernelPos = {x+radius, y+radius,  z+radius, 0};

                float image_pixel_value = read_imagef(input, sampler, pos + (int4)( x, y, z, 0 )).x;

                sum_minuend += read_imagef(filterkernel_minuend, sampler, kernelPos).x
                     * image_pixel_value;
                sum_subtrahend += read_imagef(filterkernel_subtrahend, sampler, kernelPos).x
                     * image_pixel_value;
            }
        }
    }

    float4 pix = {sum_minuend - sum_subtrahend,0,0,0};
	write_imagef(output, pos, pix);
}
