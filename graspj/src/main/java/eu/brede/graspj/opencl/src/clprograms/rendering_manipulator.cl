#define M_PI      3.141592653589793

// a*b => a, b unchanged

#define MUL(a,b,tmp) { tmp=a; a.x=tmp.x*b.x-tmp.y*b.y; a.y=tmp.x*b.y+tmp.y*b.x; }

// DFT2(a,b) => (a,b)

#define DFT2(a,b,tmp) { tmp=a-b; a+=b; b=tmp; }

// Return cos(alpha)+I*sin(alpha)

float2 exp_alpha(float alpha)
{
  float cs,sn;
  sn = sincos(alpha,&cs);
  return (float2)(cs,sn);
}


// Complex product, multiply vectors of complex numbers


#define MUL_RE(a,b) (a.even*b.even - a.odd*b.odd)
#define MUL_IM(a,b) (a.even*b.odd + a.odd*b.even)

float2 mul_1(float2 a,float2 b)
{ float2 x; x.even = MUL_RE(a,b); x.odd = MUL_IM(a,b); return x; }

float4 mul_2(float4 a,float4 b)
{ float4 x; x.even = MUL_RE(a,b); x.odd = MUL_IM(a,b); return x; }

// Return the DFT2 of the two complex numbers in vector A

float4 dft2_2(float4 a) { return (float4)(a.lo+a.hi,a.lo-a.hi); }

// Return cos(alpha)+I*sin(alpha)  (3 variants)

float2 exp_alpha_1(float alpha)
{
  float cs,sn;
  // sn = sincos(alpha,&cs);  // sincos

  cs = native_cos(alpha); sn = native_sin(alpha);  // native sin+cos

  // cs = cos(alpha); sn = sin(alpha); // sin+cos

  return (float2)(cs,sn);
}



// mul_p*q*(a) returns a*EXP(-I*PI*P/Q)

#define mul_p0q1(a) (a)

#define mul_p0q2 mul_p0q1
float2  mul_p1q2(float2 a) { return (float2)(a.y,-a.x); }

__constant float SQRT_1_2 = 0.707106781188f; // cos(Pi/4)

#define mul_p0q4 mul_p0q2
float2  mul_p1q4(float2 a) { return (float2)(SQRT_1_2)*(float2)(a.x+a.y,-a.x+a.y); }
#define mul_p2q4 mul_p1q2
float2  mul_p3q4(float2 a) { return (float2)(SQRT_1_2)*(float2)(-a.x+a.y,-a.x-a.y); }

// T = N/8 = number of threads.

// P is the length of input sub-sequences, 1,8,64,...,N/8.



#define MUL_RE(a,b) (a.even*b.even - a.odd*b.odd)
#define MUL_IM(a,b) (a.even*b.odd + a.odd*b.even)




__kernel void fft_radix2(__global const float2* x,__global float2* y,const int global_size,const int p)
{
  //int p = pA[0];
  
  //int i = get_global_id(0); // number of threads

  //int t = get_global_size(0); // current thread

  //int global_size = get_global_size(0);
  int global_id = get_global_id(0);
  
  
  //int rows = 4096; // what for?
  // (float) cast added
  int t = (int) rint(native_divide(native_sqrt((float)2*global_size),2));
  int i = global_id % t;
  
  int row = global_id / t;
  
  int offset = 2*row*t;// = get_global_offset(0);
  x += offset;
  y += offset;

  int k = i & (p-1); // index in input sequence, in 0..P-1

  x += i; // input offset

  y += (i<<1) - k; // output offset

  float4 u = dft2_2( (float4)(
    x[0],
    mul_1(exp_alpha_1(-M_PI*(float)k/(float)p),x[t]) ));
  y[0] = u.lo;
  y[p] = u.hi;
}



