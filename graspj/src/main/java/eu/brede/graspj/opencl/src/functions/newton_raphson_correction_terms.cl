float correction_term1 (float t, float du_dt, float u, float mu)
{
	return (du_dt * (native_divide(u,mu) -1));
}

float correction_term2 (float t, float du_dt, float d2u_dt2, float u, float mu)
{
	return (d2u_dt2 * (native_divide(u,mu) - 1) - pown(du_dt,2) * native_divide(u,(pown(mu,2))));
}