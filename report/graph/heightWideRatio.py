import numpy as np
import matplotlib.pyplot as plt

# Fixing random state for reproducibility
np.random.seed(19680801)


N = 8
x = np.random.normal(423,30, N)
y = np.random.normal(1160,38, N)
colors = np.random.rand(N)
area = 24000+np.random.rand(N)*24800  # 0 to 15 point radii

plt.scatter(x, y, s=area, c=colors, alpha=0.5)
plt.axis((350,500,1050,1300))
plt.title('Silouette identification', fontsize=30)
plt.xlabel('Width of subject', fontsize=18)
plt.ylabel('Height of subject', fontsize=18)
plt.show()