__kernel void fft_radix8(__global const float2* x,__global float2* y,__global int* pA)
{
  
  // int t = get_global_size(0); // number of threads

  // int i = get_global_id(0); // current thread

  int global_size = get_global_size(0);
  int global_id = get_global_id(0);
  
  
  //int rows = 4096; // what for?
  // (float) cast added
  int t = (int) rint(native_divide(native_sqrt((float) 8*global_size),8));
  int i = global_id % t;
  
  int row = global_id / t;
  
  int offset = row*8*t;// = get_global_offset(0); 8 * t because of t = width / 8
  x += offset;
  y += offset;
  
  
  int p = pA[0];
  
  int k = i & (p-1); // index in input sequence, in 0..P-1
  


  // Inputs indices are I+{0,1,2,3,4,5,6,7}*T

  x += i;
  // Output indices are J+{0,1,2,3,4,5,6,7}*P, where

  // J is I with three 0 bits inserted at bit log2(P)

  y += ((i-k)<<3) + k;

  // Load and twiddle inputs

  // Twiddling factors are exp(_I*PI*{0,1,2,3,4,5,6,7}*K/4P)

  float alpha = -M_PI*(float)k/(float)(4*p);

  // Load and twiddle

// Load and twiddle (variant A)

float2 u0 = x[0];
float2 u1 = mul_1(exp_alpha_1(alpha),x[t]);
float2 u2 = mul_1(exp_alpha_1(2*alpha),x[2*t]);
float2 u3 = mul_1(exp_alpha_1(3*alpha),x[3*t]);
float2 u4 = mul_1(exp_alpha_1(4*alpha),x[4*t]);
float2 u5 = mul_1(exp_alpha_1(5*alpha),x[5*t]);
float2 u6 = mul_1(exp_alpha_1(6*alpha),x[6*t]);
float2 u7 = mul_1(exp_alpha_1(7*alpha),x[7*t]);


  // 4x in-place DFT2 and twiddle

  float2 v0 = u0 + u4;
  float2 v4 = mul_p0q4(u0 - u4);
  float2 v1 = u1 + u5;
  float2 v5 = mul_p1q4(u1 - u5);
  float2 v2 = u2 + u6;
  float2 v6 = mul_p2q4(u2 - u6);
  float2 v3 = u3 + u7;
  float2 v7 = mul_p3q4(u3 - u7);

  // 4x in-place DFT2 and twiddle

  u0 = v0 + v2;
  u2 = mul_p0q2(v0 - v2);
  u1 = v1 + v3;
  u3 = mul_p1q2(v1 - v3);
  u4 = v4 + v6;
  u6 = mul_p0q2(v4 - v6);
  u5 = v5 + v7;
  u7 = mul_p1q2(v5 - v7);

  // 4x DFT2 and store (reverse binary permutation)

  y[0]   = u0 + u1;
  y[p]   = u4 + u5;
  y[2*p] = u2 + u3;
  y[3*p] = u6 + u7;
  y[4*p] = u0 - u1;
  y[5*p] = u4 - u5;
  y[6*p] = u2 - u3;
  y[7*p] = u6 - u7;
}

__kernel void transpose(__global float2* input, __global float2* output) // will be slow, but does it matter?
{
    int xIndex = get_global_id(0);
    int yIndex = get_global_id(1);
	int width = get_global_size(0);
	int height = get_global_size(1);
    
    
    int index_in  = xIndex + width * yIndex;
    int index_out = yIndex + height * xIndex;
    
	output[index_out] = input[index_in];   
}

__kernel void quick_transpose(__global float2* image) // will be slow, but does it matter?
{
    int xIndex = get_global_id(0);
    int yIndex = get_global_id(1);
	int width = get_global_size(0)*2;
	int height = get_global_size(1);
	int lid = get_local_id(0);
	
    int index1  = xIndex + width * yIndex;
    int index2 = yIndex + height * xIndex;
    
    local float2 cache1[64];
    local float2 cache2[64];
    
    async_work_group_copy(cache1+lid,image+index1,1,0);
    async_work_group_copy(cache2+lid,image+index2,1,0);
    mem_fence(CLK_LOCAL_MEM_FENCE);

    //prefetch(image+index1,8);
    //prefetch(image+index2,8);
    //float2 tempValue1 = cache1[lid];
    //float2 tempValue2 = cache2[lid];
    image[index1] = cache2[lid];
    image[index2] = cache1[lid];   
}

