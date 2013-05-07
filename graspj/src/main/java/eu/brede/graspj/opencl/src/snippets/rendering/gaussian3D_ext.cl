		float z = z_nm/z_pixel_size;
		float center_z = ((spots[spot_index+2])/z_pixel_size);
		
		float sz = spots[spot_index+fit_dimension+2];
		float sz_px=(sz/z_pixel_size);
		float sz_sqrt2 = sz_px*M_SQRT2_F;
		
		z_tz_p12 = f_i_ti_p12(z,center_z);
		z_tz_m12 = f_i_ti_m12(z,center_z);
		
		dE_x = f_dE_i(x_tx_p12,x_tx_m12,sx_sqrt2);