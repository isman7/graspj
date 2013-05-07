float f_du_dts (
				float tI, float s2,
				float exp_x_plus, float exp_x_minus, float x_tx_p12, float x_tx_m12, float dE_x,
				float exp_y_plus, float exp_y_minus, float y_ty_p12, float y_ty_m12, float dE_y)
{
	return tI*0.5f*(((x_tx_m12*exp_x_minus-x_tx_p12*exp_x_plus)/(sqrt_2pi*s2))*dE_y
			+ ((y_ty_m12*exp_y_minus-y_ty_p12*exp_y_plus)/(sqrt_2pi*s2))*dE_x);
}

float f_d2u_dts2 (
				float tI, float s2, float s3, float s5,
				float exp_x_plus, float exp_x_minus, float x_tx_p12, float x_tx_m12, float dE_x,
				float exp_y_plus, float exp_y_minus, float y_ty_p12, float y_ty_m12, float dE_y)
{
	return tI*0.5f*(
					(((pown(x_tx_m12,3)*exp_x_minus-pown(x_tx_p12,3)*exp_x_plus)/(sqrt_2pi*s5))
					-(2*(x_tx_m12*exp_x_minus+x_tx_p12*exp_x_plus))/(sqrt_2pi*s3))*dE_y
					+
					(((pown(y_ty_m12,3)*exp_y_minus-pown(y_ty_p12,3)*exp_y_plus)/(sqrt_2pi*s5))
					-(2*(y_ty_m12*exp_y_minus+y_ty_p12*exp_y_plus))/(sqrt_2pi*s3))*dE_x
					+
					2*(((x_tx_m12*exp_x_minus-x_tx_p12*exp_x_plus)/(sqrt_2pi*s2))
						*((y_ty_m12*exp_y_minus-y_ty_p12*exp_y_plus)/(sqrt_2pi*s2)))
					);
}