#define PADDING         (0)
#define GROUP_DIMX      (32)
#define LOG_GROUP_DIMX  (5)
#define GROUP_DIMY      (2)
//define WIDTH           (2048)
//define HEIGHT          (2048)

__kernel void apple_transpose(
    __global float2* input, 
    __global float2* output,
    __local float2* tile,
	const int WIDTH,
	const int HEIGHT)
{
	
	int block_x = get_group_id(0);
	int block_y = get_group_id(1);
		
	int local_x = get_local_id(0) & (GROUP_DIMX - 1);
	int local_y = get_local_id(0) >> LOG_GROUP_DIMX;
		
	int local_input  = mad24(local_y, GROUP_DIMX + 1, local_x);
	int local_output = mad24(local_x, GROUP_DIMX + 1, local_y);
	
	int in_x = mad24(block_x, GROUP_DIMX, local_x);
	int in_y = mad24(block_y, GROUP_DIMX, local_y);	
	int input_index = mad24(in_y, WIDTH, in_x);	
		
	int out_x = mad24(block_y, GROUP_DIMX, local_x);
	int out_y = mad24(block_x, GROUP_DIMX, local_y);	
	
	int output_index = mad24(out_y, HEIGHT + PADDING, out_x);	
	
	int global_input_stride  = WIDTH * GROUP_DIMY;
	int global_output_stride = (HEIGHT + PADDING) * GROUP_DIMY;
	
	int local_input_stride  = GROUP_DIMY * (GROUP_DIMX + 1);
	int local_output_stride = GROUP_DIMY;
	
	tile[local_input] = input[input_index];	local_input += local_input_stride; input_index += global_input_stride;
	tile[local_input] = input[input_index];	local_input += local_input_stride; input_index += global_input_stride;
	tile[local_input] = input[input_index];	local_input += local_input_stride; input_index += global_input_stride;
	tile[local_input] = input[input_index];	local_input += local_input_stride; input_index += global_input_stride;
	tile[local_input] = input[input_index];	local_input += local_input_stride; input_index += global_input_stride;
	tile[local_input] = input[input_index];	local_input += local_input_stride; input_index += global_input_stride;
	tile[local_input] = input[input_index];	local_input += local_input_stride; input_index += global_input_stride;
	tile[local_input] = input[input_index];	local_input += local_input_stride; input_index += global_input_stride;
	tile[local_input] = input[input_index];	local_input += local_input_stride; input_index += global_input_stride;
	tile[local_input] = input[input_index];	local_input += local_input_stride; input_index += global_input_stride;
	tile[local_input] = input[input_index];	local_input += local_input_stride; input_index += global_input_stride;
	tile[local_input] = input[input_index];	local_input += local_input_stride; input_index += global_input_stride;
	tile[local_input] = input[input_index];	local_input += local_input_stride; input_index += global_input_stride;
	tile[local_input] = input[input_index];	local_input += local_input_stride; input_index += global_input_stride;
	tile[local_input] = input[input_index];	local_input += local_input_stride; input_index += global_input_stride;
	tile[local_input] = input[input_index];	
	
	barrier(CLK_LOCAL_MEM_FENCE);
	
	output[output_index] = tile[local_output]; local_output += local_output_stride; output_index += global_output_stride;
	output[output_index] = tile[local_output]; local_output += local_output_stride; output_index += global_output_stride;
	output[output_index] = tile[local_output]; local_output += local_output_stride; output_index += global_output_stride;
	output[output_index] = tile[local_output]; local_output += local_output_stride; output_index += global_output_stride;
	output[output_index] = tile[local_output]; local_output += local_output_stride; output_index += global_output_stride;
	output[output_index] = tile[local_output]; local_output += local_output_stride; output_index += global_output_stride;
	output[output_index] = tile[local_output]; local_output += local_output_stride; output_index += global_output_stride;
	output[output_index] = tile[local_output]; local_output += local_output_stride; output_index += global_output_stride;
	output[output_index] = tile[local_output]; local_output += local_output_stride; output_index += global_output_stride;
	output[output_index] = tile[local_output]; local_output += local_output_stride; output_index += global_output_stride;
	output[output_index] = tile[local_output]; local_output += local_output_stride; output_index += global_output_stride;
	output[output_index] = tile[local_output]; local_output += local_output_stride; output_index += global_output_stride;
	output[output_index] = tile[local_output]; local_output += local_output_stride; output_index += global_output_stride;
	output[output_index] = tile[local_output]; local_output += local_output_stride; output_index += global_output_stride;
	output[output_index] = tile[local_output]; local_output += local_output_stride; output_index += global_output_stride;
	output[output_index] = tile[local_output]; 
	
}

