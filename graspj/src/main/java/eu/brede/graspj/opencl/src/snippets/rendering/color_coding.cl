float m = (hmax-hmin)/(zmax-zmin);
float b = (hmax*zmin+hmin*zmax)/(zmin-zmax);

#define f_hue(z) (rint(fmax(fmin((m*(z)+b),hmax),hmin)))