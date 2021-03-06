	int worker_nr = get_global_id(0);
	//if(worker_nr==0) printf("starts with 0\n");
	
	int vars_per_dimension = 2;
	
	int first_spot_nr = worker_nr*spots_per_worker;
	int spot_nr;
	for(spot_nr=first_spot_nr;spot_nr<first_spot_nr+spots_per_worker;spot_nr++)
	{
		int spot_index = spot_nr * fit_dimension * vars_per_dimension;
		
		if(apply_mask == 1)
		{
			if(specific_channel == 0)
			{
				if(mask[spot_nr]==0)
				{
					continue;
				}
			}
			else
			{
				if(mask[spot_nr]!=channel)
				{
					continue;
				}
			}
		}
		
		float spot_x = spots[spot_index+0];
		float spot_y = spots[spot_index+1];
		
		if(apply_drift == 1)
		{
			int frame_nr = frame_nrs[spot_nr];
			spot_x = spot_x + drift[frame_nr].x;
			spot_y = spot_y + drift[frame_nr].y;
		}
	
		float sx = spots[spot_index+fit_dimension+0];
		float sy = spots[spot_index+fit_dimension+1];

		// hard coded limits to avoid crash
		if(isnan(sx) || isnan(sy))
			continue;
		if((sx>100.0f) || (sy>100.0f))
			continue;
		if((sx<0.1f) || (sy<0.1f))
		{
			//continue;
			sx = 1.0f;
			sy = 1.0f;
		}
		
		// end hard coded limits
		
		// TODO use native divs?
		float sx_px=(sx/pixel_size);
		float sy_px=(sy/pixel_size);
		int box_radius = ceil(3*fmax(sx_px,sy_px));
		float sx_sqrt2 = sx_px*M_SQRT2_F;
		float sy_sqrt2 = sy_px*M_SQRT2_F;
		
		//float center_x = ((spots[spot_index+0]+offset_x)/pixel_size); //native?
		//float center_y = ((spots[spot_index+1]+offset_y)/pixel_size);
		
		float center_x = ((spot_x+offset_x)/pixel_size);
		float center_y = ((spot_y+offset_y)/pixel_size);
		
		int start_x = fmax(floor(center_x-box_radius),0);
		int end_x = fmin(ceil(center_x+box_radius),render_width-1);
		int start_y = fmax(floor(center_y-box_radius),0);
		int end_y = fmin(ceil(center_y+box_radius),render_height-1);
		int x,y;
		
		float x_tx_p12, x_tx_m12, y_ty_p12, y_ty_m12, dE_x, dE_y;
		float mu;
		
		for (y=start_y; y<end_y; y++)
		{
			int image_index_offset = y*render_width;
			for (x=start_x; x<end_x; x++)
			{
				int image_index = x+image_index_offset;
				
				// hard coded crash prevention
				
				//if(image_index<0)
				if(image_index<1)
					continue;
				//if(image_index>render_width*render_height-1)
				if(image_index>render_width*render_height-2)
					continue;
				
				// end
				
				x_tx_p12 = f_i_ti_p12(x,center_x);
				x_tx_m12 = f_i_ti_m12(x,center_x);
				
				y_ty_p12 = f_i_ti_p12(y,center_y);
				y_ty_m12 = f_i_ti_m12(y,center_y);
				
				dE_x = f_dE_i(x_tx_p12,x_tx_m12,sx_sqrt2);
				dE_y = f_dE_i(y_ty_p12,y_ty_m12,sy_sqrt2);
				
				mu = (f_mu(intensity,0,dE_x,dE_y));
				//mu = intensity;
				
				/*${update_image}*/
			}
		}
	}