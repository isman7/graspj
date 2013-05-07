void inverse_5x5_matrix(private float A[5][5], private float I[5][5])
{

	private float augmentedmatrix[5][10] ;
	/* 2D array declared to store augmented matrix */
	float temporary, r ;
	int i, j, k, dimension, temp;                                  /* declaring counter variables for loops */

	dimension = 5;

	for(i=0; i<dimension; i++)
		for(j=0; j<dimension; j++)
			augmentedmatrix[i][j]=A[i][j];

	/* augmenting with identity matrix of similar dimensions */

	for(i=0;i<dimension; i++)
		for(j=dimension; j<2*dimension; j++)
			if(i==j%dimension)
				augmentedmatrix[i][j]=1;
			else
				augmentedmatrix[i][j]=0;

	/* using gauss-jordan elimination */
	
	for(j=0; j<dimension; j++)
	{
		temp=j;

		/* finding maximum jth column element in last (dimension-j) rows */

		for(i=j+1; i<dimension; i++)
			if(augmentedmatrix[i][j]>augmentedmatrix[temp][j])
				temp=i;


		/* swapping row which has maximum jth column element */

		if(temp!=j)
			for(k=0; k<2*dimension; k++)
			{
				temporary=augmentedmatrix[j][k] ;
				augmentedmatrix[j][k]=augmentedmatrix[temp][k] ;
				augmentedmatrix[temp][k]=temporary ;
			}

		/* performing row operations to form required identity matrix out of the input matrix */

		for(i=0; i<dimension; i++)
			if(i!=j)
			{
				r=augmentedmatrix[i][j];
				for(k=0; k<2*dimension; k++)
					augmentedmatrix[i][k]-=(augmentedmatrix[j][k]/augmentedmatrix[j][j])*r ;
			}
			else
			{
				r=augmentedmatrix[i][j];
				for(k=0; k<2*dimension; k++)
					augmentedmatrix[i][k]/=r ;
			}
	}


	for(i=0; i<dimension; i++)
		for(j=dimension; j<2*dimension; j++)
			I[i][j-dimension] = augmentedmatrix[i][j];

}