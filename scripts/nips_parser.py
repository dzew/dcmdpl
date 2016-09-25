from matplotlib.pyplot import *

# This file contains the code used to generate the plots in the NIPS paper.

f = open('../results/icml/pb/20kens04d')
j = 'X'
stf = []
while not j == '':
    j = f.readline()
    if 'XTest' in j:
        stf.append(j)
        stf.append(f.readline())
f.close()

class Dat():
    def __init__(self, start, steps, val, path):
        self.start = start
        self.steps = steps
        self.val = val
        self.path = path
    def __str__(s):
        return 'D' + str(s.start) + str(s.steps) +' '+ str(s.val) +' '+ s.path

def parse(a, b):
    a = a[12:-1]
    start = eval('[' + a.split('[')[1])
    a = a.split(' ')
    b = b[12:-1]
    return Dat(start, int(a[3]), float(a[5]), b)

# Prints a formatted string containing the results of the i^th test.
# Useful for visualizing trajectories with GuiTests
def prep(i):
    print it0[i].start
    print it0[i].steps, ' ', it1[i].steps, it2[i].steps
    print '{"%s","%s","%s"}'%(it0[i].path,it1[i].path,it2[i].path)

def makeCd(ix):
    m = [0] * (1 + max([i.steps for i in ix]))
    for i in ix:
        m[i.steps] += 1
    for i in xrange(len(m) - 1):
        m[i+1] += m[i]
    return m

# Function to generate the cumulative distribution plots.
def makePlots(num = 500,clip = 501):
    num *= 1.
    for ix,m,t in zip(its,['k-','r--','b-.'],['KBRL','DKBRL, Iter 1', 'DKBRL, Iter 2']):
        a = makeCd(ix)
        a = a[:clip]
        #print a
        a = [i/num for i in a]
        plot(a, m, label=t, linewidth=3)
    legend(loc = 'best', prop={'size':18})
    xlabel('Steps', fontsize=15)
    ylabel('Fraction Reaching Goal', fontsize=15)
    show()

it0 = []
it1 = []
it2 = []

#while len(stf) < 3000:
#    stf.append('     [java] XTest: 261 steps 500 rewards -1017.3683299959545 start [0.975283986288124, 0.7678305374311731, 0.40288785589691345, -0.31771732663201224]\n')
#    stf.append('     [java] DAWIT')

print len(stf)
tsts = len(stf) / 6
for i in xrange(tsts):
    it0.append(parse(stf[2 * i], stf[2 * i + 1]))
    it1.append(parse(stf[2 * tsts + 2 * i], stf[2 * tsts + 1  + 2 * i]))
    it2.append(parse(stf[4 * tsts + 2 * i], stf[4 * tsts + 1 + 2 * i]))

its = (it0, it1, it2)
makePlots(tsts,1002)
