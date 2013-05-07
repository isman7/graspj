float f_sigma_i (float sigma0, float sigma_i_n)
{
	return sigma0*sigma_i_n;
}

float f_sigma_i_n (float z_gi_2, float z_gi_3, float z_gi_4, float Ai, float Bi, float rdi2)
{
	return native_sqrt(1+rdi2*(z_gi_2 + Ai*z_gi_3 + Bi*z_gi_4));
}

float f_du_dtz (float du_dsx, float dsx_dtz, float du_dsy, float dsy_dtz)
{
	return du_dsx*dsx_dtz + du_dsy*dsy_dtz;
}

float f_d2u_dtz2 (float du_dsx, float d2u_dsx2, float dsx_dtz, float d2sx_dtz2, float du_dsy, float d2u_dsy2, float dsy_dtz, float d2sy_dtz2)
{
	return d2u_dsx2*pown(dsx_dtz,2) + du_dsx*d2sx_dtz2 + d2u_dsy2*pown(dsy_dtz,2) + du_dsy*d2sy_dtz2;
}

float f_du_dsy (float tI, float dE_x, float Gy_21)
{
	return tI*dE_x*Gy_21;
}

float f_du_dsx (float tI, float dE_y, float Gx_21)
{
	return tI*dE_y*Gx_21;
}

float f_d2u_dsy2 (float tI, float dE_x, float Gy_53, float Gy_31)
{
	return tI*dE_x*(Gy_53-2*Gy_31);
}

float f_d2u_dsx2 (float tI, float dE_y, float Gx_53, float Gx_31)
{
	return f_d2u_dsy2 (tI, dE_y, Gx_53, Gx_31);
}

float f_Gx_nm (int n, int m, float sx, float exp_x_plus, float exp_x_minus, float x_tx_p12, float x_tx_m12)
{
	return native_recip(sqrt_2pi*pow(sx,n))*(pow(x_tx_m12,m)*exp_x_minus - pow(x_tx_p12,m)*exp_x_plus);
}

float f_Gy_nm (int n, int m, float sy, float exp_y_plus, float exp_y_minus, float y_ty_p12, float y_ty_m12)
{
	return f_Gx_nm(n, m, sy, exp_y_plus, exp_y_minus, y_ty_p12, y_ty_m12);
}

float f_dsi_dtz (float z_gi, float z_gi_2, float z_gi_3, float Ai, float Bi, float rdi2, float si0, float sigma_i_n)
{
	return native_divide(si0*rdi2*(2*z_gi + Ai*3*z_gi_2 + Bi*4*z_gi_3), 2*sigma_i_n);
}

float f_d2si_dtz2 (float z_gi, float z_gi_2, float z_gi_3, float Ai, float Bi, float rdi2, float si0, float sigma_i_n)
{
	return native_divide(si0*rdi2*(2 + Ai*6*z_gi + Bi*12*z_gi_2), 2*sigma_i_n) - native_divide(si0*rdi2*(2*z_gi + Ai*3*z_gi_2 + Bi*4*z_gi_3), 4*pown(sigma_i_n,3)) ;
}