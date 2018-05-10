import numpy as np
import matplotlib.pyplot as plt
val = 0. # this is the value where you want the data to appear on the y-axis.
ar = (np.random.rand(56))*-100-90# just as an example array
plt.plot(ar, np.zeros_like(ar) + val, 'x')
plt.axis((-200,0,-0.25,0.25))
plt.xlabel('Head position', fontsize=18)
plt.show()