__kernel void complex_conjugate (__global float2* input, __global float2* output)
{
	int xIndex = get_global_id(0);
    int yIndex = get_global_id(1);
	int width = get_global_size(0);
	int height = get_global_size(1);

	int index  = xIndex + width * yIndex;
	
	output[index].x = input[index].x;
	output[index].y = -input[index].y;
}

__kernel void absolute (__global float2* input, __global float2* output)
{
	int xIndex = get_global_id(0);
    int yIndex = get_global_id(1);
	int width = get_global_size(0);
	int height = get_global_size(1);

	int index  = xIndex + width * yIndex;
	
	//output[index].x = native_sqrt(native_powr(input[index].x,2)+native_powr(input[index].y,2));
	output[index].x = native_sqrt(pown(input[index].x,2)+pown(input[index].y,2));
	output[index].y = 0;
}

__kernel void hanning_window (__global float2* input, __global float2* output)
{
	int xIndex = get_global_id(0);
    int yIndex = get_global_id(1);
	int width = get_global_size(0);
	int height = get_global_size(1);

	int index  = xIndex + width * yIndex;
	
	//(float) cast added
	float ru = native_divide((float)2*xIndex,width)-1;
	float rv = native_divide((float)2*yIndex,height)-1;
	//float ruv = native_powr(ru,2) + native_powr(rv,2);
	float ruv = pown(ru,2) + pown(rv,2);
	ruv = native_sqrt(ruv);
	float wuv =  0.5*(native_cos(M_PI*ruv)+1);
	if (ruv >= 0 && ruv < 1) 
	{
		output[index].x = input[index].x * wuv;
		output[index].y = input[index].y * wuv;
	}
	else
	{
		output[index] = (0,0);
	}
}

__kernel void divide_complex_by_N (__global float2* input, __global float2* output)
{
	int xIndex = get_global_id(0);
    int yIndex = get_global_id(1);
	int width = get_global_size(0);
	int height = get_global_size(1);

	int index  = xIndex + width * yIndex;
	float n = width*height;
	float2 N = (n,n);
	
	output[index] = native_divide(input[index], N);
}

__kernel void divide_complex (__global float2* numerator, __global float2* denominator, __global float2* fraction)
{
	int xIndex = get_global_id(0);
    int yIndex = get_global_id(1);
	int width = get_global_size(0);
	int height = get_global_size(1);
	
	int index  = xIndex + width * yIndex;
	
	float fraction_denominator;
	//fraction_denominator = native_powr(denominator[index].x,2)+native_powr(denominator[index].y,2);
	fraction_denominator = pown(denominator[index].x,2)+pown(denominator[index].y,2);
		
	fraction[index].x = native_divide(numerator[index].x * denominator[index].x + numerator[index].y * denominator[index].y,fraction_denominator);
	fraction[index].y = native_divide(numerator[index].y * denominator[index].x - numerator[index].x * denominator[index].y,fraction_denominator);
	
}

