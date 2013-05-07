float f_du_dtx (float tI, float s, float exp_x_plus, float exp_x_minus, float dE_y)
{
	return (native_divide(tI,(sqrt_2pi*s))) * (exp_x_minus - exp_x_plus) * dE_y;
}

float f_du_dty (float tI, float s, float exp_y_plus, float exp_y_minus, float dE_x)
{
	return f_du_dtx (tI,s,exp_y_plus,exp_y_minus,dE_x);
}

float f_du_dtI (float dE_x, float dE_y)
{
	return dE_x*dE_y;
}

float f_d2u_dtx2 (float tI, float s, float exp_x_plus, float exp_x_minus, float dE_y, float x_tx_p12, float x_tx_m12)
{
	return (native_divide(tI,(sqrt_2pi*s*s*s))) * (x_tx_m12*exp_x_minus - x_tx_p12*exp_x_plus) * dE_y;
}

float f_d2u_dty2 (float tI, float s, float exp_y_plus, float exp_y_minus, float dE_x, float y_ty_p12, float y_ty_m12)
{
	return f_d2u_dtx2 (tI, s, exp_y_plus, exp_y_minus, dE_x, y_ty_p12, y_ty_m12);
}

float f_exp_i_pm (float two_s2, float i_ti_pm12)
{
	return native_exp(native_divide((-pown(i_ti_pm12,2)),two_s2));
}