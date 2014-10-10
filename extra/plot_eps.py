import matplotlib.pyplot as plt

x = [.1, .05, .01, .005, .001, .0005, .0001, .00005, .00001]
french = [.4034, .4034, .4034, .4034, .3674, .3604, .3554, .3544, .3534]
chinese = [.6280, .6280, .6280, .6280, .5852, .5906, .5883, .5880, .5875]
hindi = [.6424, .6424, .6424, .6091, .5916, .5876, .5828, .5852, .5852]
fig = plt.figure()
ax = fig.add_subplot(111)
ax.plot(x, french, color='b', label='French', marker='o')
ax.plot(x, chinese, color='r', label='Chinese', marker='o')
ax.plot(x, hindi, color='g', label='Hindi', marker='o')

ax.set_xscale('log')
plt.gca().invert_xaxis()
plt.xlabel('Epsilon')
plt.ylabel('AER')
plt.title('AER vs Epsilon on dev 10k, IBM Model 1')
plt.legend()
plt.show()
