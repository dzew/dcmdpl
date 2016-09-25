import math

cos = math.cos

#1D functions
def identity(x):
    return x

def parab(x):
    return 4 * (x-.5)**2

def angle(x):
    if x > .5:
        return 2 - 2*x
    return 2*x

def angle2(x):
    if x > .5:
        x = 1 - x
    return 4*x*x

def bumpy(x):
    x *= math.pi
    v = cos(x) - .7*cos(2*x) - .6*cos(3*x) + .4*cos(4*x) - .5*cos(5*x)
    return v / 3.13434588481

def step(x):
    eps = .0000001
    if abs(x-.5) < eps:
        return 2*(x - .5)/eps + 2
    if x > .5:
        return 4. + -x + .5
    return -x - 0.5

def flip(x):
    eps = .0001
    x = 2*x - 1
    if x < -eps:
        return x*x*x + 1
    if x > eps:
        return x*x*x - 1
    return - x /eps

def threeStep(x):
    if x < .3:
        return 1 + cos(1000 * x)
    if x < .7:
        return 10 + cos(800 * x)
    return 4 + cos(1200 * x)

def edges(x):
    return math.tan(3 * (x - .5))
    

#2D functions
def rosenbrock((x,y)):
    return (1-x)*(1-x) + 100 * (y - x*x) ** 2
    
def tworoom((x,y)):
    if y > .5:
        return 2 - x
    return x
