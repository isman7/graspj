	private float fisher_matrix[5][5] = { 0.0f }; // first index is rows, second index is columns
	private float inverse_fisher_matrix[5][5];
	
	int var;
	float du_dt[5];
	float d2u_dt2[5];
	float mu,u;
	
	t[4] = sigma;
	
	
	
	
	float x_tx_p12, x_tx_m12, y_ty_p12, y_ty_m12, exp_x_plus, exp_x_minus, exp_y_plus, exp_y_minus, dE_x, dE_y;
	float4 tempCRLB;
	
	int i;
	for (i=0; i<iterations; i++)
	{
		float sum_correction_term[5][2] =  { 0.0f };
		// for (var=0; var<4; var++)
		// {
			// sum_correction_term[var][0] = 0.0f;
			// sum_correction_term[var][1] = 0.0f;
		// }
		
		float sigma = t[4];
		printf("sigma: %f in iteration: %d\n",sigma,i);
		float s2 = pown(sigma,2);
		float s3 = pown(sigma,3);
		float s5 = pown(sigma,5);
		float two_s2 = 2*s2;
		float s_sqrt2 = sigma*M_SQRT2_F;
		
		for (x = cand_x - box_radius; x <= cand_x + box_radius; x++)
		{
			lx = x - cand_x + box_radius;
			for (y = cand_y - box_radius; y <= cand_y + box_radius; y++)
			{
				ly = y - cand_y + box_radius;
				// u = spot_cache[x][y];
				// u = native_divide(convert_float(image_buffer[index(x,y,chunk_frame_nr,frame_height,frame_width)]),10);
				// u = 0.42f * convert_float(image_buffer[index(x,y,chunk_frame_nr,frame_height,frame_width)]);
				u = spot_cache[lx][ly];
				x_tx_p12 = f_i_ti_p12(x,t[0]);
				x_tx_m12 = f_i_ti_m12(x,t[0]);
				
				y_ty_p12 = f_i_ti_p12(y,t[1]);
				y_ty_m12 = f_i_ti_m12(y,t[1]);
				
				exp_x_plus = f_exp_i_pm(two_s2,x_tx_p12);
				exp_x_minus = f_exp_i_pm(two_s2,x_tx_m12);
				
				exp_y_plus = f_exp_i_pm(two_s2,y_ty_p12);
				exp_y_minus = f_exp_i_pm(two_s2,y_ty_m12);
				
				
				dE_x = f_dE_i(x_tx_p12,x_tx_m12,s_sqrt2);
				dE_y = f_dE_i(y_ty_p12,y_ty_m12,s_sqrt2);
				
				//bad = bad + globid;
				//bad = x_tx_p12;
				
				du_dt[0] = f_du_dtx(t[2],sigma,exp_x_plus,exp_x_minus,dE_y);
				du_dt[1] = f_du_dtx(t[2],sigma,exp_y_plus,exp_y_minus,dE_x);
				du_dt[2] = f_du_dtI(dE_x,dE_y);
				du_dt[3] = 1;
				du_dt[4] = 	f_du_dts(t[2],s2,
							exp_x_plus, exp_x_minus, x_tx_p12, x_tx_m12, dE_x,
							exp_y_plus, exp_y_minus, y_ty_p12, y_ty_m12, dE_y);
				
				
				
				d2u_dt2[0] = f_d2u_dtx2(t[2],sigma,exp_x_plus,exp_x_minus,dE_y,x_tx_p12,x_tx_m12);
				d2u_dt2[1] = f_d2u_dtx2(t[2],sigma,exp_y_plus,exp_y_minus,dE_x,y_ty_p12,y_ty_m12);
				d2u_dt2[2] = 0;
				d2u_dt2[3] = 0;
				d2u_dt2[4] = 	f_d2u_dts2(t[2],s2,s3,s5,
								exp_x_plus, exp_x_minus, x_tx_p12, x_tx_m12, dE_x,
								exp_y_plus, exp_y_minus, y_ty_p12, y_ty_m12, dE_y);
				
				
				// mu(float tx, float ty, float x, float y, float tI, float tB, float sigma)
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
	
	int vars_per_dimension = 2;
	int result_index = spot_nr * fit_dimension * vars_per_dimension;

	inverse_5x5_matrix(fisher_matrix,inverse_fisher_matrix);


    spots[result_index+0] = t[0]*pixel_size + half_pixel_size;
    spots[result_index+1] = t[1]*pixel_size + half_pixel_size;
	spots[result_index+2] = t[4];
	spots[result_index+3] = t[2];
	spots[result_index+4] = t[3];
	spots[result_index+5] = native_sqrt(inverse_fisher_matrix[0][0])*pixel_size;
	spots[result_index+6] = native_sqrt(inverse_fisher_matrix[1][1])*pixel_size;
	spots[result_index+7] = native_sqrt(inverse_fisher_matrix[4][4]);
	spots[result_index+8] = native_sqrt(inverse_fisher_matrix[2][2]);
	spots[result_index+9] = native_sqrt(inverse_fisher_matrix[3][3]);