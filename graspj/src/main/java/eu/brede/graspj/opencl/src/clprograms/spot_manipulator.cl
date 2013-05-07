__kernel void shift_spots	(
								global float* spots,
								global int* frame_nrs,
								global float2* drift
							)

{
	int spot_nr = get_global_id(0);
	int frame_nr = frame_nrs[spot_nr];
	int parameters_per_spot = 5;
	int vars_per_parameter = 2;

	int spot_index = spot_nr * parameters_per_spot * vars_per_parameter;
	
	spots[spot_index+0] = spots[spot_index+0] + drift[frame_nr].x;
	spots[spot_index+1] = spots[spot_index+1] + drift[frame_nr].y;

	
}