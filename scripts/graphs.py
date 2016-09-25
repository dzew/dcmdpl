import random 
  
# Creates a completely connected graph. 
def completeGraph(n): 
    vertexSet = []
    for i in xrange(n): 
        vertexSet.append([j for j in xrange(n) if not j == i])
    return vertexSet 
  
# Creates a graph where node#0 is connected to all other nodes 
# and no other pairs of nodes are connected 
def starGraph(n): 
    vertexSet = [[0]] * n
    vertexSet[0] = range(1,n) 
    return vertexSet 
  
# Creates a graph where node#i is connected to nodes#(i+1) and (i-1). 
def linearGraph(n, m):
    vertexSet = []
    nbrs = [i for i in xrange(-m,m + 1) if i != 0]
    for i in xrange(n): 
        vertexSet.append([i+j for j in nbrs if 0 <= i+j < n])
    return vertexSet

def gridGraph(n,m):
    vertexSet = []
    nbrs = [(1,0),(-1,0),(0,1),(0,-1)]
    fix = lambda x,y: x + n * y
    valid = lambda x,y: 0 <= x < n and 0<= y < m
    for j in xrange(m):
        for i in xrange(n):
            vertexSet.append(
                [fix(i+di,j+dj) for di,dj in nbrs if valid(i+di,j+dj)])
    return vertexSet
