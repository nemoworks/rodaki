import numpy as np
import os
class StandardScaler:
    """
    Standard the input
    """

    def __init__(self, mean, std):
        self.mean = mean
        self.std = std

    def transform(self, data):
        return (data - self.mean) / self.std

    def inverse_transform(self, data):
        return (data * self.std) + self.mean

def load_dataset(dataset_dir, train_batch_size, test_batch_size=None):
    data = {}
    for category in ['train', 'val', 'test']:
        cat_data = np.load(os.path.join(dataset_dir, category + '.npz'))
        data['x_' + category] = cat_data['x'].astype('float32')
        data['y_' + category] = cat_data['y'].astype('float32')
        print(data['x_' + category].shape,data['y_' + category].shape)
    scaler = StandardScaler(mean=data['x_train'][..., 0].mean(), std=data['x_train'][..., 0].std())
    # Data format
    #for category in ['train', 'val', 'test']:
    #    data['x_' + category][..., 0] = scaler.transform(data['x_' + category][..., 0])
    #    data['y_' + category][..., 0] = scaler.transform(data['y_' + category][..., 0])

    return data,scaler