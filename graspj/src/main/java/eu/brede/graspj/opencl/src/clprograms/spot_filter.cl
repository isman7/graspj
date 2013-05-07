#pragma OPENCL EXTENSION cl_amd_printf : enable

kernel void filter_spots	(
								global float* minima, //both constant?!
								global float* maxima,
								global float* spots,
								global char* mask
							)
{
	
	int number_of_parameters = 10;
	int spot_nr = get_global_id(0);
	int spot_index = spot_nr*number_of_parameters;
	float value = spots[spot_index];
	
	bool valid = true;
	
	/*if (value == 0.0f)
	{
		valid = false;
	}*/
	
	int i;
	for (i=0; i<number_of_parameters; i++)
	{
		//spots[spot_index+i] = 1986.1603f;//convert_float(spot_index+i);
		value = spots[spot_index+i];
		//spots[spot_index+i] = value+1.0f;
		
		
		/*if ((value == value) == false)
		{
			valid = false;
			//break;
		}*/
		
		if (isnan(value)>0) // todo remove > 0 and test
		{
			valid = false;
			break;
		}
		
		if (value < minima[i])
		{
			valid = false;
			break;
		}
		
		if (value > maxima[i])
		{
			valid = false;
			//mask[spot_nr] = i;
			break;
		}
		
	}
	
	// temp fix
	// sx/sy <= 1.4
	//printf("right paras? %f, %f\n",spots[spot_index+5],spots[spot_index+6]);
	float ratio = ((float)spots[spot_index+5])/((float)spots[spot_index+6]);
	
	if(ratio > 1.4f) valid = false;
	if(1/ratio > 1.4f) valid = false;
	
	if(valid==true)
	{
		mask[spot_nr] = 1;
	}
	else
	{
		mask[spot_nr] = 0;
	}
}