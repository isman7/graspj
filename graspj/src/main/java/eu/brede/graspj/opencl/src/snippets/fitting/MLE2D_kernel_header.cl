kernel void fit_spots	(
							private float pixel_size,
							private float sigma,
							private float count_conversion,
							private int count_offset,
							private int fit_dimension,
							private int iterations,
							private int box_radius,
							private int frame_width,
							private int frame_height,
							read_only global ushort* image_buffer,
							global ushort* candidates,
							global float* spots
						)