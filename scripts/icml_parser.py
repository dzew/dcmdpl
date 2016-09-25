from matplotlib.pyplot import *

# This file contains the code used to generate the plots in the ICML paper.
class Dat():
    def __init__(self, start, steps, val, path):
        self.start = start
        self.steps = steps
        self.val = val
        self.path = path
    def __str__(s):
        return 'D' + str(s.start) + str(s.steps) +' '+ str(s.val) +' '+ s.path

def load(fname):
    f = open(fname)
    j = 'X'
    stf = []
    while not j == '':
        j = f.readline()
        if 'XTest' in j:
            stf.append(j)
            stf.append(f.readline())
    f.close()
    return stf

def parse(a, b):
    a = a[12:-1]
    start = eval('[' + a.split('[')[1])
    a = a.split(' ')
    b = b[12:-1]
    return Dat(start, int(a[3]), float(a[5]), b)

def makeCd(ix):
    m = [0] * (1 + max([i.steps for i in ix]))
    for i in ix:
        m[i.steps] += 1
    for i in xrange(len(m) - 1):
        m[i+1] += m[i]
    return m

def stderr(lst):
    ave = sum(lst) * 1.0 / len(lst)
    nsum = sum([(i-ave)*(i-ave) for i in lst])
    return (ave, (nsum/(len(lst) - 1))**.5/len(lst)**.5)

bwid = '9'
files = ['../results/icml/pb/20kens0' + bwid + i for i in 'abcd']
results = [[],[],[]]

for f in files:
    stf = load(f)
    its = [[],[],[]]
    tsts = len(stf) / 6
    print '%s had %d tests', f, tsts
    for i in xrange(tsts):
        its[0].append(parse(stf[2 * i], stf[2 * i + 1]))
        its[1].append(parse(stf[2 * tsts + 2 * i], stf[2 * tsts + 1  + 2 * i]))
        its[2].append(parse(stf[4 * tsts + 2 * i], stf[4 * tsts + 1 + 2 * i]))
    for i, ix in enumerate(its):
        cdf = makeCd(ix)
        z = 1.0 * cdf[-1]
        results[i].append([i/z for i in cdf])

lbs = ['KBRL','DKBRL, Iter 1', 'DKBRL, Iter 2']
for r,ln,lb,sb,cr  in zip(results,['k-','r--','b-.'],lbs,'d*+','brk'):
    ms = []
    es = []
    for x in xrange(len(r[0])):
        m,e = stderr([r[i][x] for i in xrange(len(files))])
        ms.append(m)
        es.append(e)
    errorbar(range(len(ms))[::7], ms[::7], fmt='-'+sb, yerr=es[::7], label=lb,color=cr)

xlabel('Steps', fontsize=15)
ylabel('Fraction Reaching Goal', fontsize=15)
title('Solution qualities, b=.0'+bwid)
legend(loc = 'best', prop={'size':28})
ylim(0.,1.)
show()
