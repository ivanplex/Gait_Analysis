import numpy as np
import matplotlib.pyplot as plt
val = 0. # this is the value where you want the data to appear on the y-axis.
ar = (np.random.rand(56) -0.5)*230# just as an example array
plt.plot(ar, np.zeros_like(ar) + val, 'x')
plt.xlabel('Head position relative to arm', fontsize=18)
plt.show()