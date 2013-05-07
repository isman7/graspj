	float sx0 = dfcx[0];
	float gx = dfcx[1];
	float dx = dfcx[2];
	float Ax = dfcx[3];
	float Bx = dfcx[4];
	
	float sy0 = dfcy[0];
	float gy = dfcy[1];
	float dy = dfcy[2];
	float Ay = dfcy[3];
	float By = dfcy[4];	
	
	// TODO use native recipe?
	float rdx2 = 1.0f / (dx*dx);
	float rdy2 = 1.0f / (dy*dy);
	
	/*printf("sy0: %f\n",sy0);
	printf("gy: %f\n",gy);
	printf("dy: %f\n",dy);
	printf("Ay: %f\n",Ay);
	printf("By: %f\n",By);
	printf("sx0: %f\n",sx0);
	printf("gx: %f\n",gx);
	printf("dx: %f\n",dx);
	printf("Ax: %f\n",Ax);
	printf("Bx: %f\n",Bx);*/
	
	/*float sx0 = (294.1f / pixel_size) / 2.0f;
	float sy0 = (307.9f / pixel_size) / 2.0f;
	
	float dx = 416.0f / pixel_size;
	float dy = 414.5f / pixel_size;
	
	float rdx2 = 1.0f / (dx*dx);
	float rdy2 = 1.0f / (dy*dy);
	
	float Ax = -0.126f / dx;
	float Bx = 0.46f / (dx*dx);
	float gx = -240.7f / pixel_size;
	
	float Ay = 0.596f / dy;
	float By = 0.653 / (dy*dy);
	float gy = 135.3 / pixel_size;
	
	if(spot_nr == 1)
	{
		printf("sy0: %f\n",sy0);
		printf("gy: %f\n",gy);
		printf("dy: %f\n",dy);
		printf("Ay: %f\n",Ay);
		printf("By: %f\n",By);
		printf("sx0: %f\n",sx0);
		printf("gx: %f\n",gx);
		printf("dx: %f\n",dx);
		printf("Ax: %f\n",Ax);
		printf("Bx: %f\n",Bx);
		printf("\n\n %f\n",pixel_size);
	}*/
	