float4 calculate_CRLB (float matrix[4][4])
{	
	// float a0, a1, a2, a3, a4, b0, b1, b2, b3, b4;
	
	float a0 = matrix[0][0]*matrix[1][1] - matrix[0][1]*matrix[1][0];
	float a1 = matrix[0][0]*matrix[1][2] - matrix[0][2]*matrix[1][0];
	float a2 = matrix[0][0]*matrix[1][3] - matrix[0][3]*matrix[1][0];
	float a3 = matrix[0][1]*matrix[1][2] - matrix[0][2]*matrix[1][1];
	float a4 = matrix[0][1]*matrix[1][3] - matrix[0][3]*matrix[1][1];
	float a5 = matrix[0][2]*matrix[1][3] - matrix[0][3]*matrix[1][2];
	float b0 = matrix[2][0]*matrix[3][1] - matrix[2][1]*matrix[3][0];
	float b1 = matrix[2][0]*matrix[3][2] - matrix[2][2]*matrix[3][0];
	float b2 = matrix[2][0]*matrix[3][3] - matrix[2][3]*matrix[3][0];
	float b3 = matrix[2][1]*matrix[3][2] - matrix[2][2]*matrix[3][1];
	float b4 = matrix[2][1]*matrix[3][3] - matrix[2][3]*matrix[3][1];
	float b5 = matrix[2][2]*matrix[3][3] - matrix[2][3]*matrix[3][2];

	float det = a0*b5 - a1*b4 + a2*b3 + a3*b2 - a4*b1 + a5*b0;
	
	float det_recip = native_recip(det);
	
	float4 CRLB;
	if (isinf(det_recip))
	{
		CRLB.x = 0;
		CRLB.y = 0;
		CRLB.z = 0;
		CRLB.w = 0;
	}
	else
	{
		CRLB.x = det_recip * (matrix[1][1]*b5 - matrix[1][2]*b4 + matrix[1][3]*b3);
		CRLB.y = det_recip * (matrix[0][0]*b5 - matrix[0][2]*b2 + matrix[0][3]*b1);
		CRLB.z = det_recip * (matrix[3][0]*a4 - matrix[3][1]*a2 + matrix[3][3]*a0);
		CRLB.w = det_recip * (matrix[2][0]*a3 - matrix[2][1]*a1 + matrix[2][2]*a0);
	}

	//CRLB.x = -1.0f;
	
	return CRLB;
}