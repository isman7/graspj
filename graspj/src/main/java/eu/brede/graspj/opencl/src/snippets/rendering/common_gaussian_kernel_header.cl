__kernel void render_spots	(
									int render_width,
									int render_height,
									float offset_x,
									float offset_y,
									float pixel_size,
									float intensity, // e.g. 100
									int fit_dimension,
									int complex, // no boolean available in OpenCL
									// future color coded 3d min/max z for hue
									int apply_mask,
									int specific_channel,
									int channel,
									global char* mask,
									
									int apply_drift,
									global float2* drift,
									global int* frame_nrs,
									
									int spots_per_worker,
									global float* spots,									
									global float* image_buffer
									// global uchar* hue
									/*${additional_kernel_arguments}*/
								)