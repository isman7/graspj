void elem_swap(ushort* a, ushort* b)
{
	ushort temp;
	temp = *a;
	*a = *b;
	*b = temp;
	return;
}