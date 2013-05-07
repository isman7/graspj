	int box_size = 2*box_radius + 1;
	float box_area = box_size*box_size; // calculate?
	//float box_area = pown(box_size,2); // calculate?
	ushort box_edge[96];

	float spot_cache[25][25];
	
	int spot_nr = get_global_id(0);
	int parameters_per_candidate=3;
	int cand_index = spot_nr*parameters_per_candidate;
	ushort cand_x, cand_y;
	cand_x = candidates[cand_index+0];
	cand_y = candidates[cand_index+1];
	ushort frame_nr = candidates[cand_index+2];
	
	//#define img_index(row,column) (frame_height * frame_width * frame_nr) + ((column) * frame_height) + (row)
	#define img_index(row,column) (frame_height * frame_width * frame_nr) + ((column) * frame_width) + (row)
	
	
	int edge_count = 0;
	float x0 = 0.0f;
	float y0 = 0.0f;
	float tB0 = 0;
	float tI0 = 0;
	ushort x, y, lx, ly;

	float half_pixel_size = native_divide(pixel_size,2);