import matplotlib.pyplot as plt

x = [.1, .05, .01, .005, .001, .0005, .0001, .00005, .00001]
french = [.3413, .3564, .3173, .3193, .3153, .3193, .3253, .3173, .3193]
chinese = [.5978, .5983, .5890, .5878, .5857, .5866, .5911, .5931, .5890]
hindi = [.6305, .6019, .6138, .6186, .6186, .6067, .6043, .6162, .6043]
fig = plt.figure()
ax = fig.add_subplot(111)
ax.plot(x, french, color='b', label='French', marker='o')
ax.plot(x, chinese, color='r', label='Chinese', marker='o')
ax.plot(x, hindi, color='g', label='Hindi', marker='o')

ax.set_xscale('log')
plt.gca().invert_xaxis()
plt.xlabel('Epsilon')
plt.ylabel('AER')
plt.title('AER vs Epsilon on dev 10k, IBM Model 2')
plt.legend()
plt.show()
