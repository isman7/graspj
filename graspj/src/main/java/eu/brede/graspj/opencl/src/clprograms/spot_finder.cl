#pragma OPENCL EXTENSION cl_amd_printf : enable

kernel void find_spots	(
								const int threshold,
								const int max_spots_per_frame,
								const int box_radius,
								const int frame_width,
								const int frame_height,
								read_only global ushort* image_buffer,
								global char* mask,
								write_only global ushort* candidates, // saving coords and framenr in one in order to minimize read commands
								//global ushort* cands_frame_nr, // better make uint? otherwise only 2^16 frames per package max!
								global short* counts
							)
							
{
	int max_box_overlap = 2;
	int inner_box_radius = 2*box_radius-max_box_overlap;
	int number_of_candidates = 0;
	int frame_nr = get_global_id(0);

	//#define img_index(row,column) (frame_height * frame_width * frame_nr) + ((column) * frame_height) + (row)
	#define img_index(row,column) (frame_height * frame_width * frame_nr) + ((column) * frame_width) + (row)
	
	int parameters_per_spot = 3; // TODO read from config
	int frame_spot_offset = frame_nr * max_spots_per_frame * parameters_per_spot;
	
	#define cand_index(candidate_nr) frame_spot_offset + (candidate_nr * parameters_per_spot)
	int i,j;
	for (i=box_radius; i < frame_width - box_radius; i++)
	{
		for (j=box_radius; j < frame_height - box_radius; j++)
		{
			ushort intensity = image_buffer[img_index(i,j)];
			
			if(mask[img_index(i,j)] > 0) {continue;}
			
			if(intensity > threshold)
			{
				if (intensity > (image_buffer[img_index(i+1,j)]))
					if (intensity > (image_buffer[img_index(i-1,j+1)]))
						if (intensity > (image_buffer[img_index(i,j+1)]))
							if (intensity > (image_buffer[img_index(i+1,j+1)]))
								if (intensity >= (image_buffer[img_index(i-1,j-1)]))
									if (intensity >= (image_buffer[img_index(i,j-1)]))
										if (intensity >= (image_buffer[img_index(i+1,j-1)]))
											if (intensity >= (image_buffer[img_index(i-1,j)]))
											{
												if (number_of_candidates < max_spots_per_frame)
												{
															
													//int this_cand_index = cand_index(number_of_candidates);
													int this_cand_index = frame_nr * max_spots_per_frame * parameters_per_spot + number_of_candidates*parameters_per_spot;
													candidates[this_cand_index+0] = i;
													candidates[this_cand_index+1] = j;
													candidates[this_cand_index+2] = frame_nr;
													
													int k,l;
													for (k=i-inner_box_radius; k < i + inner_box_radius; k++)
													{
														
														if(k<0) {continue;}
														if(k>=frame_height) {continue;}
														
														for (l=j-inner_box_radius; l < j + inner_box_radius; l++)
														{
															if(l<0) {continue;}
															if(l>=frame_width) {continue;}
																	
															mask[img_index(k,l)] = 1;
														}
													}
													
													number_of_candidates++;
													//printf("x=%d, y=%d, f=%d, i=%d\n",i,j,frame_nr,intensity);
												}
											}
			} // end for intensity if
		} // end for col (j)
	} // end for row (i)
	
	counts[frame_nr] = number_of_candidates;

} // end kernel