__kernel void complex_multiply (__global float2* multiplicator_a, __global float2* multiplicator_b, __global float2* product)
{
	int xIndex = get_global_id(0);
    int yIndex = get_global_id(1);
	int width = get_global_size(0);
	int height = get_global_size(1);
	
	int index  = xIndex + width * yIndex;
	
	product[index].x = multiplicator_a[index].x * multiplicator_b[index].x - multiplicator_a[index].y * multiplicator_b[index].y;
	product[index].y = multiplicator_a[index].y * multiplicator_b[index].x + multiplicator_a[index].x * multiplicator_b[index].y;
	
}

__constant uchar matrix[25] = {64,64,64,64,64,64,16,16,16,64,64,16,4,16,64,64,16,16,16,64,64,64,64,64,64};

__kernel void blur (__global float2* input, __global float2* output)
{
	 
	//uchar matrix[25] = {64,64,64,64,64,64,16,16,16,64,64,16,4,16,64,64,16,16,16,64,64,64,64,64,64};
	
	int xIndex = get_global_id(0);
    int yIndex = get_global_id(1);
	int width = get_global_size(0);
	int height = get_global_size(1);
	
	int oindex  = xIndex + width * yIndex;
	int2 box_radius;
	box_radius.x = 2;
	box_radius.y = 2;
	
	int2 pos;
	pos.x = xIndex;
	pos.y = yIndex;
	
	int2 start = pos-box_radius;
	int2 end = pos+box_radius;
	
	float total_intensity = 0;
	int x,y;
	for (y=start.y; y<=end.y; y++)
	{
		int index_y = y;
		int matrix_offset = (y-start.y)*5; // 5 = 2*box_size +1
		
		if (index_y<0)
		{
			index_y += height;
		}
		if (index_y>height-1)
		{
			index_y -= height;
		}
		
		int index_offset = width * index_y;
		for (x=start.x; x<=end.x; x++)
		{
			int index_x = x;
			int matrix_x = x-start.x;
			
			if (index_x<0)
			{
				index_x += width;
			}
			if (index_x>width-1)
			{
				index_x -= width;
			}
			
			int index  = index_x + index_offset;
			int matrix_index = matrix_x + matrix_offset;
			float intensity = native_divide(input[index].x, matrix[matrix_index]);
			
			total_intensity += intensity;
		}
	}
	
	output[oindex].x = total_intensity;
	output[oindex].y = input[oindex].y;
}


/*kernel void find_peak_dac (
							global float* in_values,
							global float* in_positions,
							global float* out_values,
							global float* out_positions )
{
	int out_index = get_global_id();
	int in_index = out_index*2;
	
	if(in_values[in_index] > in_values[in_index+1])
	{
		out_values[out_index] = in_values[in_index];
		out_positions[out_index] = in_positions[in_index];
	}
	else
	{
		out_values[out_index] = in_values[in_index+1];
		out_positions[out_index] = in_positions[in_index+1];
	}
}*/

kernel void first_quick_find_peak (
	global float2* image,
	global float* out_values,
	global int* out_indices,
	const int elements_per_work_item)
{
	int out_index=get_global_id(0);
	int in_index=out_index*elements_per_work_item;
	
	float max_value = -1.0e10f; // replace with -INFINITY or -HUGE_VALF or -MAX_FLOAT??
	int max_index=-42;
	// perhaps cache values?
	int i;
	for(i=0;i<elements_per_work_item;i++)
	{
		int index=in_index+i;
		float current_value=image[index].x;
		
		if(current_value>max_value)
		{
			max_value = current_value;
			max_index = index;
		}
	}
	
	out_values[out_index]=max_value;
	out_indices[out_index]=max_index;
}

kernel void quick_find_peak (
	global float* in_values,
	global int* in_indices,
	global float* out_values,
	global int* out_indices,
	const int elements_per_work_item)
{
	int out_index=get_global_id(0);
	int in_index=out_index*elements_per_work_item;
	
	float max_value = -1.0e10f; // replace with -INFINITY or -HUGE_VALF or -MAX_FLOAT??
	int max_index=0;
	// perhaps cache values?
	int i;
	for(i=0;i<elements_per_work_item;i++)
	{
		int index=in_index+i;
		float current_value=in_values[index];
		
		if(current_value>max_value)
		{
			max_value = current_value;
			max_index = in_indices[index];
		}
	}
	
	out_values[out_index]=max_value;
	out_indices[out_index]=max_index;
}

