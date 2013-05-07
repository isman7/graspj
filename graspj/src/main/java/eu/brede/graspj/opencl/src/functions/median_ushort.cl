ushort kth_smallest(ushort a[], int n, int k) {
    int i,j,l,m ;
    ushort x ;

    l=0 ;
    m=n-1 ;
    while (l<m) {
        x=a[k] ;
        i=l ;
        j=m ;
        do {
            while (a[i]<x) i++ ;
            while (x<a[j]) j-- ;
            if (i<=j) {
                elem_swap(&(a[i]),&(a[j])) ;
                i++ ;
                j-- ;
            }
        } while (i<=j) ;
        if (j<k) l=i ;
        if (k<i) m=j ;
    }
    return a[k] ;
}
#define median(a,n) kth_smallest(a,n,(((n)&1)?((n)/2):(((n)/2)-1)))