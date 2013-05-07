float f_dE_i (float i_ti_p12, float i_ti_m12, float s_sqrt2)
{
	return 0.5f * (erf(native_divide(i_ti_p12,s_sqrt2)) - erf(native_divide(i_ti_m12,s_sqrt2)));
}

float f_i_ti_p12 (float i, float ti)
{
	return i-ti+0.5f;
}

float f_i_ti_m12 (float i, float ti)
{
	return i-ti-0.5f;
}

float f_mu (float tI, float tB, float dE_x, float dE_y)
{
	return tI*dE_x*dE_y + tB;
}