kernel void refine_peak (
	global float2* image,
	global int* index,
	global float2* result,
	const int width,
	const int height,
	const int box_radius_1)
{

	int2 box_radius = (box_radius_1,box_radius_1);

	int2 pos;
	pos.x = index[0]%width;//height switch / and %?
	pos.y = index[0]/width;
	
	int x,y;

	
	int2 start = pos-box_radius;
	int2 end = pos+box_radius;
	
	
	float2 total_pos = (0,0);
	float total_intensity = 0;
	
	for (y=start.y; y<=end.y; y++)
	{
		int index_y = y;
		if (index_y<0)
		{
			index_y += height;
		}
		if (index_y>height-1)
		{
			index_y -= height;
		}
		
		int index_offset = width * index_y;
		for (x=start.x; x<=end.x; x++)
		{
			int index_x = x;
			if (index_x<0)
			{
				index_x += width;
			}
			if (index_x>width-1)
			{
				index_x -= width;
			}
			
			int index  = index_x + index_offset;
			float intensity = image[index].x;
			total_pos.x = (total_pos.x*total_intensity + x*intensity)/(total_intensity + intensity);
			total_pos.y = (total_pos.y*total_intensity + y*intensity)/(total_intensity + intensity);
			total_intensity += intensity;
		}
	}
	
	//result[0] = pos;
	// result[0].x = 23;
	// result[0].y = 42;
	
	result[0].x = total_pos.x;
	result[0].y = total_pos.y;
}
	

__kernel void find_peak (
	global float2* image,
	global float2* result,
	const int width,
	const int height,
	const int box_radius_1)
{	
	//int width = parameters[0];
	//int height = parameters[1];
	int2 box_radius = (box_radius_1,box_radius_1);
	
	float intensity = -1.0e10f;
	int2 pos;
	//pos.x = 19;
	//pos.y = 86;
	int x,y;
	for (y=0; y<height; y++)
	{
		int index_offset = width * y;
		for (x=0; x<width; x++)
		{
			if ((x < 50) || (x > width-50))
				if ((y < 50) || (y > height-50))
				{
					int index  = x + index_offset;
					if (image[index].x > intensity)
					{
						intensity = image[index].x;
						pos.x = x;
						pos.y = y;
					}
				}
		}
	}
	
	//int2 start = max(pos-box_radius,(0,0));
	//int2 end = min(pos+box_radius,(width-1,height-1));
	
	int2 start = pos-box_radius;
	int2 end = pos+box_radius;
	
	
	float2 total_pos = (0,0);
	float total_intensity = 0;
	
	for (y=start.y; y<=end.y; y++)
	{
		int index_y = y;
		if (index_y<0)
		{
			index_y += height;
		}
		if (index_y>height-1)
		{
			index_y -= height;
		}
		
		int index_offset = width * index_y;
		for (x=start.x; x<=end.x; x++)
		{
			int index_x = x;
			if (index_x<0)
			{
				index_x += width;
			}
			if (index_x>width-1)
			{
				index_x -= width;
			}
			
			int index  = index_x + index_offset;
			float intensity = image[index].x;
			total_pos.x = (total_pos.x*total_intensity + x*intensity)/(total_intensity + intensity);
			total_pos.y = (total_pos.y*total_intensity + y*intensity)/(total_intensity + intensity);
			total_intensity += intensity;
		}
	}
	
	//result[0] = pos;
	// result[0].x = 23;
	// result[0].y = 42;
	
	result[0].x = total_pos.x;
	result[0].y = total_pos.y;
	
	// result[0].x = image[0*4096+0].x;
	// result[0].y = image[20*4096+20].x;
	
	// result[0].x = image[0*512+0].x;
	// result[0].y = image[20*512+20].x;
	
}
