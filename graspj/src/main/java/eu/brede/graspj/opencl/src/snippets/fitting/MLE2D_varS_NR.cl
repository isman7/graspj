	int var;
	float du_dt[6];
	float d2u_dt2[6];

	float du_dsx, d2u_dsx2, du_dsy, d2u_dsy2;
	float Gx_21, Gx_53, Gx_31, Gy_21, Gy_53, Gy_31;

	float mu,u;
	

	float x_tx_p12, x_tx_m12, y_ty_p12, y_ty_m12, exp_x_plus, exp_x_minus, exp_y_plus, exp_y_minus, dE_x, dE_y;
	
	//t[4] = sigma;
	//t[5] = sigma;
	float sx, sy, two_sx2, two_sy2;
	float sx_sqrt2, sy_sqrt2;// = sigma*M_SQRT2_F;


	private float fisher_matrix[5][5] = { 0.0f }; // first index is rows, second index is columns
	private float inverse_fisher_matrix[5][5];
	
	//printf("box_radius: %d\n",box_radius);
	
	int i;
	for (i=0; i<iterations; i++)
	{
		// very experimental, bad correction
		t[4] = fmax(fmin(2.2f,t[4]),0.75f);
		t[5] = fmax(fmin(2.2f,t[5]),0.75f);
	
		float sum_correction_term[6][2] =  { 0.0f };
		
		for (x = cand_x - box_radius; x <= cand_x + box_radius; x++)
		{
			lx = x - cand_x + box_radius;
			for (y = cand_y - box_radius; y <= cand_y + box_radius; y++)
			{
				ly = y - cand_y + box_radius;
				
				u = spot_cache[lx][ly];
				// u = native_divide(convert_float(image_buffer[index(x,y,chunk_frame_nr,frame_height,frame_width)]),10);
				// u = 0.42f * convert_float(image_buffer[index(x,y,chunk_frame_nr,frame_height,frame_width)]);
				// u = count_conversion * convert_float(image_buffer[index(x,y,chunk_frame_nr,frame_height,frame_width)]);
				
				x_tx_p12 = f_i_ti_p12(x,t[0]);
				x_tx_m12 = f_i_ti_m12(x,t[0]);
				
				y_ty_p12 = f_i_ti_p12(y,t[1]);
				y_ty_m12 = f_i_ti_m12(y,t[1]);
				

				sx = t[4];
				two_sx2 = 2.0f*pown(sx,2);
				sx_sqrt2 = sx*M_SQRT2_F;
				
				exp_x_plus = f_exp_i_pm(two_sx2,x_tx_p12);
				exp_x_minus = f_exp_i_pm(two_sx2,x_tx_m12);
				
				sy = t[5];
				two_sy2 = 2.0f*pown(sy,2);
				sy_sqrt2 = sy*M_SQRT2_F;
				
				exp_y_plus = f_exp_i_pm(two_sy2,y_ty_p12);
				exp_y_minus = f_exp_i_pm(two_sy2,y_ty_m12);
				
				
				/*dE_x = f_dE_i(x_tx_p12,x_tx_m12,two_sx2);
				dE_y = f_dE_i(y_ty_p12,y_ty_m12,two_sy2*/
				
				dE_x = f_dE_i(x_tx_p12,x_tx_m12,sx_sqrt2);
				dE_y = f_dE_i(y_ty_p12,y_ty_m12,sy_sqrt2);
				
				Gx_21 = f_Gx_nm(2, 1, sx, exp_x_plus, exp_x_minus, x_tx_p12, x_tx_m12);
				Gx_53 = f_Gx_nm(5, 3, sx, exp_x_plus, exp_x_minus, x_tx_p12, x_tx_m12);
				Gx_31 = f_Gx_nm(3, 1, sx, exp_x_plus, exp_x_minus, x_tx_p12, x_tx_m12);
				
				Gy_21 = f_Gy_nm(2, 1, sy, exp_y_plus, exp_y_minus, y_ty_p12, y_ty_m12);
				Gy_53 = f_Gy_nm(5, 3, sy, exp_y_plus, exp_y_minus, y_ty_p12, y_ty_m12);
				Gy_31 = f_Gy_nm(3, 1, sy, exp_y_plus, exp_y_minus, y_ty_p12, y_ty_m12);
				
				du_dsx =  f_du_dsx(t[2], dE_y, Gx_21);
				du_dsy =  f_du_dsy(t[2], dE_x, Gy_21);
				
				d2u_dsx2 = f_d2u_dsx2(t[2], dE_y, Gx_53, Gx_31);
				d2u_dsy2 = f_d2u_dsy2(t[2], dE_x, Gy_53, Gy_31);
				
				du_dt[0] = f_du_dtx(t[2],sx,exp_x_plus,exp_x_minus,dE_y);
				du_dt[1] = f_du_dtx(t[2],sy,exp_y_plus,exp_y_minus,dE_x);
				du_dt[2] = f_du_dtI(dE_x,dE_y);
				du_dt[3] = 1;
				du_dt[4] = du_dsx;
				du_dt[5] = du_dsy;
				
				
				
				d2u_dt2[0] = f_d2u_dtx2(t[2],sx,exp_x_plus,exp_x_minus,dE_y,x_tx_p12,x_tx_m12);
				d2u_dt2[1] = f_d2u_dtx2(t[2],sy,exp_y_plus,exp_y_minus,dE_x,y_ty_p12,y_ty_m12);
				d2u_dt2[2] = 0;
				d2u_dt2[3] = 0;
				d2u_dt2[4] = d2u_dsx2;
				d2u_dt2[5] = d2u_dsy2;
				
				
				mu = f_mu(t[2],t[3],dE_x,dE_y);
				
				
				// if this is the last iteration calculate Fisher information matrix
				// calculation is done using the last iteration values, not the last corrected ones! (speed reasons)
				int m,n;
				if (i == iterations-1)
				{
					float reciproke_mu = native_recip(mu);
					
					for (m=0; m<5; m++)
						for (n=0; n<5; n++)
							fisher_matrix[m][n] += reciproke_mu*du_dt[m]*du_dt[n];
				}
				
				// calculate the correction terms for all parameters
				for (var=0; var<6; var++)
				{
						sum_correction_term[var][0] = sum_correction_term[var][0] + correction_term1(t[var], du_dt[var], u, mu);
						sum_correction_term[var][1] = sum_correction_term[var][1] + correction_term2(t[var], du_dt[var], d2u_dt2[var], u, mu);
				}
			}
		}
		
		//update parameter values
		for (var=0; var<6; var++)
		{
			t[var] = t[var] - native_divide(sum_correction_term[var][0] , sum_correction_term[var][1]);
		}
		
		
	}
		
	
		
		// int result_index = spot_index(number_of_spots, global_frame_nr, max_spots_per_frame);
		//int result_index = spot_index(number_of_spots, chunk_frame_nr, max_spots_per_frame);
	//int result_index = spot_index(number_of_spots, chunk_frame_nr, max_spots_per_frame) * fitDimension;
		
		// calculate the CRLBs for all parameters using (the inverse of the fisher matrix	
		
	int vars_per_dimension = 2;
	int result_index = spot_nr * fit_dimension * vars_per_dimension;
		
	// temporary
	// tests if result is within 1px of candidate
	/*if((fabs(t[0]-cand_x)>5.0f) || (fabs(t[1]-cand_y)>1.1f))
	{
		for(i=0;i<10;i++)
		{
			spots[result_index+i] = nan(0);
		}
		return;
	}*/
	 
	inverse_5x5_matrix(fisher_matrix,inverse_fisher_matrix);


    spots[result_index+0] = t[0]*pixel_size + half_pixel_size;
    spots[result_index+1] = t[1]*pixel_size + half_pixel_size;
	spots[result_index+2] = t[4];
	spots[result_index+3] = t[2];
	spots[result_index+4] = t[3];
	spots[result_index+5] = native_sqrt(inverse_fisher_matrix[0][0])*pixel_size;
	spots[result_index+6] = native_sqrt(inverse_fisher_matrix[1][1])*pixel_size;
	spots[result_index+7] = t[5];
	spots[result_index+8] = native_sqrt(inverse_fisher_matrix[2][2]);
	spots[result_index+9] = native_sqrt(inverse_fisher_matrix[3][3]);