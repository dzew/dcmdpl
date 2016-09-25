import matplotlib.pyplot as pt
import math
import graphs
import funcs

def tostr(lst):
    return ['%.3f'%i for i in lst]

def error(ys, yt):
    return sum([(a-b)*(a-b) for a,b in zip(ys,yt)])

#smoothing kernels
def gaussian(d,b):
    return math.exp(-(d*d)/(b*b))

def epan(d,b):
    diff = d / b
    if diff > .9999:
        diff = .9999
    return (1 - diff * diff)

#Euclidean distance function
def dist(x1,x2):
    if isinstance(x1, float) or isinstance(x1, int):
        return abs(x1 - x2)
    return sum([(i-j)*(i-j) for i,j in zip(x1,x2)])**.5

def tfKernel(df, kern):
    def g(x1,x2,b):
        return kern(df(x1,x2), b)
    return g

#smoothers
def model(xs, f, k, b):
    n = [sum([k(x,x2,b) for x in xs]) for x2 in xs]
    return [sum([k(x,x2,b)*f(x2)/n[i] for x2 in xs]) for i, x in enumerate(xs)]

def extrapolate(xs, ys, nx, k, b):
    ns = [sum([k(x,x2,b) for x in xs]) for x2 in nx]
    return [sum([k(x,x2,b)*y/n for x2,y in zip(xs,ys)]) for x,n in zip(nx,ns)]

def model2(xs, f, nx, k, b):
    ys = model(xs, f, k, b)
    return extrapolate(xs, ys, nx, k, b)

def nadWatson(xs, f, k, b):
    cache = {}
    def ret(x):
        if x in cache:
            return cache[x]
        ks = [k(x,xi,b) for xi in xs]
        res = sum([ks[i]*f(xi) for i,xi in enumerate(xs)])/sum(ks)
        cache[x] = res
        return res
    return ret
