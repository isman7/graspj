	int var;
	float du_dt[5];
	float d2u_dt2[5];

	float dsx_dtz, d2sx_dtz2, dsy_dtz, d2sy_dtz2, du_dsx, d2u_dsx2, du_dsy, d2u_dsy2;
	float Gx_21, Gx_53, Gx_31, Gy_21, Gy_53, Gy_31;

	float mu,u;
	

	float x_tx_p12, x_tx_m12, y_ty_p12, y_ty_m12, exp_x_plus, exp_x_minus, exp_y_plus, exp_y_minus, dE_x, dE_y;
	float z_gx, z_gx_2, z_gx_3, z_gx_4, z_gy, z_gy_2, z_gy_3, z_gy_4, sigma_x_n, sigma_y_n;
	
	float sx, sy, two_sx2, two_sy2;
	


	float fisher_matrix[5][5] = { 0.0f }; // first index is rows, second index is columns
	float inverse_fisher_matrix[5][5];

	int i;
	for (i=0; i<iterations; i++)
	{
		float sum_correction_term[5][2] =  { 0.0f };
		
		for (x = candidates_coordinates[candidate].x - box_radius; x <= candidates_coordinates[candidate].x + box_radius; x++)
		{
			lx = x - candidates_coordinates[candidate].x + box_radius;
			
			for (y = candidates_coordinates[candidate].y - box_radius; y <= candidates_coordinates[candidate].y + box_radius; y++)
			{
				ly = y - candidates_coordinates[candidate].y + box_radius;
				
				u = spot_cache[lx][ly];
				// u = native_divide(convert_float(image_buffer[index(x,y,chunk_frame_nr,frame_height,frame_width)]),10);
				// u = 0.42f * convert_float(image_buffer[index(x,y,chunk_frame_nr,frame_height,frame_width)]);
				// u = count_conversion * convert_float(image_buffer[index(x,y,chunk_frame_nr,frame_height,frame_width)]);
				
				x_tx_p12 = f_i_ti_p12(x,t[0]);
				x_tx_m12 = f_i_ti_m12(x,t[0]);
				
				y_ty_p12 = f_i_ti_p12(y,t[1]);
				y_ty_m12 = f_i_ti_m12(y,t[1]);
				
				z_gx = t[4]-gx;
				z_gx_2 = pow(z_gx,2);
				z_gx_3 = pow(z_gx,3);
				z_gx_4 = pow(z_gx,4);
				
				z_gy = t[4]-gy;
				z_gy_2 = pow(z_gy,2);
				z_gy_3 = pow(z_gy,3);
				z_gy_4 = pow(z_gy,4);
				

				sigma_x_n = f_sigma_i_n(z_gx_2, z_gx_3, z_gx_4, Ax, Bx, rdx2);
				sx = f_sigma_i(sx0, sigma_x_n);
				two_sx2 = 2*pown(sx,2);
				
				exp_x_plus = f_exp_i_pm(two_sx2,x_tx_p12);
				exp_x_minus = f_exp_i_pm(two_sx2,x_tx_m12);
				
				sigma_y_n = f_sigma_i_n(z_gy_2, z_gy_3, z_gy_4, Ay, By, rdy2);
				sy = f_sigma_i(sy0, sigma_y_n);
				two_sy2 = 2*pown(sy,2);
				
				exp_y_plus = f_exp_i_pm(two_sy2,y_ty_p12);
				exp_y_minus = f_exp_i_pm(two_sy2,y_ty_m12);
				
				
				dE_x = f_dE_i(x_tx_p12,x_tx_m12,two_sx2);
				dE_y = f_dE_i(y_ty_p12,y_ty_m12,two_sy2);
				
				
				Gx_21 = f_Gx_nm(2, 1, sx, exp_x_plus, exp_x_minus, x_tx_p12, x_tx_m12);
				Gx_53 = f_Gx_nm(5, 3, sx, exp_x_plus, exp_x_minus, x_tx_p12, x_tx_m12);
				Gx_31 = f_Gx_nm(3, 1, sx, exp_x_plus, exp_x_minus, x_tx_p12, x_tx_m12);
				
				Gy_21 = f_Gy_nm(2, 1, sy, exp_y_plus, exp_y_minus, y_ty_p12, y_ty_m12);
				Gy_53 = f_Gy_nm(5, 3, sy, exp_y_plus, exp_y_minus, y_ty_p12, y_ty_m12);
				Gy_31 = f_Gy_nm(3, 1, sy, exp_y_plus, exp_y_minus, y_ty_p12, y_ty_m12);
				
				du_dsx =  f_du_dsx(t[2], dE_y, Gx_21);
				du_dsy =  f_du_dsy(t[2], dE_x, Gy_21);
				
				dsx_dtz = f_dsi_dtz(z_gx, z_gx_2, z_gx_3, Ax, Bx, rdx2, sx0, sigma_x_n);
				dsy_dtz = f_dsi_dtz(z_gy, z_gy_2, z_gy_3, Ay, By, rdy2, sy0, sigma_y_n);
				
				d2u_dsx2 = f_d2u_dsx2(t[2], dE_y, Gx_53, Gx_31);
				d2u_dsy2 = f_d2u_dsy2(t[2], dE_x, Gy_53, Gy_31);
				
				d2sx_dtz2 = f_d2si_dtz2 (z_gx, z_gx_2, z_gx_3, Ax, Bx, rdx2, sx0, sigma_x_n);
				d2sy_dtz2 = f_d2si_dtz2 (z_gy, z_gy_2, z_gy_3, Ay, By, rdy2, sy0, sigma_y_n);
				
				du_dt[0] = f_du_dtx(t[2],sx,exp_x_plus,exp_x_minus,dE_y);
				du_dt[1] = f_du_dtx(t[2],sy,exp_y_plus,exp_y_minus,dE_x);
				du_dt[2] = f_du_dtI(dE_x,dE_y);
				du_dt[3] = 1;
				du_dt[4] = f_du_dtz(du_dsx, dsx_dtz, du_dsy, dsy_dtz);
				
				
				
				d2u_dt2[0] = f_d2u_dtx2(t[2],sx,exp_x_plus,exp_x_minus,dE_y,x_tx_p12,x_tx_m12);
				d2u_dt2[1] = f_d2u_dtx2(t[2],sy,exp_y_plus,exp_y_minus,dE_x,y_ty_p12,y_ty_m12);
				d2u_dt2[2] = 0;
				d2u_dt2[3] = 0;
				d2u_dt2[4] = f_d2u_dtz2(du_dsx, d2u_dsx2, dsx_dtz, d2sx_dtz2, du_dsy, d2u_dsy2, dsy_dtz, d2sy_dtz2);
				
				
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
				for (var=0; var<5; var++)
				{
						sum_correction_term[var][0] = sum_correction_term[var][0] + correction_term1(t[var], du_dt[var], u, mu);
						sum_correction_term[var][1] = sum_correction_term[var][1] + correction_term2(t[var], du_dt[var], d2u_dt2[var], u, mu);
				}
			}
		}
		
		//update parameter values
		for (var=0; var<5; var++)
		{
			t[var] = t[var] - native_divide(sum_correction_term[var][0] , sum_correction_term[var][1]);
		}
	}
		

		
		// int result_index = spot_index(number_of_spots, global_frame_nr, max_spots_per_frame);
		//int result_index = spot_index(number_of_spots, chunk_frame_nr, max_spots_per_frame);
		int result_index = spot_index(number_of_spots, chunk_frame_nr, max_spots_per_frame) * fitDimension;
		
		// calculate the CRLBs for all parameters using (the inverse of the fisher matrix	
		
		
		inverse_5x5_matrix(fisher_matrix,inverse_fisher_matrix);
		

		CRLBs[result_index+0] = native_sqrt(inverse_fisher_matrix[0][0])*pixel_size;
		CRLBs[result_index+1] = native_sqrt(inverse_fisher_matrix[1][1])*pixel_size;
		CRLBs[result_index+2] = native_sqrt(inverse_fisher_matrix[4][4])*pixel_size;
		CRLBs[result_index+3] = native_sqrt(inverse_fisher_matrix[2][2]);
		CRLBs[result_index+4] = native_sqrt(inverse_fisher_matrix[3][3]);

		
		spots[result_index+0] = t[0]*pixel_size;
		spots[result_index+1] = t[1]*pixel_size;
		spots[result_index+2] = t[4]*pixel_size;
		spots[result_index+3] = t[2];
		spots[result_index+4] = t[3];