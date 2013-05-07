	
	
	for (x = cand_x - box_radius; x <= cand_x + box_radius; x++)
	{
		lx = x - cand_x + box_radius;
		
		for (y = cand_y - box_radius; y <= cand_y + box_radius; y++)
		{
			ly = y - cand_y + box_radius;
				 
			spot_cache[lx][ly] = count_conversion * convert_float(image_buffer[img_index(x,y)] - count_offset);
			
			if(	(x==cand_x - box_radius) ||
				(x==cand_x + box_radius) ||
				(y==cand_y - box_radius) ||
				(y==cand_y + box_radius) )
			{
				box_edge[edge_count] = spot_cache[lx][ly];
				edge_count++;				
			}
			
			
			
			tI0 = tI0 + spot_cache[lx][ly];
			x0 = x0 + x*spot_cache[lx][ly];
			y0 = y0 + y*spot_cache[lx][ly];
			
		}
	}
	
	
	
	x0 = native_divide(x0,tI0);
	//x0 = cand_x;
	y0 = native_divide(y0,tI0);
	//y0 = cand_y;
	
	/*int pxs = 160;
	printf("cand_x = %d\n",cand_x*pxs);
	printf("x0 = %f\n",x0*pxs);
	
	printf("cand_y = %d\n",cand_y*pxs);
	printf("y0 = %f\n",y0*pxs);*/
	
	tB0 = kth_smallest(box_edge,edge_count,edge_count/2);
	//tB0 = 70;
	tI0 = tI0 - box_area*tB0;
	
	int var_box_radius = 3;
	float mux = var_box_radius + 0.5f;
	float muy = mux; // eventually allow different box width & height! 
	
	float varx = 0;
	float vary = 0;
	float varxy = 0;
	float stdx = 0;
	float stdy = 0;
	
	float total_count = 0; 
	
	// experimental
	/*if(cand_x != rint(x0)) printf("old: %d, new: %d\n", cand_x, rint(x0));
	if(cand_y != rint(y0)) printf("old: %d, new: %d\n", cand_y, rint(y0));*/
	cand_x = rint(x0);
	cand_y = rint(y0);
	
	for (x = cand_x - var_box_radius; x <= cand_x + var_box_radius; x++)
	{
		lx = x - cand_x + box_radius;
		
		for (y = cand_y - var_box_radius; y <= cand_y + var_box_radius; y++)
		{
			ly = y - cand_y + box_radius;
			
			float bg_subbed_count = fmax(spot_cache[lx][ly]-tB0,0);
			
			varx = varx + bg_subbed_count*(x-x0)*(x-x0);
			//varx = varx + bg_subbed_count*pown(x-x0,2);
			vary = vary + bg_subbed_count*(y-y0)*(y-y0);
			//vary = vary + bg_subbed_count*pown(y-y0,2);
			
			stdx = stdx + bg_subbed_count*fabs(x-x0);
			stdy = stdy + bg_subbed_count*fabs(y-y0);
			
			
			varxy = varxy + bg_subbed_count*((x-x0)*(x-x0)+(y-y0)*(y-y0));
			//varxy = varxy + bg_subbed_count*(pown(x-x0,2)+pown(y-y0,2));
			total_count = total_count + bg_subbed_count;
		}
	}
	

	
	float t[5]; //fit variables (x,y,I,B,z), temp change: (x,y,I,B,sx,sy)
	t[0] = x0;
	t[1] = y0;
	t[2] = tI0; // * count_conversion;
	//t[3] = image_buffer[img_index(0,0)];; // DEBUG
	t[3] = tB0; // * count_conversion;
	//t[4] = 0.0f;
	//t[4] = native_sqrt(native_divide(varx,total_count));
	//t[5] = native_sqrt(native_divide(vary,total_count));
	//varxy = native_sqrt(native_divide(varxy,total_count));
	stdx = native_divide(stdx,total_count);
	stdy = native_divide(stdy,total_count);
	
	// temporary
	//stdx = fmax(fmin(1.2f,stdx),1.0f);
	//stdy = fmax(fmin(1.2f,stdy),1.0f);
		
	
	//t[4] = stdx;
	//t[5] = stdy;
	
	t[4] = 0.0f;
	
	//printf("sx = %f\n",t[4]);
	//printf("sy = %f\n",t[5]);
	
	// latest test:
	//printf("sx = %f\n",stdx);
	//printf("sy = %f\n",stdy);
	
	//printf("sxy = %f\n",varxy);
	