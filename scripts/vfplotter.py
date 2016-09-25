import matplotlib.pyplot as pt
import numpy as np
from matplotlib import cm
from mpl_toolkits.mplot3d import Axes3D

#Makes a 2D scatterplot with points colored according to vals
def scatter(dat, vals = None):
    if vals is None:
        if len(dat[0]) < 3:
            vals = range(len(dat))
        else:
            vals = [i[2] for i in dat]
    mv = min(vals)
    vals = [i - mv for i in vals]
    mv = max(vals) * 1.0
    vals = [i / mv for i in vals]
    pt.scatter([i[0] for i in dat], [-i[1] for i in dat], s=80,
               c=cm.coolwarm(vals))
    pt.show()

# Draws a graph
# dat is a list of (x,y) coordinates
# gpg is an adjacency list representing the graph
def graph(dat, gph, c = lambda i, j: 'b', lw = 1):
    xs = [i[0] for i in dat]
    ys = [-i[1] for i in dat]
    for i, lst in enumerate(gph):
        for j in lst:
            u = pt.plot((xs[i], xs[j]), (ys[i], ys[j]),
                        color = c(i,j), linewidth = lw)
    pt.show()

def displayPlots():
    for i in seq:                                                                                                              
        pt.plot(x, i[0], 'y')                                                                                                    
        pt.plot(x, i[1], 'r')                                                                                                    
        pt.plot(x, i[2], 'b')                                                                                                    
        pt.show()

def makeLinePlots(x):
    #TODO: fix
    q = [[eval(i) for i in j.split(' ')] for j in x.split('\n')]
    bs = [i[0] for i in q]
    for i in xrange(len(q[0][1])):
        pt.plot(bs, [x[1][i] for x in q], label='iteration ' + str(i))
    pt.legend()
    pt.show()

# Method for visualising the output of PrintUtils
# try the following
#   surfaceView('1 2 3 4 \n2 3 4 5 \n3 4 5 6 ')
def surfaceView(zString):
    Z = [i.split(' ') for i in zString.split('\n')]
    for i in Z:
        i.pop()
    Z = [[float(f) for f in i] for i in Z]
    for i in Z:
        i.reverse()
    Z.reverse()
    showSurface(Z)

# Like scatter but in three dimensions.
def scatter3D(xs, vals):
    mv = min(vals)
    vals = [i - mv for i in vals]
    mv = max(vals)
    vals = [i / mv for i in vals]
    fig = pt.figure()
    ax = fig.add_subplot(111, projection='3d')
    ax.scatter([i[0] for i in xs],
               [i[1] for i in xs],
               [i[2] for i in xs], s=50, c=pt.cm.jet(vals))
    pt.show()

def showSurface(Z):
    X = np.arange(0,1,1./len(Z[0]))
    Y = np.arange(0,1,1./len(Z))
    #for i in xrange(len(X)):
    #    X[i] /= 1.*len(Z[0])
    #for i in xrange(len(Y)):
    #    Y[i] /= 1.*len(Z)
    X, Y = np.meshgrid(X, Y)
    fig = pt.figure()
    ax = fig.add_subplot(111, projection='3d')
    surf = ax.plot_surface(X, Y, Z, rstride=1, cstride=1, cmap=cm.coolwarm,
        linewidth=0, antialiased=False)
    fig.colorbar(surf, shrink=0.5, aspect=5)
    ax.set_xlabel('x-axis')
    ax.set_ylabel('y-axis')
    ax.set_zlabel('Value')
    #ax.set_title('Fourth Iteration of DKBRL. alpha=1, b=.09')
    ax.view_init(elev=-142, azim=49)
    pt.show()
