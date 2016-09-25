import matplotlib.pyplot as pt

# This file contains the code used to generate plots in my thesis.
def stderr(lst):
    ave = sum(lst) * 1.0 / len(lst)
    nsum = sum([(i-ave)*(i-ave) for i in lst])
    return (nsum/(len(lst) - 1))**.5/len(lst)**.5

def process(base, n):
    outs = []
    bs = []
    res = []
    clear = False
    for i in xrange(1,n+1):
        f = open(base + str(i))
        j = f.readline()
        while (not 'End computation' in j) and j != '':
            if 'Processed bandwidth' in j:
                outs = []
                clear = True
            elif 'on iteration' in j:
                outs = []
                clear = False
            elif clear:
                outs.append(j)
            j = f.readline()
        f.close()
        print 'found marker'
        k = 0
        for j in outs:
            line = j[12:].replace('[','').replace(',','').replace(']\n','')
            line = line.split(' ')
            if i == 1:
                bs.append(float(line[0]))
                res.append([[] for x in line[1:]])
            for m,x in enumerate(line[1:]):
                res[k][m].append(int(x))
            k += 1
        f.close()
    return (bs, res)

n = 20
b1, r1 = process('../results/facfun/bul', 20) #112
b2, r2 = process('../results/facfin/bul', 20)
bs = [b2[0], b1[0], b2[1], b1[1], b2[2], b1[2], b1[3]]
res = [r2[0], r1[0], r2[1], r1[1], r2[2], r1[2], r1[3]]
#bs,res = process('./fac07/red', n)

sums = []
errs = []
for i in xrange(len(res[0])):
    sums.append([sum(x[i]) for x in res])
    errs.append([stderr(x[i]) for x in res])

kbrl = [i[0] for i in res]

mins = [[9999999.9]*n for i in bs]
for i,q in enumerate(res):
    for j,w in enumerate(q):
        for k,e in enumerate(w):
            mins[i][k] = min(mins[i][k], e)
merrs = [stderr(i) for i in mins]
msums = [sum(i)*1./n for i in mins]


sums = [[1. * x / n for x in i] for i in sums]

print bs
print sums[0]
print errs[0]
for i in xrange(len(res[0])):
    pt.errorbar(bs, sums[i], yerr=errs[i], label='Iteration '+str(i))
pt.xlabel('Bandwidth')
pt.ylabel('Solution Length')
pt.title('Acrobot Solution Quality vs. Bandwidth')
pt.legend()
pt.show()

pt.errorbar(bs, sums[0], yerr=errs[0], label='KBRL')
pt.errorbar(bs, msums, yerr=merrs, label='DKBRL')

pt.xlabel('Bandwidth')
pt.ylabel('Solution Length')
pt.title('Acrobot Solution Quality vs. Bandwidth')
pt.legend()
pt.show()
